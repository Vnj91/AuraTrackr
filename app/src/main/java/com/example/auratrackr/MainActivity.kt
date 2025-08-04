package com.example.auratrackr

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.core.navigation.NavGraph
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.onboarding.viewmodel.AuthNavigationState
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AuraTrackrTheme {
                val navController = rememberNavController()

                // Handle the initial intent when the app starts
                HandleIntent(navController = navController, intent = intent)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navigationState = authViewModel.navigationState.collectAsStateWithLifecycle().value

                    LaunchedEffect(navigationState) {
                        when (navigationState) {
                            AuthNavigationState.Unauthenticated -> {
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            AuthNavigationState.GoToFitnessOnboarding -> {
                                navController.navigate(Screen.FitnessOnboarding.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            AuthNavigationState.GoToDashboard -> {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            null -> {}
                        }
                    }

                    NavGraph(navController = navController)
                }
            }
        }
    }

    // *** THIS IS THE FIX ***
    // The 'intent' parameter is now correctly non-nullable (Intent)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // We update the activity's intent, which the Composable will react to.
        setIntent(intent)
    }
}

/**
 * A helper Composable to handle navigation from an Intent.
 * It's safer to handle the intent this way in a LaunchedEffect.
 */
@Composable
private fun HandleIntent(navController: NavHostController, intent: Intent?) {
    LaunchedEffect(intent) {
        intent?.getStringExtra("NAVIGATE_TO")?.let { route ->
            navController.navigate(route)
            // Clear the extra from the intent so it's not handled again on configuration change
            intent.removeExtra("NAVIGATE_TO")
        }
    }
}
