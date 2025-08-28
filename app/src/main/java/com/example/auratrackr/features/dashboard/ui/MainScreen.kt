package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.core.navigation.BottomNavItem
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.focus.ui.FocusSettingsScreen
import com.example.auratrackr.features.schedule.ui.ScheduleScreen
import com.example.auratrackr.features.settings.ui.SettingsScreen
import com.example.auratrackr.features.vibe.viewmodel.VibeViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainScreen(mainNavController: NavHostController) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val mainGraphEntry = remember(navBackStackEntry) {
        mainNavController.getBackStackEntry(Screen.Dashboard.route)
    }
    val vibeViewModel: VibeViewModel = hiltViewModel(mainGraphEntry)
    val vibeUiState by vibeViewModel.uiState.collectAsStateWithLifecycle()

    val selectedColor = vibeUiState.selectedVibe?.backgroundColor ?: MaterialTheme.colorScheme.background
    val animatedBgColor by animateColorAsState(
        targetValue = selectedColor,
        animationSpec = tween(500),
        label = "BackgroundColor"
    )

    val pagerState = rememberPagerState(initialPage = 0) { BottomNavItem.items.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = animatedBgColor,
        bottomBar = {
            AppBottomNavigation(
                selectedIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(paddingValues),
            verticalAlignment = Alignment.Top,
        ) { page ->
            val pageOffset = pagerState.getOffsetFractionForPage(page)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val scale = 1f - (abs(pageOffset) * 0.07f)
                        val alpha = 1f - (abs(pageOffset) * 0.15f)
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        translationX = pageOffset * (size.width / 2)
                    }
            ) {
                when (page) {
                    0 -> DashboardScreenContent(
                        mainNavController = mainNavController,
                        onVibeClicked = { mainNavController.navigate(Screen.Vibe.route) }
                    )
                    1 -> ScheduleScreen(navController = mainNavController)
                    2 -> FocusSettingsScreen(onBackClicked = {})
                    3 -> SettingsScreen(navController = mainNavController)
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigation(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // ✅ FIX: Replaced the semi-transparent Surface with a standard NavigationBar.
    // This provides a solid background, better contrast, and a subtle shadow (tonalElevation).
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 3.dp // Adds a subtle shadow to lift the bar off the content.
    ) {
        BottomNavItem.items.forEachIndexed { index, screen ->
            val label = stringResource(id = screen.labelResId)
            val isSelected = selectedIndex == index
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = label) },
                label = { Text(label) },
                selected = isSelected,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTabSelected(index)
                },
                // ✅ FIX: Updated colors for better contrast and a clearer active state indicator.
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
private fun androidx.compose.foundation.pager.PagerState.getOffsetFractionForPage(page: Int): Float {
    val currentPageOffset = currentPage - page
    val pageOffset = currentPageOffset + currentPageOffsetFraction
    return pageOffset.coerceIn(-1f, 1f)
}

@Preview
@Composable
fun AppBottomNavigationPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        var selectedIndex by remember { mutableIntStateOf(0) }
        Scaffold(
            bottomBar = {
                AppBottomNavigation(
                    selectedIndex = selectedIndex,
                    onTabSelected = { selectedIndex = it }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding))
        }
    }
}
