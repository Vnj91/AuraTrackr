package com.example.auratrackr.core.navigation

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
import com.example.auratrackr.features.permissions.viewmodel.PermissionsViewModel
import com.example.auratrackr.features.schedule.ui.ScheduleEditorScreen
import com.example.auratrackr.features.tasks.ui.AuraTaskScreen
import com.example.auratrackr.features.tasks.viewmodel.TaskResult
import com.example.auratrackr.features.tasks.viewmodel.TaskViewModel
import com.example.auratrackr.features.vibe.ui.VibeScreen
import com.example.auratrackr.features.vibe.viewmodel.VibeViewModel
import com.example.auratrackr.features.workout.ui.SuccessScreen
import com.example.auratrackr.features.workout.ui.WorkoutInProgressScreen
import com.example.auratrackr.features.workout.viewmodel.WorkoutNavigationEvent
import com.example.auratrackr.features.workout.viewmodel.WorkoutViewModel
import com.example.auratrackr.features.wrapped.ui.WrappedScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ... (All previous routes remain the same)
        composable(Screen.Splash.route) { AnimatedSplashScreen(onTimeout = {}) }
        composable(Screen.Welcome.route) { WelcomeScreen(onLoginClicked = { navController.navigate(Screen.Login.route) }, onRegisterClicked = { navController.navigate(Screen.Register.route) }, onContinueAsGuestClicked = { authViewModel.signInAnonymously() }, viewModel = authViewModel) }
        composable(Screen.Login.route) { LoginScreen(onBackClicked = { navController.popBackStack() }, onLoginClicked = { email, password -> authViewModel.login(email, password) }, onRegisterClicked = { navController.navigate(Screen.Register.route) { popUpTo(Screen.Welcome.route) } }, onForgotPasswordClicked = { navController.navigate(Screen.ForgotPassword.route) }) }
        composable(Screen.Register.route) { RegisterScreen(onBackClicked = { navController.popBackStack() }, onRegisterClicked = { username, email, password, _ -> authViewModel.register(email, username, password) }, onLoginClicked = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Welcome.route) } }) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(onBackClicked = { navController.popBackStack() }, onSendCodeClicked = { /* TODO */ }, onLoginClicked = { navController.popBackStack() }) }
        composable(Screen.FitnessOnboarding.route) { FitnessOnboardingScreen(onLetsStartClicked = { navController.navigate(Screen.PersonalInfo.route) }) }
        composable(Screen.PersonalInfo.route) { PersonalInfoScreen(onFinished = { weight, height -> authViewModel.completeOnboarding(weight, height); navController.navigate(Screen.Permissions.route) { popUpTo(Screen.Splash.route) } }, onBack = { navController.popBackStack() }) }
        composable(Screen.Permissions.route) {
            val permissionsViewModel: PermissionsViewModel = hiltViewModel()
            val state by permissionsViewModel.uiState.collectAsStateWithLifecycle()
            val context = LocalContext.current
            PermissionsScreen(
                usageAccessGranted = state.isUsageAccessGranted,
                accessibilityGranted = state.isAccessibilityServiceEnabled,
                onGrantUsageAccess = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
                onGrantAccessibility = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                onContinue = {
                    val serviceIntent = Intent(context, UsageTrackingService::class.java).apply { action = UsageTrackingService.ACTION_START_SERVICE }
                    context.startService(serviceIntent)
                    navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                },
                onRefresh = { permissionsViewModel.checkPermissions() }
            )
        }
        composable(Screen.Dashboard.route) { MainScreen(mainNavController = navController) }
        composable(Screen.Vibe.route) {
            val backStackEntry = remember(it) { navController.getBackStackEntry(Screen.Dashboard.route) }
            val vibeViewModel: VibeViewModel = hiltViewModel(backStackEntry)
            val vibeUiState by vibeViewModel.uiState.collectAsStateWithLifecycle()
            VibeScreen(
                vibes = vibeUiState.vibes,
                selectedVibeId = vibeUiState.selectedVibe?.id,
                onVibeSelected = { vibeId -> vibeViewModel.onVibeSelected(vibeId); navController.popBackStack() }
            )
        }
        composable(Screen.FocusSettings.route) { FocusSettingsScreen(onBackClicked = { navController.popBackStack() }) }
        composable(
            route = Screen.AuraTask.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            val viewModel: TaskViewModel = hiltViewModel()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                viewModel.taskResultEvent.collectLatest { result ->
                    val message = if (result == TaskResult.Success) "Correct! 5 minutes added." else "Incorrect. Try again!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (result == TaskResult.Success) {
                        navController.popBackStack()
                    }
                }
            }
            AuraTaskScreen(
                question = viewModel.question,
                onAnswerSubmitted = { answer -> viewModel.onAnswerSubmitted(answer) },
                onBackClicked = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.WorkoutInProgress.route,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType }, navArgument("workoutId") { type = NavType.StringType })
        ) {
            val viewModel: WorkoutViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                viewModel.navigationEvent.collectLatest { event ->
                    when (event) {
                        is WorkoutNavigationEvent.NavigateToSuccess -> navController.navigate(Screen.Success.route)
                        is WorkoutNavigationEvent.FinishSession -> navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                    }
                }
            }
            WorkoutInProgressScreen(viewModel = viewModel, onBackClicked = { navController.popBackStack() })
        }
        composable(Screen.Success.route) {
            val backStackEntry = remember(it) { navController.getBackStackEntry(Screen.WorkoutInProgress.route) }
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
            CreateChallengeScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
        composable(Screen.Wrapped.route) {
            WrappedScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }

        // --- ADDED THIS NEW DESTINATION ---
        composable(
            route = Screen.ScheduleEditor.route,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType; nullable = true })
        ) {
            ScheduleEditorScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
