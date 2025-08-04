package com.example.auratrackr.features.focus.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.auratrackr.R
import com.example.auratrackr.features.focus.tracking.UsageTracker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A Foreground Service responsible for running the app usage tracking task
 * periodically in the background.
 */
@AndroidEntryPoint
class UsageTrackingService : LifecycleService() {

    // Hilt injects the UsageTracker instance for us.
    @Inject
    lateinit var usageTracker: UsageTracker

    private var trackingJob: Job? = null

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "UsageTrackingChannel"
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val TRACKING_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_SERVICE -> startTracking()
            ACTION_STOP_SERVICE -> stopTracking()
        }
        return Service.START_STICKY
    }

    private fun startTracking() {
        startForeground(NOTIFICATION_ID, createNotification())

        // Start the periodic tracking coroutine if it's not already running.
        if (trackingJob?.isActive != true) {
            trackingJob = lifecycleScope.launch {
                while (true) {
                    try {
                        usageTracker.trackUsage()
                    } catch (e: Exception) {
                        // It's important to catch exceptions to prevent the service from crashing.
                        e.printStackTrace()
                    }
                    delay(TRACKING_INTERVAL_MS)
                }
            }
        }
    }

    private fun stopTracking() {
        // Cancel the tracking coroutine and stop the service.
        trackingJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * Creates the persistent notification required for a Foreground Service.
     */
    private fun createNotification(): Notification {
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
            .setSmallIcon(R.drawable.ic_logo) // Replace with your app's notification icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure the coroutine is cancelled when the service is destroyed.
        trackingJob?.cancel()
    }
}
