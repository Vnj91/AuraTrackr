package com.example.auratrackr.features.focus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.auratrackr.R
import com.example.auratrackr.features.focus.tracking.UsageTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Foreground Service responsible for running the app usage tracking task
 * periodically in the background. It uses [LifecycleService] to gain access
 * to a coroutine scope that is automatically managed with the service's lifecycle.
 */
@AndroidEntryPoint
class UsageTrackingService : LifecycleService() {

    @Inject
    lateinit var usageTracker: UsageTracker

    private var trackingJob: Job? = null

    companion object {
        private const val TAG = "UsageTrackingService"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "UsageTrackingChannel"
        private const val TRACKING_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes

        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_SERVICE -> startTracking()
            ACTION_STOP_SERVICE -> stopTracking()
        }
        // START_STICKY ensures the service will be restarted by the system if it's killed.
        return Service.START_STICKY
    }

    private fun startTracking() {
        // Promote the service to a foreground service to prevent it from being killed by the system.
        startForeground(NOTIFICATION_ID, createNotification())
        Log.i(TAG, "Usage tracking service started.")

        // Start the periodic tracking coroutine only if it's not already active.
        if (trackingJob?.isActive != true) {
            trackingJob = lifecycleScope.launch {
                while (isActive) {
                    try {
                        Log.d(TAG, "Executing usage tracking task.")
                        usageTracker.trackUsage()
                    } catch (e: Exception) {
                        // Catching exceptions is crucial to prevent the service from crashing.
                        Log.e(TAG, "Error during usage tracking", e)
                    }
                    delay(TRACKING_INTERVAL_MS)
                }
            }
        }
    }

    private fun stopTracking() {
        Log.i(TAG, "Stopping usage tracking service.")
        // Cancel the tracking coroutine.
        trackingJob?.cancel()
        // Stop the foreground service and remove the notification.
        stopForeground(STOP_FOREGROUND_REMOVE)
        // Stop the service itself.
        stopSelf()
    }

    /**
     * Creates the persistent notification required for a Foreground Service.
     * This notification informs the user that the app is running in the background.
     */
    private fun createNotification(): Notification {
        // Create a notification channel for Android 8.0 (Oreo) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "AuraTrackr Focus Engine",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps track of your app usage to help you stay focused."
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("AuraTrackr is active")
            .setContentText("Monitoring app usage to help you reach your goals.")
            .setSmallIcon(R.drawable.ic_logo_notification) // Ensure you have a dedicated notification icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure the coroutine is cancelled when the service is destroyed for any reason.
        trackingJob?.cancel()
        Log.d(TAG, "Service destroyed.")
    }
}