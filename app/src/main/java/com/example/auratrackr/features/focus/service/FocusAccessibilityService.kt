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

@AndroidEntryPoint
class FocusAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var blockerState: BlockerState

    @Inject
    lateinit var unblockManager: TemporaryUnblockManager // <-- INJECT THE MANAGER

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastBlockedPackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("FocusAccessibility", "Service Connected")
        serviceScope.launch {
            blockerState.appsOverBudget.collectLatest { appsOverBudget ->
                Log.d("FocusAccessibility", "Apps over budget: $appsOverBudget")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            if (packageName == "com.example.auratrackr" || isSystemUi(packageName)) {
                lastBlockedPackage = null
                return
            }

            // *** THIS IS THE UPDATE ***
            // Before blocking, check if the app is in a temporary grace period.
            if (unblockManager.isAppTemporarilyUnblocked(packageName)) {
                Log.d("FocusAccessibility", "App is temporarily unblocked: $packageName")
                return // Do not block the app
            }

            val appsToBlock = blockerState.appsOverBudget.value
            if (packageName in appsToBlock) {
                if (packageName != lastBlockedPackage) {
                    lastBlockedPackage = packageName
                    Log.d("FocusAccessibility", "Blocking app: $packageName")

                    val intent = Intent(this, BlockingActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("PACKAGE_NAME", packageName)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun isSystemUi(packageName: String?): Boolean {
        return packageName?.startsWith("com.android.systemui") == true ||
                packageName?.contains("launcher") == true
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
