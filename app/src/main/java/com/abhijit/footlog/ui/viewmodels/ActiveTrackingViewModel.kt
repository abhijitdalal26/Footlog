package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.LatLngPoint
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import com.abhijit.footlog.service.LocationTrackingService
import com.abhijit.footlog.util.estimateCalories
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TrackingUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val distanceMeters: Float = 0f,
    val elapsedSeconds: Long = 0L,
    val routePoints: List<LatLngPoint> = emptyList(),
    val highlights: List<HighlightEntity> = emptyList(),
    val currentLocation: Location? = null,
    val isStarted: Boolean = false,
    val caloriesBurned: Int = 0,
    val currentSpeedKmh: Float = 0f,
    val isLocating: Boolean = true
)

class ActiveTrackingViewModel(
    app: Application,
    private val activityType: String
) : AndroidViewModel(app) {

    private val repo = SessionRepository(app)
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(app)

    private var actualStartTime = 0L
    private var lastLocation: Location? = null
    private var timerJob: Job? = null

    // High-frequency pre-tracking location callback for the blue dot
    private val preTrackingCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            _uiState.update { it.copy(currentLocation = loc, isLocating = false) }
        }
    }

    // High-frequency tracking callback for route recording
    private val trackingCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            onNewTrackingLocation(loc)
        }
    }

    private val preTrackingRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 2000L
    ).setMinUpdateIntervalMillis(1000L).build()

    private val trackingRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 1500L
    ).setMinUpdateIntervalMillis(750L).build()

    init {
        startPreTrackingLocationUpdates()
        // Timeout: stop showing "Finding your location..." after 20s if no fix
        viewModelScope.launch {
            delay(20_000L)
            if (_uiState.value.isLocating && !_uiState.value.isStarted) {
                _uiState.update { it.copy(isLocating = false) }
            }
        }
    }

    private fun startPreTrackingLocationUpdates() {
        try {
            // Get last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null && _uiState.value.currentLocation == null) {
                    _uiState.update { it.copy(currentLocation = loc, isLocating = false) }
                }
            }
            // Then register for live updates
            fusedLocationClient.requestLocationUpdates(
                preTrackingRequest,
                preTrackingCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            _uiState.update { it.copy(isLocating = false) }
        }
    }

    private fun onNewTrackingLocation(loc: Location) {
        val state = _uiState.value
        if (!state.isStarted) return

        // Relaxed accuracy filter — 100m covers most real-world urban scenarios
        if (loc.accuracy > 100f) return

        val prev = lastLocation
        val added = if (prev != null) prev.distanceTo(loc) else 0f

        // Only skip if barely moved (< 1m) AND we already have a point
        if (prev != null && added < 1f) {
            // Still update current location and speed even if not moving much
            _uiState.update { s ->
                s.copy(
                    currentLocation = loc,
                    currentSpeedKmh = if (loc.hasSpeed()) loc.speed * 3.6f else 0f
                )
            }
            return
        }

        lastLocation = loc
        val newPoint = LatLngPoint(loc.latitude, loc.longitude)
        _uiState.update { s ->
            val newDist = s.distanceMeters + added
            s.copy(
                currentLocation = loc,
                routePoints = s.routePoints + newPoint,
                distanceMeters = newDist,
                caloriesBurned = estimateCalories(newDist, activityType),
                currentSpeedKmh = if (loc.hasSpeed()) loc.speed * 3.6f else 0f
            )
        }
        viewModelScope.launch { repo.insertExploredCell(loc.latitude, loc.longitude) }
    }

    fun startTracking() {
        // Stop pre-tracking updates
        try { fusedLocationClient.removeLocationUpdates(preTrackingCallback) } catch (_: Exception) {}

        _uiState.update { it.copy(isStarted = true) }
        actualStartTime = System.currentTimeMillis()

        // Start foreground service (also tracks in background)
        getApplication<Application>().startForegroundService(
            Intent(getApplication(), LocationTrackingService::class.java)
        )

        // Register high-accuracy tracking updates directly in ViewModel
        // (Service handles the notification; we drive location from here for lower latency)
        try {
            fusedLocationClient.requestLocationUpdates(
                trackingRequest,
                trackingCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {}

        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - actualStartTime) / 1000
                _uiState.update { it.copy(elapsedSeconds = elapsed) }
            }
        }
    }

    fun addHighlight(lat: Double, lng: Double, category: String, emoji: String, name: String, note: String?) {
        val h = HighlightEntity(
            id = UUID.randomUUID().toString(),
            sessionId = _uiState.value.sessionId,
            lat = lat, lng = lng,
            category = category, emoji = emoji, name = name, note = note
        )
        viewModelScope.launch { repo.saveHighlight(h) }
        _uiState.update { it.copy(highlights = it.highlights + h) }
    }

    fun addPhotoHighlight(photoPath: String?, lat: Double, lng: Double) {
        val h = HighlightEntity(
            id = UUID.randomUUID().toString(),
            sessionId = _uiState.value.sessionId,
            lat = lat, lng = lng,
            category = "photo", emoji = "📷", name = "Photo",
            photoPath = photoPath
        )
        viewModelScope.launch { repo.saveHighlight(h) }
        _uiState.update { it.copy(highlights = it.highlights + h) }
    }

    fun stopTracking() {
        timerJob?.cancel()
        try { fusedLocationClient.removeLocationUpdates(trackingCallback) } catch (_: Exception) {}
        try { fusedLocationClient.removeLocationUpdates(preTrackingCallback) } catch (_: Exception) {}
        getApplication<Application>().stopService(
            Intent(getApplication(), LocationTrackingService::class.java)
        )
        val state = _uiState.value
        val session = SessionEntity(
            id = state.sessionId,
            activityType = activityType,
            startTime = actualStartTime,
            endTime = System.currentTimeMillis(),
            distanceMeters = state.distanceMeters,
            title = activityType.replaceFirstChar { it.uppercase() },
            routePoints = state.routePoints
        )
        viewModelScope.launch(kotlinx.coroutines.NonCancellable) { repo.saveSession(session) }
    }

    override fun onCleared() {
        super.onCleared()
        try { fusedLocationClient.removeLocationUpdates(preTrackingCallback) } catch (_: Exception) {}
        try { fusedLocationClient.removeLocationUpdates(trackingCallback) } catch (_: Exception) {}
    }

    companion object {
        fun Factory(activityType: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
                return ActiveTrackingViewModel(app, activityType) as T
            }
        }
    }
}
