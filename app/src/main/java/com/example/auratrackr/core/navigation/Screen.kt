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
    object Vibe : Screen("vibe_screen")
    object AuraTask : Screen("aura_task_screen/{packageName}") {
        fun createRoute(packageName: String): String = "aura_task_screen/$packageName"
    }
    object Success : Screen("success_screen")
    object ScheduleEditor : Screen("schedule_editor_screen?scheduleId={scheduleId}") {
        fun createRoute(scheduleId: String): String = "schedule_editor_screen?scheduleId=$scheduleId"
        fun newScheduleRoute(): String = "schedule_editor_screen"
    }
    object WorkoutInProgress : Screen("workout_in_progress/{scheduleId}/{workoutId}") {
        fun createRoute(scheduleId: String, workoutId: String): String = "workout_in_progress/$scheduleId/$workoutId"
    }
    object FindFriends : Screen("find_friends_screen")
    object Friends : Screen("friends_screen")
    object Leaderboard : Screen("leaderboard_screen")
    object Challenges : Screen("challenges_screen")
    object CreateChallenge : Screen("create_challenge_screen")

    // --- ADDED THIS NEW ROUTE ---
    object Wrapped : Screen("wrapped_screen")
}
