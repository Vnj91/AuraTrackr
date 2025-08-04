package com.example.auratrackr.core.permissions

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import com.example.auratrackr.features.focus.service.FocusAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton manager class responsible for checking and handling
 * the special permissions required for the Focus Engine.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Checks if the app has been granted Usage Stats access.
     * @return True if access is granted, false otherwise.
     */
    fun isUsageAccessGranted(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Checks if our app's Accessibility Service is enabled in the system settings.
     * @return True if the service is enabled, false otherwise.
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        val service = "${context.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(service, ignoreCase = false) == true
    }
}
