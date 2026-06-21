package com.abhijit.footlog.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationTrackingService : Service() {

    companion object {
        const val CHANNEL_ID = "footlog_tracking"
        const val NOTIFICATION_ID = 1
        private val _locationFlow = MutableStateFlow<Location?>(null)
        val locationFlow: StateFlow<Location?> = _locationFlow
        var isRunning = false
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
        .setMinUpdateIntervalMillis(1500L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { _locationFlow.value = it }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        isRunning = true
        try {
            fusedClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (_: SecurityException) {}
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedClient.removeLocationUpdates(locationCallback)
        _locationFlow.value = null
        isRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Footlog Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Active session tracking" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking your route")
            .setContentText("Footlog is recording your session")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
}
