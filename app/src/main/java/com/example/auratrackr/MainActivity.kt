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
                WindowCompat.setDecorFitsSystemWindows(window, false)

                val navController = rememberNavController()

                HandleIntent(navController = navController, intent = intent)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navigationState = authViewModel.navigationState.collectAsStateWithLifecycle().value

                    // This LaunchedEffect is the brain of our navigation.
                    // It listens to the ViewModel and navigates when the state changes.
                    LaunchedEffect(navigationState) {
                        when (navigationState) {
                            AuthNavigationState.Unauthenticated -> {
                                // If user is unauthenticated (e.g., after logout),
                                // navigate to the Welcome screen and clear the entire back stack.
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(navController.graph.id) { inclusive = true }
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
private fun HandleIntent(navController: NavHostController, intent: Intent?) {
    LaunchedEffect(intent) {
        intent?.getStringExtra("NAVIGATE_TO")?.let { route ->
            navController.navigate(route)
            intent.removeExtra("NAVIGATE_TO")
        }
    }
}
