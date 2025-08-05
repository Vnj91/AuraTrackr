package com.example.auratrackr.features.wrapped.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.features.wrapped.viewmodel.WrappedViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun WrappedScreen(
    onBackClicked: () -> Unit,
    viewModel: WrappedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState()

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C1B2E), Color(0xFF4A148C))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Your Aura Wrapped", color = Color.White) },
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
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.summary != null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HorizontalPager(
                        count = 5, // We have 5 summary pages
                        state = pagerState,
                        modifier = Modifier.weight(1f)
                    ) { page ->
                        val summary = uiState.summary!!
                        when (page) {
                            0 -> SummaryStatCard(icon = Icons.Default.Timer, title = "Minutes Focused", value = summary.totalMinutesFocused.toString(), unit = "mins")
                            1 -> SummaryStatCard(icon = Icons.Default.FitnessCenter, title = "Workouts Completed", value = summary.totalWorkoutsCompleted.toString(), unit = "sessions")
                            2 -> SummaryStatCard(icon = Icons.Default.Star, title = "Aura Points Earned", value = summary.auraPointsEarned.toString(), unit = "points")
                            3 -> SummaryStatCard(icon = Icons.Default.Block, title = "Most Blocked App", value = summary.mostBlockedApp, unit = "")
                            4 -> SummaryStatCard(icon = Icons.Default.Favorite, title = "Favorite Workout", value = summary.favoriteWorkout, unit = "")
                        }
                    }
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier.padding(16.dp),
                        activeColor = Color.White,
                        inactiveColor = Color.White.copy(alpha = 0.5f)
                    )
                }
            } else {
                Text(
                    text = uiState.error ?: "Summary not available.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
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
                    .clip(CircleShape) // <-- This now works
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title.uppercase(),
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WrappedScreenPreview() {
    // WrappedScreen(onBackClicked = {})
}
