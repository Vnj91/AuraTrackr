package com.example.auratrackr.features.focus.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.example.auratrackr.MainActivity
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.focus.service.FocusAccessibilityService
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * A transparent activity that acts as an overlay to block other apps.
 * It hosts the [BlockingScreen] composable and is launched by the [FocusAccessibilityService].
 */
@AndroidEntryPoint
class BlockingActivity : ComponentActivity() {

    companion object {
        const val EXTRA_NAVIGATE_TO = "NAVIGATE_TO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackageName = intent.getStringExtra(FocusAccessibilityService.EXTRA_PACKAGE_NAME)

        if (blockedPackageName.isNullOrEmpty()) {
            finish()
            return
        }

        // ✅ FIX: Provide feedback to the user when they press the back button.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@BlockingActivity, "Back is disabled in focus mode.", Toast.LENGTH_SHORT).show()
            }
        })

        setContent {
            // ✅ FIX: Removed the forced dark theme. The theme will now correctly match the user's system/saved preference.
            AuraTrackrTheme {
                BlockingScreen(
                    onClose = {
                        finish()
                    },
                    onNavigateToTask = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra(EXTRA_NAVIGATE_TO, Screen.AuraTask.createRoute(blockedPackageName))
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
