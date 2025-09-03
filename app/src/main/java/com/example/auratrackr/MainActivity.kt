package com.example.auratrackr

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.core.navigation.NavGraph
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.onboarding.viewmodel.AuthNavigationTarget
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.features.settings.ui.ThemeSetting
import com.example.auratrackr.features.settings.viewmodel.ThemeViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    // ✅ ADDED: Get an instance of the new ThemeViewModel.
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // ✅ ADDED: Collect the theme setting from the ViewModel.
            val themeSetting by themeViewModel.themeSetting.collectAsStateWithLifecycle()
            val useDarkTheme = when (themeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
            }

            // ✅ ADDED: Pass the determined theme to the AuraTrackrTheme composable.
            AuraTrackrTheme(useDarkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val authState by authViewModel.authState.collectAsStateWithLifecycle()

                AuthNavigator(
                    navController = navController,
                    authState = authState,
                    onStateHandled = { authViewModel.resetState() }
                )

                var currentIntent by remember { mutableStateOf(intent) }
                IntentNavigator(navController = navController, intent = currentIntent)

                DisposableEffect(intent) {
                    currentIntent = intent
                    onDispose { }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavGraph(navController = navController)

                        AnimatedVisibility(
                            visible = authState is AuthState.Loading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
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
private fun AuthNavigator(
    navController: NavHostController,
    authState: AuthState,
    onStateHandled: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                authState.successMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
                when (authState.navigationTarget) {
                    AuthNavigationTarget.GoToDashboard -> {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    AuthNavigationTarget.GoToFitnessOnboarding -> {
                        navController.navigate(Screen.FitnessOnboarding.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    AuthNavigationTarget.GoToLogin -> {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }
                    null -> { /* No navigation needed */ }
                }
                onStateHandled()
            }
            is AuthState.Error -> {
                Toast.makeText(context, authState.message, Toast.LENGTH_LONG).show()
                onStateHandled()
            }
            else -> { /* Idle or Loading */ }
        }
    }
}

@Composable
private fun IntentNavigator(navController: NavHostController, intent: Intent?) {
    LaunchedEffect(intent) {
        intent?.getStringExtra("NAVIGATE_TO")?.let { route ->
            navController.navigate(route)
            intent.removeExtra("NAVIGATE_TO")
        }
    }
}