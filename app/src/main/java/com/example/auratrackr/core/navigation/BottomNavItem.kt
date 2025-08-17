package com.example.auratrackr.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.auratrackr.R

/**
 * Represents the items in the main bottom navigation bar.
 *
 * This sealed class defines the structure for each navigation item, including its
 * navigation route, display icon, and a localizable string resource for its label.
 *
 * @param route The navigation route string, sourced from the [Screen] sealed class for consistency.
 * @param icon The [ImageVector] to be displayed for this item.
 * @param labelResId The string resource ID for the item's label, allowing for localization.
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int
) {
    /** The Dashboard tab, the main landing screen of the app. */
    object Dashboard : BottomNavItem(
        route = "dashboard_screen", // Assuming Screen.Dashboard.route
        icon = Icons.Default.Dashboard,
        labelResId = R.string.bottom_nav_dashboard
    )

    /** The Schedule tab for viewing and managing workout plans. */
    object Schedule : BottomNavItem(
        route = "schedule_screen", // Assuming Screen.Schedule.route
        icon = Icons.Default.CalendarToday,
        labelResId = R.string.bottom_nav_schedule
    )

    /** The Focus tab for managing app usage and blocking settings. */
    object Focus : BottomNavItem(
        route = "focus_screen", // Assuming Screen.Focus.route
        icon = Icons.Default.BarChart,
        labelResId = R.string.bottom_nav_focus
    )

    /** The Settings tab for user profile and app preferences. */
    object Settings : BottomNavItem(
        route = "settings_screen", // Assuming Screen.Settings.route
        icon = Icons.Default.Settings,
        labelResId = R.string.bottom_nav_settings
    )

    companion object {
        /** A list of all bottom navigation items in their display order. */
        val items = listOf(Dashboard, Schedule, Focus, Settings)
    }
}