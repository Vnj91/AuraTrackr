package com.example.auratrackr.features.focus.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.auratrackr.MainActivity
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * A transparent activity that acts as an overlay to block other apps.
 * It hosts the BlockingScreen composable.
 */
@AndroidEntryPoint
class BlockingActivity : ComponentActivity() {

    // Retrieve the blocked package name passed from the AccessibilityService
    private val blockedPackageName: String by lazy {
        intent.getStringExtra("PACKAGE_NAME") ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuraTrackrTheme {
                BlockingScreen(
                    onClose = {
                        finish()
                    },
                    onNavigateToTask = {
                        // Launch the main app and tell it to navigate to our task screen,
                        // passing along the package name of the app that needs to be unblocked.
                        val intent = Intent(this, MainActivity::class.java).apply {
                            // *** THIS IS THE UPDATE ***
                            // We now create the full route with the package name argument
                            putExtra("NAVIGATE_TO", Screen.AuraTask.createRoute(blockedPackageName))
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(intent)
                        // Close the blocking screen itself
                        finish()
                    }
                )
            }
        }
    }
}
