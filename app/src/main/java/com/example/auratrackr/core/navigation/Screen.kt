package com.example.auratrackr.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Welcome : Screen("welcome_screen")
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object ForgotPassword : Screen("forgot_password_screen")
    object FitnessOnboarding : Screen("fitness_onboarding_screen")
    object PersonalInfo : Screen("personal_info_screen")
    object Permissions : Screen("permissions_screen")
    object Dashboard : Screen("dashboard_screen")
    object FocusSettings : Screen("focus_settings_screen")
    object Vibe : Screen("vibe_screen") // <-- ADDED THIS NEW ROUTE
    object WorkoutInProgress : Screen("workout_in_progress/{workoutId}") {
        fun createRoute(workoutId: String): String {
            return "workout_in_progress/$workoutId"
        }
    }
    object Success : Screen("success_screen")
}
