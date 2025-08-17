package com.example.auratrackr.features.focus.tracking

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date // ✅ FIX: Added the missing import for Date
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton class that manages the state of temporarily unblocked apps.
 *
 * This provides a grace period after a user spends points, waits, or completes a task.
 * It uses a [ConcurrentHashMap] for thread-safe in-memory storage of grace period expiry times.
 * It also features a proactive cleanup mechanism to remove expired entries periodically.
 */
@Singleton
class TemporaryUnblockManager @Inject constructor() {

    private val unblockedApps = ConcurrentHashMap<String, Long>()
    private val cleanupScope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val GRACE_PERIOD_MINUTES = 5L
        private const val CLEANUP_INTERVAL_MS = 60 * 1000L // 1 minute
    }

    init {
        startPeriodicCleanup()
    }

    /**
     * Grants a temporary grace period for a specific app.
     *
     * @param packageName The package name of the app to unblock.
     */
    fun grantTemporaryUnblock(packageName: String) {
        val expiryTime = System.currentTimeMillis() + (GRACE_PERIOD_MINUTES * 60 * 1000)
        unblockedApps[packageName] = expiryTime
        Timber.d("Granted temporary unblock for '$packageName' until ${Date(expiryTime)}")
    }

    /**
     * Checks if a specific app is currently within its grace period.
     * This method also performs lazy cleanup of expired entries upon check.
     *
     * @param packageName The package name of the app to check.
     * @return `true` if the app is unblocked, `false` otherwise.
     */
    fun isAppTemporarilyUnblocked(packageName: String): Boolean {
        val expiryTime = unblockedApps[packageName] ?: return false

        if (System.currentTimeMillis() > expiryTime) {
            unblockedApps.remove(packageName)
            Timber.d("Grace period for '$packageName' expired. Removing entry.")
            return false
        }

        return true
    }

    /**
     * Starts a background coroutine to periodically clean up expired entries from the map.
     * This prevents the map from growing indefinitely with stale data.
     */
    private fun startPeriodicCleanup() {
        cleanupScope.launch {
            while (true) {
                delay(CLEANUP_INTERVAL_MS)
                val now = System.currentTimeMillis()
                var removedCount = 0
                unblockedApps.entries.removeIf { entry ->
                    val shouldRemove = entry.value < now
                    if (shouldRemove) removedCount++
                    shouldRemove
                }
                if (removedCount > 0) {
                    Timber.d("Proactive cleanup removed $removedCount expired entries.")
                }
            }
        }
    }
}
