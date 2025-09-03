package com.example.auratrackr.features.focus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.auratrackr.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * A foreground service for tracking app usage during focus sessions.
 * Marked with @AndroidEntryPoint to allow Hilt to inject dependencies.
 *
 * This class now correctly extends android.app.Service to resolve the Hilt compile error.
 * It also implements the necessary foreground service logic to comply with modern Android
 * background execution limits.
 */
@AndroidEntryPoint
class UsageTrackingService : Service() {

    // Lazy initialization of the NotificationManager system service.
    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Called by the system when the service is first created.
     * This is the ideal place to set up one-time initializations.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    /**
     * Called every time the service is started with startService() or startForegroundService().
     * This is where the main logic of the service resides.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create the persistent notification that is required for a foreground service.
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focus Session Active")
            .setContentText("AuraTrackr is monitoring your app usage.")
            // Ensure this drawable icon exists in your res/drawable folder.
            .setSmallIcon(R.drawable.ic_placeholder_app_icon)
            .setOngoing(true) // Makes the notification non-dismissible.
            .build()

        // This call promotes the service to a foreground service, showing the notification.
        startForeground(NOTIFICATION_ID, notification)

        // =======================================================
        // TODO: Add your custom app usage tracking logic here.
        // This could involve starting a coroutine to periodically
        // check the user's current foreground app.
        // =======================================================

        // This flag ensures that if the system kills the service, it will be automatically
        // restarted once resources are available.
        return START_STICKY
    }

    /**
     * Called when the service is no longer used and is being destroyed.
     * This is the place to clean up any resources.
     */
    override fun onDestroy() {
        super.onDestroy()
        // =======================================================
        // TODO: Stop your usage tracking logic and clean up any
        // resources (e.g., cancel coroutines).
        // =======================================================
    }

    /**
     * This service does not support binding, so we return null.
     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Creates the NotificationChannel required on Android 8.0 (API 26) and higher
     * for displaying notifications.
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Usage Tracking Service", // Name shown to the user in app settings.
            NotificationManager.IMPORTANCE_LOW // Use LOW to prevent sound/vibration.
        ).apply {
            description = "Channel for the active focus session notification."
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Companion object to hold constants related to the service.
     */
    companion object {
        private const val CHANNEL_ID = "UsageTrackingServiceChannel"
        private const val NOTIFICATION_ID = 12345 // A unique ID for the notification.
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }
}

