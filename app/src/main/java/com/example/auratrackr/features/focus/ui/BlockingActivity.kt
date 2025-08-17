package com.example.auratrackr.features.focus.ui

import android.content.Intent
import android.os.Bundle
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
 * This activity is responsible for preventing user access to an app that has exceeded its
 * usage budget and providing options to gain a temporary grace period.
 */
@AndroidEntryPoint
class BlockingActivity : ComponentActivity() {

    companion object {
        // Use a constant for the intent extra key to ensure consistency and prevent typos.
        const val EXTRA_NAVIGATE_TO = "NAVIGATE_TO"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockedPackageName = intent.getStringExtra(FocusAccessibilityService.EXTRA_PACKAGE_NAME)

        // Gracefully handle the edge case where the package name is missing.
        if (blockedPackageName.isNullOrEmpty()) {
            finish() // Close the activity immediately if there's nothing to block.
            return
        }

        // Disable the system back button to prevent the user from easily bypassing the block.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing to effectively disable the back press.
            }
        })

        setContent {
            AuraTrackrTheme(darkTheme = true) { // Assuming a dark theme for the overlay
                BlockingScreen(
                    onClose = {
                        // Close the blocking overlay. The user will return to the blocked app,
                        // which will likely be immediately blocked again unless they gained a grace period.
                        finish()
                    },
                    onNavigateToTask = {
                        // Launch the main app and tell it to navigate to the AuraTask screen,
                        // passing the package name of the app that needs to be unblocked.
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra(EXTRA_NAVIGATE_TO, Screen.AuraTask.createRoute(blockedPackageName))
                            // This flag brings an existing MainActivity to the front instead of creating a new one.
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(intent)
                        finish() // Close the blocking screen itself.
                    }
                )
            }
        }
    }
}