package com.example.auratrackr.core.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * A sealed class representing all unique screens in the application for type-safe navigation.
 *
 * Each object corresponds to a screen and holds its route string. Routes with arguments
 * include helper functions to construct the full route URL safely.
 */
sealed class Screen(val route: String) {

    // --- Core & Onboarding ---

    /** The initial splash screen shown on app launch. */
    object Splash : Screen("splash_screen")

    /** The main welcome screen with Login/Register options. */
    object Welcome : Screen("welcome_screen")

    /** The user login screen. */
    object Login : Screen("login_screen")

    /** The user registration screen. */
    object Register : Screen("register_screen")

    /** The screen for handling forgotten passwords. */
    object ForgotPassword : Screen("forgot_password_screen")

    /** A visual, introductory screen for the fitness features. */
    object FitnessOnboarding : Screen("fitness_onboarding_screen")

    /** The screen for collecting the user's initial height and weight. */
    object PersonalInfo : Screen("personal_info_screen")

    /** The screen for requesting necessary app permissions like Usage Stats and Accessibility. */
    object Permissions : Screen("permissions_screen")

    // --- Main App ---

    /** The main dashboard screen, acting as the app's home. */
    object Dashboard : Screen("dashboard_screen")

    /** The screen for managing app focus settings and time limits. */
    object FocusSettings : Screen("focus_settings_screen")

    /** The screen for selecting a "Vibe" which themes the app. */
    object Vibe : Screen("vibe_screen")

    /** The screen for completing a quick task to unblock an app. */
    object AuraTask : Screen("aura_task_screen/{packageName}") {
        private const val ARG_PACKAGE_NAME = "packageName"
        fun createRoute(packageName: String): String {
            val encodedPackageName = URLEncoder.encode(packageName, StandardCharsets.UTF_8.toString())
            return "aura_task_screen/$encodedPackageName"
        }
    }

    /** A celebratory screen shown after completing a workout. */
    object Success : Screen("success_screen")

    /** The screen for creating a new workout schedule or editing an existing one. */
    object ScheduleEditor : Screen("schedule_editor_screen") {
        const val ARG_SCHEDULE_ID = "scheduleId"
        val routeWithArgs = "$route?$ARG_SCHEDULE_ID={$ARG_SCHEDULE_ID}"

        /**
         * Creates a route to the schedule editor.
         * @param scheduleId The ID of the schedule to edit. If null, opens the editor for a new schedule.
         */
        fun createRoute(scheduleId: String? = null): String {
            return if (scheduleId != null) {
                "$route?$ARG_SCHEDULE_ID=$scheduleId"
            } else {
                route
            }
        }
    }

    /** The screen displayed during an active workout session. */
    object WorkoutInProgress : Screen("workout_in_progress/{scheduleId}/{workoutId}") {
        private const val ARG_SCHEDULE_ID = "scheduleId"
        private const val ARG_WORKOUT_ID = "workoutId"
        fun createRoute(scheduleId: String, workoutId: String): String =
            "workout_in_progress/$scheduleId/$workoutId"
    }

    // --- Social Features ---

    /** The screen for searching and adding new friends. */
    object FindFriends : Screen("find_friends_screen")

    /** The screen for managing the user's friends list. */
    object Friends : Screen("friends_screen")

    /** The leaderboard screen to compare points with friends. */
    object Leaderboard : Screen("leaderboard_screen")

    /** The screen listing all group challenges. */
    object Challenges : Screen("challenges_screen")

    /** The screen for creating a new group challenge. */
    object CreateChallenge : Screen("create_challenge_screen")

    /** The "Spotify Wrapped" style summary screen. */
    object Wrapped : Screen("wrapped_screen")
}