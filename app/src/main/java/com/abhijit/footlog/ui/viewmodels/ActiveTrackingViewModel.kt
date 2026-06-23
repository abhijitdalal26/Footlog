package com.abhijit.footlog.ui.viewmodels

import android.app.Application
import android.content.Intent
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.abhijit.footlog.data.entity.HighlightEntity
import com.abhijit.footlog.data.entity.LatLngPoint
import com.abhijit.footlog.data.entity.SessionEntity
import com.abhijit.footlog.data.repository.SessionRepository
import com.abhijit.footlog.service.LocationTrackingService
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
    val countdownSeconds: Int = 3
)

class ActiveTrackingViewModel(
    app: Application,
    private val activityType: String
) : AndroidViewModel(app) {

    private val repo = SessionRepository(app)
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var actualStartTime = 0L
    private var lastLocation: Location? = null
    private var timerJob: Job? = null

    init {
        startCountdown()
    }

    private fun startCountdown() {
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(countdownSeconds = i) }
                delay(1000L)
            }
            _uiState.update { it.copy(countdownSeconds = 0) }
            beginTracking()
        }
    }

    private fun beginTracking() {
        actualStartTime = System.currentTimeMillis()
        getApplication<Application>().startForegroundService(
            Intent(getApplication(), LocationTrackingService::class.java)
        )
        startTimer()
        collectLocation()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun collectLocation() {
        viewModelScope.launch {
            LocationTrackingService.locationFlow.filterNotNull().collect { loc ->
                if (loc.accuracy > 50f) return@collect

                val prev = lastLocation
                val added = if (prev != null) prev.distanceTo(loc) else 0f
                if (prev != null && added < 2f) return@collect

                lastLocation = loc
                val newPoint = LatLngPoint(loc.latitude, loc.longitude)
                _uiState.update { s ->
                    s.copy(
                        currentLocation = loc,
                        routePoints = s.routePoints + newPoint,
                        distanceMeters = s.distanceMeters + added
                    )
                }
                repo.insertExploredCell(loc.latitude, loc.longitude)
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
