package com.example.auratrackr.features.focus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * A transparent activity that acts as an overlay to block other apps.
 * It hosts the BlockingScreen composable. The @AndroidEntryPoint annotation
 * is crucial for allowing Hilt to inject ViewModels into the composables
 * hosted by this Activity.
 */
@AndroidEntryPoint
class BlockingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuraTrackrTheme {
                // The BlockingScreen will automatically get its ViewModel via hiltViewModel()
                BlockingScreen(
                    onClose = {
                        // Finish this activity to close the overlay and return the user
                        // to their previous screen (likely the home screen).
                        finish()
                    }
                )
            }
        }
    }
}
