package com.example.auratrackr.core.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.auratrackr.features.challenges.ui.ChallengesListScreen
import com.example.auratrackr.features.challenges.ui.CreateChallengeScreen
import com.example.auratrackr.features.dashboard.ui.MainScreen
import com.example.auratrackr.features.focus.service.UsageTrackingService
import com.example.auratrackr.features.focus.ui.FocusSettingsScreen
import com.example.auratrackr.features.friends.ui.FindFriendsScreen
import com.example.auratrackr.features.friends.ui.FriendsScreen
import com.example.auratrackr.features.friends.ui.LeaderboardScreen
import com.example.auratrackr.features.onboarding.ui.*
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.features.permissions.ui.PermissionsScreen
import com.example.auratrackr.features.schedule.ui.ScheduleEditorScreen
import com.example.auratrackr.features.tasks.ui.AuraTaskScreen
import com.example.auratrackr.features.vibe.ui.VibeScreen
import com.example.auratrackr.features.vibe.viewmodel.VibeViewModel
import com.example.auratrackr.features.wrapped.ui.WrappedScreen
import com.example.auratrackr.features.workout.ui.SuccessScreen
import com.example.auratrackr.features.workout.ui.WorkoutInProgressScreen
import com.example.auratrackr.features.workout.viewmodel.WorkoutNavigationEvent
import com.example.auratrackr.features.workout.viewmodel.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

private const val AUTH_GRAPH_ROUTE = "auth_graph"
private const val MAIN_APP_GRAPH_ROUTE = "main_app_graph"

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            AnimatedSplashScreen(onTimeout = {})
        }

        authGraph(navController)
        mainAppGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Welcome.route,
        route = AUTH_GRAPH_ROUTE
    ) {
        composable(Screen.Welcome.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            WelcomeScreen(
                onLoginClicked = { navController.navigate(Screen.Login.route) },
                onRegisterClicked = { navController.navigate(Screen.Register.route) },
                onContinueAsGuestClicked = { authViewModel.signInAnonymously() },
                viewModel = authViewModel
            )
        }

        composable(Screen.Login.route) {
            val backStackEntry = remember(it) { navController.getBackStackEntry(Screen.Welcome.route) }
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            LoginScreen(
                onRegisterClicked = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClicked = { navController.navigate(Screen.ForgotPassword.route) },
                viewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            val backStackEntry = remember(it) { navController.getBackStackEntry(Screen.Welcome.route) }
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            RegisterScreen(
                onBackClicked = { navController.popBackStack() },
                onLoginClicked = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        composable(Screen.ForgotPassword.route) {
            val backStackEntry = remember(it) { navController.getBackStackEntry(Screen.Welcome.route) }
            val authViewModel: AuthViewModel = hiltViewModel(backStackEntry)
            ForgotPasswordScreen(
                onBackClicked = { navController.popBackStack() },
                onLoginClicked = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        composable(Screen.FitnessOnboarding.route) {
            FitnessOnboardingScreen(onLetsStartClicked = { navController.navigate(Screen.PersonalInfo.route) })
        }

        composable(Screen.PersonalInfo.route) {
            val authViewModel: AuthViewModel = hiltViewModel()
            PersonalInfoScreen(
                onFinished = { weight, height ->
                    // ✅ FIX: Navigate to the new Permissions screen after onboarding is complete.
                    authViewModel.completeOnboarding(weight, height)
                    navController.navigate(Screen.Permissions.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ✅ FIX: Added the PermissionsScreen to the authentication/onboarding graph.
        composable(Screen.Permissions.route) {
            val context = LocalContext.current
            PermissionsScreen(
                onContinue = {
                    // Start the tracking service and navigate to the main app dashboard.
                    val serviceIntent = Intent(context, UsageTrackingService::class.java).apply { action = UsageTrackingService.ACTION_START_SERVICE }
                    context.startService(serviceIntent)
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                },
                onGrantUsageAccess = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                onGrantAccessibility = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
            )
        }
    }
}

private fun NavGraphBuilder.mainAppGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Dashboard.route,
        route = MAIN_APP_GRAPH_ROUTE
    ) {
        composable(Screen.Dashboard.route) {
            MainScreen(mainNavController = navController)
        }

        composable(Screen.Vibe.route) {
            val vibeViewModel: VibeViewModel = hiltViewModel()
            val uiState by vibeViewModel.uiState.collectAsStateWithLifecycle()
            VibeScreen(
                vibes = uiState.vibes,
                selectedVibeId = uiState.selectedVibe?.id,
                onVibeSelected = { vibeId ->
                    vibeViewModel.onVibeSelected(vibeId)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FocusSettings.route) {
            FocusSettingsScreen(onBackClicked = { navController.popBackStack() })
        }

        composable(
            route = Screen.ScheduleEditor.routeWithArgs,
            arguments = listOf(navArgument(Screen.ScheduleEditor.ARG_SCHEDULE_ID) { type = NavType.StringType; nullable = true })
        ) {
            ScheduleEditorScreen(onBackClicked = { navController.popBackStack() })
        }

        composable(
            route = Screen.AuraTask.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AuraTaskScreen(
                onBackClicked = { navController.popBackStack() },
                onTaskSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.WorkoutInProgress.route,
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.StringType },
                navArgument("workoutId") { type = NavType.StringType }
            )
        ) {
            val viewModel: WorkoutViewModel = hiltViewModel()
            LaunchedEffect(viewModel) {
                viewModel.navigationEvent.collectLatest { event ->
                    when (event) {
                        is WorkoutNavigationEvent.NavigateToSuccess -> navController.navigate(Screen.Success.route)
                        is WorkoutNavigationEvent.FinishSession -> navController.popBackStack()
                    }
                }
            }
            WorkoutInProgressScreen(viewModel = viewModel, onBackClicked = { navController.popBackStack() })
        }

        composable(Screen.Success.route) {
            val workoutRoute = Screen.WorkoutInProgress.route
            val backStackEntry = remember(it) { navController.getBackStackEntry(workoutRoute) }
            val viewModel: WorkoutViewModel = hiltViewModel(backStackEntry)
            SuccessScreen(onContinue = {
                viewModel.onContinueToNextWorkout()
                navController.popBackStack()
            })
        }

        composable(Screen.FindFriends.route) {
            FindFriendsScreen(onBackClicked = { navController.popBackStack() })
        }

        composable(Screen.Friends.route) {
            FriendsScreen(
                onBackClicked = { navController.popBackStack() },
                onFindFriendsClicked = { navController.navigate(Screen.FindFriends.route) },
                onLeaderboardClicked = { navController.navigate(Screen.Leaderboard.route) },
                onChallengesClicked = { navController.navigate(Screen.Challenges.route) }
            )
        }

        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(
                onBackClicked = { navController.popBackStack() },
                currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            )
        }

        composable(Screen.Challenges.route) {
            ChallengesListScreen(
                onBackClicked = { navController.popBackStack() },
                onCreateChallengeClicked = { navController.navigate(Screen.CreateChallenge.route) }
            )
        }

        composable(Screen.CreateChallenge.route) {
            CreateChallengeScreen(onBackClicked = { navController.popBackStack() })
        }

        composable(Screen.Wrapped.route) {
            WrappedScreen(onBackClicked = { navController.popBackStack() })
        }
    }
}