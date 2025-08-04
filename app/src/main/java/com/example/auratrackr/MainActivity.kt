package com.example.auratrackr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        super.onCreate(savedInstanceState)
        setContent {
            AuraTrackrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navigationState = authViewModel.navigationState.collectAsStateWithLifecycle().value

                    // This LaunchedEffect is the brain of our navigation.
                    // It listens to the ViewModel and navigates when the state changes.
                    LaunchedEffect(navigationState) {
                        when (navigationState) {
                            AuthNavigationState.Unauthenticated -> {
                                // After splash, if not logged in, go to Welcome
                                navController.navigate(Screen.Welcome.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            AuthNavigationState.GoToFitnessOnboarding -> {
                                // New user, go to fitness onboarding
                                navController.navigate(Screen.FitnessOnboarding.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            AuthNavigationState.GoToDashboard -> {
                                // Existing user, go straight to dashboard
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                            null -> {
                                // Initial state, do nothing, wait for ViewModel to decide.
                            }
                        }
                    }

                    // The NavGraph contains all the possible screens.
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
