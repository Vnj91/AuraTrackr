package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.dashboard.viewmodel.DashboardUiState
import com.example.auratrackr.features.dashboard.viewmodel.DashboardViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.SuccessGreen

@Composable
fun DashboardScreenContent(
    viewModel: DashboardViewModel = hiltViewModel(),
    mainNavController: NavController,
    onVibeClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            DashboardHeader(
                uiState = uiState,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            // ✅ UPDATE: Replaced the old screen time card with our new, more insightful points card.
            PointsByVibeCard(
                isLoading = uiState.isLoading,
                pointsByVibe = uiState.pointsByVibe,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            ScheduleHeader(
                onVibeClicked = onVibeClicked,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.isLoading) {
                Text(
                    "Loading schedule...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
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
                        modifier = Modifier.padding(horizontal = 24.dp)
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AvatarWithInitials(
                imageUrl = uiState.profilePictureUrl,
                name = uiState.username,
                modifier = Modifier.size(48.dp)
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

// ✅ NEW: A dedicated card for our "Points by Vibe" chart.
@Composable
private fun PointsByVibeCard(
    isLoading: Boolean,
    pointsByVibe: Map<Vibe, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Aura Points by Vibe",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PointsByVibeBarChart(pointsByVibe = pointsByVibe)
            }
        }
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


@Composable
fun EmptySchedulePlaceholder(onAddScheduleClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painterResource(id = R.drawable.ic_no_schedule),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )
        Text(
            "No schedule for today. Relax or add new activities!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onAddScheduleClicked,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Add Schedule")
        }
    }
}

@Composable
fun ScheduleTimeline(
    schedule: Schedule,
    onStartWorkout: (Workout) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        schedule.workouts.forEachIndexed { index, workout ->
            WorkoutItem(
                workout = workout,
                isFirstItem = index == 0,
                isLastItem = index == schedule.workouts.lastIndex,
                onStartClicked = { onStartWorkout(workout) }
            )
        }
    }
}

@Composable
fun WorkoutItem(workout: Workout, isFirstItem: Boolean, isLastItem: Boolean, onStartClicked: () -> Unit) {
    val activeColor = MaterialTheme.colorScheme.primary
    val completedColor = SuccessGreen
    val pendingColor = MaterialTheme.colorScheme.outline

    val timelineColor = when (workout.status) {
        WorkoutStatus.ACTIVE -> activeColor
        WorkoutStatus.COMPLETED -> completedColor
        WorkoutStatus.PENDING -> pendingColor
    }

    Row(modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        val isDashed = workout.status == WorkoutStatus.PENDING
        Canvas(modifier = Modifier.width(30.dp).fillMaxHeight()) {
            val circleRadius = 8.dp.toPx()
            val circleCenterY = size.height / 2
            if (!isFirstItem) {
                drawTimelineLine(timelineColor, Offset(center.x, 0f), Offset(center.x, circleCenterY - circleRadius), isDashed)
            }
            if (!isLastItem) {
                drawTimelineLine(timelineColor, Offset(center.x, circleCenterY + circleRadius), Offset(center.x, size.height), isDashed)
            }
            when (workout.status) {
                WorkoutStatus.COMPLETED -> drawCircle(color = completedColor, radius = circleRadius)
                WorkoutStatus.ACTIVE -> drawCircle(color = activeColor, radius = circleRadius)
                WorkoutStatus.PENDING -> drawCircle(color = pendingColor, radius = circleRadius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
            Text(workout.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(workout.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            WorkoutProgressBar(
                progress = if (workout.status == WorkoutStatus.COMPLETED) 1f else 0f,
                color = timelineColor
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (workout.status == WorkoutStatus.COMPLETED) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Completed", tint = completedColor, modifier = Modifier.size(32.dp))
        } else {
            Button(
                onClick = onStartClicked,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (workout.status == WorkoutStatus.ACTIVE) activeColor else MaterialTheme.colorScheme.surfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(if (workout.status == WorkoutStatus.ACTIVE) "Resume" else "Start", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkoutProgressBar(progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800), label = "ProgressBarAnimation")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(6.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
    }
}

private fun DrawScope.drawTimelineLine(color: Color, start: Offset, end: Offset, isDashed: Boolean) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2.dp.toPx(),
        pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
    )
}

// ✅ NEW: The new bar chart for displaying points per vibe.
@Composable
fun PointsByVibeBarChart(pointsByVibe: Map<Vibe, Int>) {
    val maxPoints = remember(pointsByVibe) { (pointsByVibe.values.maxOrNull() ?: 1).coerceAtLeast(1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        pointsByVibe.entries.forEach { (vibe, points) ->
            val barHeightFraction = (points.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f)
            val animatedBarHeightFraction by animateFloatAsState(targetValue = barHeightFraction, animationSpec = tween(1000), label = "BarHeightAnimation")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                if (points > 0) {
                    Text(
                        text = "$points pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = vibe.backgroundColor
                    )
                }
                Canvas(modifier = Modifier.height(100.dp).width(20.dp)) {
                    drawRoundRect(
                        color = vibe.backgroundColor,
                        topLeft = Offset(x = 0f, y = size.height * (1 - animatedBarHeightFraction)),
                        size = Size(width = size.width, height = size.height * animatedBarHeightFraction),
                        cornerRadius = CornerRadius(x = 8.dp.toPx(), y = 8.dp.toPx())
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = vibe.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Composable
fun AuraPointsChip(points: Int) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Aura Points",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$points",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AvatarWithInitials(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenContentPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        DashboardScreenContent(mainNavController = rememberNavController(), onVibeClicked = {})
    }
}
