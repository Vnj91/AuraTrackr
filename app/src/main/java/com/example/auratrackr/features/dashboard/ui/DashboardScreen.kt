package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.dashboard.viewmodel.DashboardUiState
import com.example.auratrackr.features.dashboard.viewmodel.DashboardViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreenContent(
    viewModel: DashboardViewModel = hiltViewModel(),
    mainNavController: NavController,
    onVibeClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Use theme color
            .systemBarsPadding()
            .padding(vertical = 16.dp)
    ) {
        DashboardHeader(
            uiState = uiState,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Total screen time usage",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            ScreenTimeBarChart(weeklyUsage = uiState.weeklyUsage)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onVibeClicked,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Change Vibe",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Today's Activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
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
                    }
                )
            } ?: EmptySchedulePlaceholder()
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiState.profilePictureUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_person_placeholder),
                error = painterResource(R.drawable.ic_person_placeholder),
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
fun EmptySchedulePlaceholder() {
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
    }
}

@Composable
fun ScheduleTimeline(
    schedule: Schedule,
    onStartWorkout: (Workout) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(schedule.workouts, key = { _, workout -> workout.id }) { index, workout ->
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
    val completedColor = Color(0xFF4CAF50)
    val pendingColor = MaterialTheme.colorScheme.onSurfaceVariant

    val timelineColor = when (workout.status) {
        WorkoutStatus.ACTIVE -> activeColor
        WorkoutStatus.COMPLETED -> completedColor
        WorkoutStatus.PENDING -> pendingColor
    }

    Row(modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        // Pass a boolean for dashing instead of the color
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
                Text("Start", fontWeight = FontWeight.Bold)
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

// ✅ FIX: This function is no longer Composable and takes a boolean to decide the path effect.
private fun DrawScope.drawTimelineLine(color: Color, start: Offset, end: Offset, isDashed: Boolean) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 2.dp.toPx(),
        pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
    )
}

@Composable
fun ScreenTimeBarChart(weeklyUsage: List<Long>) {
    val days = remember {
        (0..6).map {
            LocalDate.now().minusDays(it.toLong()).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }.reversed()
    }
    val maxUsage = remember(weeklyUsage) { (weeklyUsage.maxOrNull() ?: 1L).coerceAtLeast(1L) }

    // ✅ FIX: Read theme colors here, outside the non-composable loop.
    val todayBarColor = MaterialTheme.colorScheme.primary
    val otherBarColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val todayTextColor = MaterialTheme.colorScheme.primary
    val otherTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        weeklyUsage.forEachIndexed { index, usage ->
            val barHeightFraction = (usage.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
            val animatedBarHeightFraction by animateFloatAsState(targetValue = barHeightFraction, animationSpec = tween(1000), label = "BarHeightAnimation")
            val isToday = index == weeklyUsage.lastIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                val barColor = if (isToday) todayBarColor else otherBarColor
                Canvas(modifier = Modifier.height(100.dp).width(20.dp)) {
                    drawRoundRect(
                        color = barColor, // Use the color variable
                        topLeft = Offset(x = 0f, y = size.height * (1 - animatedBarHeightFraction)),
                        size = Size(width = size.width, height = size.height * animatedBarHeightFraction),
                        cornerRadius = CornerRadius(x = 8.dp.toPx(), y = 8.dp.toPx())
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = days[index],
                    color = if (isToday) todayTextColor else otherTextColor,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
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

@Preview(showBackground = true)
@Composable
fun DashboardScreenContentPreview() {
    AuraTrackrTheme(darkTheme = true) {
        DashboardScreenContent(mainNavController = rememberNavController(), onVibeClicked = {})
    }
}
