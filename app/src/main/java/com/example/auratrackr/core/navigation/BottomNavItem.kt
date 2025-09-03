package com.example.auratrackr.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
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
 */
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int
) {
    /** The Dashboard tab, the main landing screen of the app. */
    object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        icon = Icons.Default.Dashboard,
        labelResId = R.string.bottom_nav_dashboard
    )

    /** The Schedule tab for viewing and managing workout plans. */
    object Schedule : BottomNavItem(
        route = "schedule_screen", // Placeholder, assuming Screen.Schedule.route
        icon = Icons.Default.CalendarToday,
        labelResId = R.string.bottom_nav_schedule
    )

    /** The Live Activity tab for unstructured, sensor-based tracking. */
    // ✅ FIX: Use the auto-mirrored version of the icon for better RTL support.
    object Live : BottomNavItem(
        route = Screen.LiveActivity.route,
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        labelResId = R.string.bottom_nav_live
    )

    /** The Focus tab for managing app usage and blocking settings. */
    object Focus : BottomNavItem(
        route = "focus_screen", // Placeholder, assuming Screen.Focus.route
        icon = Icons.Default.BarChart,
        labelResId = R.string.bottom_nav_focus
    )

    /** The Settings tab for user profile and app preferences. */
    object Settings : BottomNavItem(
        route = "settings_screen", // Placeholder, assuming Screen.Settings.route
        icon = Icons.Default.Settings,
        labelResId = R.string.bottom_nav_settings
    )

    companion object {
        /** A list of all bottom navigation items in their display order. */
        val items = listOf(Dashboard, Schedule, Live, Focus, Settings)
    }
}
