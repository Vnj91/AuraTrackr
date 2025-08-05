package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.core.navigation.BottomNavItem
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.focus.ui.FocusSettingsScreen
import com.example.auratrackr.features.schedule.ui.ScheduleScreen
import com.example.auratrackr.features.settings.ui.SettingsScreen
import com.example.auratrackr.features.vibe.viewmodel.VibeViewModel

@Composable
fun MainScreen(mainNavController: NavHostController) {
    val bottomNavController = rememberNavController()
    val vibeViewModel: VibeViewModel = hiltViewModel()
    val vibeUiState by vibeViewModel.uiState.collectAsStateWithLifecycle()

    val animatedBackgroundColor by animateColorAsState(
        targetValue = vibeUiState.selectedVibe?.backgroundColor ?: Color(0xFF1C1B2E),
        label = "BackgroundColorAnimation"
    )

    Scaffold(
        containerColor = animatedBackgroundColor,
        bottomBar = {
            AppBottomNavigation(navController = bottomNavController)
        }
    ) { paddingValues ->
        DashboardNavHost(
            bottomNavController = bottomNavController,
            mainNavController = mainNavController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun AppBottomNavigation(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Schedule,
        BottomNavItem.Focus,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.2f),
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .height(70.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(28.dp)
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.White.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
fun DashboardNavHost(
    bottomNavController: NavHostController,
    mainNavController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = bottomNavController,
        startDestination = BottomNavItem.Dashboard.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreenContent(
                mainNavController = mainNavController,
                onVibeClicked = {
                    mainNavController.navigate(Screen.Vibe.route)
                }
            )
        }
        composable(BottomNavItem.Schedule.route) {
            ScheduleScreen(navController = mainNavController)
        }
        composable(BottomNavItem.Focus.route) {
            FocusSettingsScreen(
                onBackClicked = { /* The back button in the top bar will handle this */ }
            )
        }
        composable(BottomNavItem.Settings.route) {
            // *** THIS IS THE UPDATE ***
            // Pass the main NavController down to the SettingsScreen
            SettingsScreen(navController = mainNavController)
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
