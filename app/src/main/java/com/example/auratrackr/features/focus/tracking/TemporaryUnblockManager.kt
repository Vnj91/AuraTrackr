package com.example.auratrackr.features.focus.tracking

import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

/**
 * A singleton class that manages the state of temporarily unblocked apps.
 * This provides a grace period after a user spends points, waits, or completes a task.
 */
@Singleton
class TemporaryUnblockManager @Inject constructor() {

    // A thread-safe map to store the package name and the timestamp when its grace period expires.
    private val unblockedApps = ConcurrentHashMap<String, Long>()

    companion object {
        private const val GRACE_PERIOD_MINUTES = 5
    }

    /**
     * Grants a temporary grace period for a specific app.
     *
     * @param packageName The package name of the app to unblock.
     */
    fun grantTemporaryUnblock(packageName: String) {
        val expiryTime = System.currentTimeMillis() + (GRACE_PERIOD_MINUTES * 60 * 1000)
        unblockedApps[packageName] = expiryTime
    }

    /**
     * Checks if a specific app is currently within its grace period.
     *
     * @param packageName The package name of the app to check.
     * @return True if the app is unblocked, false otherwise.
     */
    fun isAppTemporarilyUnblocked(packageName: String): Boolean {
        val expiryTime = unblockedApps[packageName] ?: return false

        // If the current time is past the expiry time, the grace period is over.
        if (System.currentTimeMillis() > expiryTime) {
            // Clean up the expired entry.
            unblockedApps.remove(packageName)
            return false
        }

        return true
    }
}
