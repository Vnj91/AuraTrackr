package com.example.auratrackr.features.focus.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.auratrackr.features.focus.tracking.BlockerState
import com.example.auratrackr.features.focus.tracking.TemporaryUnblockManager
import com.example.auratrackr.features.focus.ui.BlockingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
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
        Timber.tag(TAG).d("Service Connected and listening for events.")
        serviceScope.launch {
            blockerState.appsOverBudget.collectLatest { appsOverBudget ->
                Timber.tag(TAG).d("Apps over budget updated: $appsOverBudget")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (packageName == applicationContext.packageName || isSystemUi(packageName)) {
            lastBlockedPackage = null // Reset the last blocked app if we switch to a safe app.
            return
        }

        if (unblockManager.isAppTemporarilyUnblocked(packageName)) {
            Timber.tag(TAG).d("App '$packageName' is temporarily unblocked. Skipping block.")
            return
        }

        val appsToBlock = blockerState.appsOverBudget.value
        if (packageName in appsToBlock) {
            if (packageName != lastBlockedPackage) {
                lastBlockedPackage = packageName
                Timber.tag(TAG).i("Blocking app: $packageName")
                launchBlockingActivity(packageName)
            }
        } else {
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
        Timber.tag(TAG).d("Service Destroyed.")
    }
}
