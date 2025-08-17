package com.example.auratrackr.features.wrapped.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WrappedScreen(
    onBackClicked: () -> Unit,
    viewModel: WrappedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1B2E), Color(0xFF4A148C)) // Keep custom gradient for this special screen
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Your Aura Wrapped", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
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
                    val pagerState = rememberPagerState { summaryStats.size }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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

                        // Using dots indicator for the pager
                        Row(
                            Modifier
                                .height(50.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(summaryStats.size) { iteration ->
                                val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(12.dp)
                                )
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
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
                            text = uiState.error ?: "Summary not available.",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.loadUserSummary() }) {
                            Text("Retry")
                        }
                    }
                }
            }
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
            .padding(32.dp),
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
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title.uppercase(),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
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

@Preview(showBackground = true)
@Composable
fun WrappedScreenSuccessPreview() {
    AuraTrackrTheme(darkTheme = true) {
        val summary = UserSummary(
            year = "2025",
            totalMinutesFocused = 12345,
            totalWorkoutsCompleted = 150,
            auraPointsEarned = 7500,
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
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(summaryStats.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) Color.White else Color.White.copy(alpha = 0.5f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
