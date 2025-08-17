package com.example.auratrackr.features.focus.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.auratrackr.features.focus.tracking.BlockerState
import com.example.auratrackr.features.focus.tracking.TemporaryUnblockManager
import com.example.auratrackr.features.focus.ui.BlockingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * An Accessibility Service responsible for monitoring the foreground application
 * and launching the [BlockingActivity] if the app has exceeded its usage budget.
 *
 * This service is the core of the Focus Engine's enforcement mechanism.
 */
@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FocusAccessibility"
        const val EXTRA_PACKAGE_NAME = "PACKAGE_NAME"
    }

    @Inject
    lateinit var blockerState: BlockerState

    @Inject
    lateinit var unblockManager: TemporaryUnblockManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service Connected and listening for events.")
        // Optionally collect the state here for debugging purposes.
        serviceScope.launch {
            blockerState.appsOverBudget.collectLatest { appsOverBudget ->
                Log.d(TAG, "Apps over budget updated: $appsOverBudget")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We only care about window state changes, which indicate a new app is in the foreground.
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own app and common system UI components to prevent blocking loops.
        if (packageName == applicationContext.packageName || isSystemUi(packageName)) {
            lastBlockedPackage = null // Reset the last blocked app if we switch to a safe app.
            return
        }

        // CRITICAL CHECK: Do not block the app if it's in a temporary grace period.
        if (unblockManager.isAppTemporarilyUnblocked(packageName)) {
            Log.d(TAG, "App '$packageName' is temporarily unblocked. Skipping block.")
            return
        }

        val appsToBlock = blockerState.appsOverBudget.value
        if (packageName in appsToBlock) {
            // Prevent launching multiple blocker activities for the same app.
            if (packageName != lastBlockedPackage) {
                lastBlockedPackage = packageName
                Log.i(TAG, "Blocking app: $packageName")
                launchBlockingActivity(packageName)
            }
        } else {
            // If the current app is not in the block list, clear the last blocked package.
            lastBlockedPackage = null
        }
    }

    private fun launchBlockingActivity(packageName: String) {
        val intent = Intent(this, BlockingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra(EXTRA_PACKAGE_NAME, packageName)
        }
        startActivity(intent)
    }

    /**
     * A helper to determine if a package is part of the core system UI or a launcher.
     */
    private fun isSystemUi(packageName: String?): Boolean {
        return packageName != null && (
                packageName.startsWith("com.android.systemui") ||
                        packageName.contains("launcher")
                )
    }

    override fun onInterrupt() {
        // This service does not need to handle interruptions.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service Destroyed.")
    }
}