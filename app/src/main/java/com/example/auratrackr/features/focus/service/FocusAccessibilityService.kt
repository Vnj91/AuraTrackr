package com.example.auratrackr.features.focus.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.auratrackr.features.focus.tracking.BlockerState
import com.example.auratrackr.features.focus.ui.BlockingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * The core Accessibility Service for AuraTrackr's Focus Engine.
 * This service is responsible for monitoring the foreground app and
 * showing a blocking overlay when usage limits are exceeded.
 */
@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var blockerState: BlockerState

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("FocusAccessibility", "Service Connected")
        // Start collecting the state of apps that are over budget
        serviceScope.launch {
            blockerState.appsOverBudget.collectLatest { appsOverBudget ->
                Log.d("FocusAccessibility", "Apps over budget: $appsOverBudget")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // We are interested in events that indicate a window state has changed,
        // as this is a reliable way to detect when a new app is opened.
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Avoid blocking system UI (like the home screen or notification shade) and our own app.
            if (packageName == "com.example.auratrackr" || isSystemUi(packageName)) {
                lastBlockedPackage = null // Reset when user navigates away from a blocked app
                return
            }

            // Check if the current app is in our set of apps to be blocked.
            val appsToBlock = blockerState.appsOverBudget.value
            if (packageName in appsToBlock) {
                // To prevent an infinite loop of blocking, we check if we just blocked this app.
                // This stops the service from re-blocking the app when our own BlockingActivity closes.
                if (packageName != lastBlockedPackage) {
                    lastBlockedPackage = packageName
                    Log.d("FocusAccessibility", "Blocking app: $packageName")

                    // Launch the BlockingActivity as an overlay
                    val intent = Intent(this, BlockingActivity::class.java).apply {
                        // This flag is required to start an Activity from a Service.
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * A helper function to filter out common system UI packages.
     */
    private fun isSystemUi(packageName: String?): Boolean {
        return packageName?.startsWith("com.android.systemui") == true ||
                packageName?.contains("launcher") == true
    }

    override fun onInterrupt() {
        // This method is called when the service is interrupted by the system.
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine scope to clean up resources and prevent leaks.
        serviceScope.cancel()
    }
}
