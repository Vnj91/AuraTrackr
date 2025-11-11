package com.example.auratrackr.features.wrapped.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.features.wrapped.viewmodel.WrappedViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.Dimensions

// File-level layout constants (lowerCamelCase to satisfy naming rules)
private val wrappedPadding = 32.dp
private val summaryCardPadding = 32.dp
private val summaryIconSize = 64.dp
private val summaryIconInnerPadding = 12.dp
private val dotSize = 12.dp
private val dotPadding = 4.dp
private val pagerRowHeight = 50.dp

// Minimum touch target for small icons/buttons (accessibility)
private val ICON_MIN_TOUCH = 48.dp

// Preview/demo constants
private const val PREVIEW_YEAR = "2025"
private const val PREVIEW_TOTAL_MINUTES = 12345L
private const val PREVIEW_TOTAL_WORKOUTS = 150
private const val PREVIEW_AURA_POINTS = 7500

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WrappedScreen(
    onBackClicked: () -> Unit,
    viewModel: WrappedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1B2E), Color(0xFF4A148C))
    )

    // Use file-level constants directly (lowerCamelCase names)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Your Aura Wrapped", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.sizeIn(minWidth = ICON_MIN_TOUCH, minHeight = ICON_MIN_TOUCH)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        WrappedContent(
            uiState = uiState,
            gradient = gradient,
            paddingValues = paddingValues,
            onRetry = { viewModel.loadUserSummary() }
        )
    }
}

@Composable
private fun WrappedContent(
    uiState: WrappedUiState,
    gradient: Brush,
    paddingValues: PaddingValues,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(paddingValues)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            uiState.summary != null -> {
                val summary = uiState.summary!!
                val summaryStats = remember(summary) { createSummaryStats(summary) }

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    SummaryPager(summaryStats = summaryStats, modifier = Modifier.weight(1f))
                }
            }
            else -> {
                WrappedErrorContent(
                    errorMessage = uiState.error ?: "Summary not available.",
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun WrappedErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = errorMessage,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private data class SummaryStat(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val unit: String
)

private fun createSummaryStats(summary: UserSummary): List<SummaryStat> {
    return listOf(
        SummaryStat(
            icon = Icons.Default.Timer,
            title = "Minutes Focused",
            value = summary.totalMinutesFocused.toString(),
            unit = "mins"
        ),
        SummaryStat(
            icon = Icons.Default.FitnessCenter,
            title = "Workouts Completed",
            value = summary.totalWorkoutsCompleted.toString(),
            unit = "sessions"
        ),
        SummaryStat(
            icon = Icons.Default.Star,
            title = "Aura Points Earned",
            value = summary.auraPointsEarned.toString(),
            unit = "points"
        ),
        SummaryStat(
            icon = Icons.Default.Block,
            title = "Most Blocked App",
            value = summary.mostBlockedApp ?: "N/A",
            unit = ""
        ),
        SummaryStat(
            icon = Icons.Default.Favorite,
            title = "Favorite Workout",
            value = summary.favoriteWorkout ?: "N/A",
            unit = ""
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SummaryPager(summaryStats: List<SummaryStat>, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState { summaryStats.size }

    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val stat = summaryStats[page]
            SummaryStatCard(
                icon = stat.icon,
                title = stat.title,
                value = stat.value,
                unit = stat.unit
            )
        }

        Row(
            Modifier
                .height(pagerRowHeight)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(summaryStats.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.5f)
                }
                Box(
                    modifier = Modifier
                        .padding(dotPadding)
                        .clip(CircleShape)
                        .background(color)
                        .size(dotSize)
                )
            }
        }
    }
}

@Composable
fun SummaryStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    unit: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(summaryCardPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier
                    .size(summaryIconSize)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(summaryIconInnerPadding)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimensions.Small))
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (unit.isNotBlank()) {
                Text(
                    text = unit,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun WrappedScreenSuccessPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        val summary = UserSummary(
            year = PREVIEW_YEAR,
            totalMinutesFocused = PREVIEW_TOTAL_MINUTES,
            totalWorkoutsCompleted = PREVIEW_TOTAL_WORKOUTS,
            auraPointsEarned = PREVIEW_AURA_POINTS,
            mostBlockedApp = "Social Media App",
            favoriteWorkout = "Morning Run"
        )
        val summaryStats = createSummaryStats(summary)
        val pagerState = rememberPagerState { summaryStats.size }
        val gradient = Brush.verticalGradient(colors = listOf(Color(0xFF1C1B2E), Color(0xFF4A148C)))

        Box(modifier = Modifier.background(gradient)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    val stat = summaryStats[page]
                    SummaryStatCard(icon = stat.icon, title = stat.title, value = stat.value, unit = stat.unit)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(wrappedPadding),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(summaryStats.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) {
                            Color.White
                        } else {
                            Color.White.copy(
                                alpha = 0.5f
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(dotPadding)
                                .clip(CircleShape)
                                .background(color)
                                .size(dotSize)
                        )
                    }
                }
            }
        }
    }
}
