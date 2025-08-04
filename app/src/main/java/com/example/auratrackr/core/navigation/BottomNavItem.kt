package com.example.auratrackr.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents the items in the main bottom navigation bar, updated to the final structure.
 */
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    // 1. Dashboard Tab
    object Dashboard : BottomNavItem("dashboard_screen", Icons.Default.Dashboard, "Dashboard")
    // 2. Schedule Tab
    object Schedule : BottomNavItem("schedule_screen", Icons.Default.CalendarToday, "Schedule")
    // 3. Focus (App Block) Tab
    object Focus : BottomNavItem("focus_screen", Icons.Default.BarChart, "Focus")
    // 4. Settings Tab
    object Settings : BottomNavItem("settings_screen", Icons.Default.Settings, "Settings")
}
