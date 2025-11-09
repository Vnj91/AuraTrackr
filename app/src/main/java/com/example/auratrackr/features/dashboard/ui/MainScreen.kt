package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.auratrackr.core.navigation.BottomNavItem
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.focus.ui.FocusSettingsScreen
import com.example.auratrackr.features.live.ui.LiveActivityScreen
import com.example.auratrackr.features.schedule.ui.ScheduleScreen
import com.example.auratrackr.features.settings.ui.SettingsScreen
import com.example.auratrackr.features.vibe.viewmodel.VibeViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val PAGE_ANIM_DURATION_MS = 500
private const val PAGE_SCALE_FACTOR = 0.07f
private const val PAGE_ALPHA_FACTOR = 0.15f
private val NAV_BAR_ELEVATION = 3.dp
private const val PAGE_TRANSLATION_DIVISOR = 2f

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
        animationSpec = tween(PAGE_ANIM_DURATION_MS),
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
            verticalAlignment = Alignment.Top
        ) { page ->
            val pageOffset = pagerState.getOffsetFractionForPage(page)
            AnimatedPageContent(
                page = page,
                pageOffset = pageOffset,
                mainNavController = mainNavController
            )
        }
    }
}

@Composable
private fun AnimatedPageContent(
    page: Int,
    pageOffset: Float,
    mainNavController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val scale = 1f - (abs(pageOffset) * PAGE_SCALE_FACTOR)
                val alpha = 1f - (abs(pageOffset) * PAGE_ALPHA_FACTOR)
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                translationX = pageOffset * (size.width / PAGE_TRANSLATION_DIVISOR)
            }
    ) {
        // ✅ UPDATED: Added the new LiveActivityScreen to the pager.
        when (page) {
            0 -> DashboardScreenContent(
                mainNavController = mainNavController,
                onVibeClicked = { mainNavController.navigate(Screen.Vibe.route) }
            )
            1 -> ScheduleScreen(navController = mainNavController)
            2 -> LiveActivityScreen()
            3 -> FocusSettingsScreen(onBackClicked = {})
            4 -> SettingsScreen(navController = mainNavController)
        }
    }
}

@Composable
fun AppBottomNavigation(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = NAV_BAR_ELEVATION
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
