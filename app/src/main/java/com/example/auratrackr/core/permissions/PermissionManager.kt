package com.example.auratrackr.core.permissions

import android.app.AppOpsManager
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.example.auratrackr.features.focus.service.FocusAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton manager class responsible for checking the status of special permissions
 * required for the app's core functionality, such as the Focus Engine.
 *
 * This class centralizes permission-checking logic to ensure it is consistent
 * and easy to maintain.
 *
 * @param context The application context provided by Hilt.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "PermissionManager"
    }

    /**
     * Checks if the app has been granted Usage Stats access, which is necessary
     * for tracking application usage times.
     *
     * @return `true` if access is granted, `false` otherwise.
     */
    fun isUsageAccessGranted(): Boolean {
        // Platform APIs here can throw a variety of vendor / device specific runtime
        // exceptions (ClassCastException, SecurityException, NullPointerException) when
        // system services or permissions are missing. Narrowing to a single specific
        // exception is brittle across Android versions/vendors, so we add a targeted
        // suppression for this one-line helper while still logging the failure.
        @Suppress("TooGenericExceptionCaught")
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "isUsageAccessGranted check failed, assuming false")
            false
        }
    }

    /**
     * Checks if the app's [FocusAccessibilityService] is enabled in the system settings.
     * This is required to detect foreground apps and display blocking overlays.
     *
     * This implementation is robustly designed to handle different vendor formats by
     * splitting the enabled services string and performing a case-insensitive check.
     *
     * @return `true` if the service is enabled, `false` otherwise.
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedServiceId = "${context.packageName}/${FocusAccessibilityService::class.java.canonicalName}"
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false // If the setting is null, our service cannot be enabled.

        val stringSplitter = TextUtils.SimpleStringSplitter(':')
        stringSplitter.setString(enabledServicesSetting)

        while (stringSplitter.hasNext()) {
            val componentName = stringSplitter.next()
            if (componentName.equals(expectedServiceId, ignoreCase = true)) {
                return true
            }
        }

        return false
    }
}
