package com.example.auratrackr.features.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.dashboard.viewmodel.DashboardUiState
import com.example.auratrackr.features.dashboard.viewmodel.DashboardViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme

// Common spacing and sizing constants used in this file
private val HORIZONTAL_PADDING = 24.dp
private val LARGE_VERTICAL_SPACER = 32.dp
private val AVATAR_SIZE = 48.dp
private val contentVerticalPadding = 16.dp
private val mediumSpacer = 16.dp
private val SPACER_12 = 12.dp

@Composable
fun DashboardScreenContent(
    viewModel: DashboardViewModel = hiltViewModel(),
    mainNavController: NavController,
    onVibeClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Delegate the heavy LazyColumn content to a small helper composable to keep
    // this top-level function under the detekt LongMethod threshold.
    DashboardContentList(
        uiState = uiState,
        viewModel = viewModel,
        mainNavController = mainNavController,
        onVibeClicked = onVibeClicked
    )
}

@Composable
private fun DashboardContentList(
    uiState: com.example.auratrackr.features.dashboard.viewmodel.DashboardUiState,
    viewModel: DashboardViewModel,
    mainNavController: NavController,
    onVibeClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        contentPadding = PaddingValues(vertical = contentVerticalPadding)
    ) {
        item {
            DashboardHeader(
                uiState = uiState,
                modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
            )
        }

        item {
            Spacer(modifier = Modifier.height(LARGE_VERTICAL_SPACER))
            // ✅ UPDATE: Replaced the old screen time card with our new, more insightful points card.
            PointsByVibeCard(
                isLoading = uiState.isLoading,
                pointsByVibe = uiState.pointsByVibe,
                modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
            )
        }

        item {
            Spacer(modifier = Modifier.height(LARGE_VERTICAL_SPACER))
            ScheduleHeader(
                onVibeClicked = onVibeClicked,
                modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
            )
        }

        item {
            Spacer(modifier = Modifier.height(mediumSpacer))
            if (uiState.isLoading) {
                Text(
                    "Loading schedule...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                )
            } else {
                uiState.todaysSchedule?.let { schedule ->
                    ScheduleTimeline(
                        schedule = schedule,
                        onStartWorkout = { workout ->
                            viewModel.startWorkout(schedule.id, workout.id)
                            mainNavController.navigate(
                                Screen.WorkoutInProgress.createRoute(
                                    scheduleId = schedule.id,
                                    workoutId = workout.id
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                    )
                } ?: EmptySchedulePlaceholder(onAddScheduleClicked = {
                    mainNavController.navigate(Screen.ScheduleEditor.createRoute())
                })
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SPACER_12)) {
            AvatarWithInitials(
                imageUrl = uiState.profilePictureUrl,
                name = uiState.username,
                modifier = Modifier.size(AVATAR_SIZE)
            )
            Column {
                Text(
                    "Hi, ${uiState.username}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        AuraPointsChip(points = uiState.auraPoints)
    }
}

@Composable
private fun ScheduleHeader(onVibeClicked: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Your Schedule",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text("Today's Activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = onVibeClicked) {
            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Vibe")
        }
    }
}

// Moved several helper composables (WorkoutItem, WorkoutDetails, WorkoutTimelineDot,
// WorkoutProgressBar, ScheduleTimeline, EmptySchedulePlaceholder, PointsByVibeCard,
// PointsByVibeBarChart, AuraPointsChip, AvatarWithInitials) into
// `DashboardParts.kt` to reduce function count and LongMethod weight in this file.

@Preview(showBackground = true)
@Composable
fun DashboardScreenContentPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        DashboardScreenContent(mainNavController = rememberNavController(), onVibeClicked = {})
    }
}
