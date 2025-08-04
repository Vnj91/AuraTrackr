package com.example.auratrackr.features.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.dashboard.viewmodel.DashboardViewModel
import java.util.Calendar

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
            .padding(vertical = 16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hi,", color = Color.White.copy(alpha = 0.8f), fontSize = 32.sp)
                    Text("@" + uiState.username, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
                AuraPointsChip(points = uiState.auraPoints)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Total screen time usage", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.height(150.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Loading chart...", color = Color.Gray)
            }
        } else {
            ScreenTimeBarChart(weeklyUsage = uiState.weeklyUsage)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Schedule Section
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Schedule", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onVibeClicked,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Change Vibe", tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Today's Activity", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Timeline
        if (uiState.isLoading) {
            Text("Loading schedule...", color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
        } else if (uiState.todaysSchedule != null) {
            ScheduleTimeline(
                schedule = uiState.todaysSchedule!!, // We know it's not null here
                onStartWorkout = { workout ->
                    // Call the ViewModel to update the state in Firestore
                    viewModel.startWorkout(uiState.todaysSchedule!!.id, workout.id)
                    // Navigate to the workout screen with both IDs
                    mainNavController.navigate(
                        Screen.WorkoutInProgress.createRoute(
                            scheduleId = uiState.todaysSchedule!!.id,
                            workoutId = workout.id
                        )
                    )
                }
            )
        } else {
            Text("No schedule for today.", color = Color.Gray, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
fun ScheduleTimeline(
    schedule: Schedule, // <-- UPDATED: Takes the full Schedule
    onStartWorkout: (Workout) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 24.dp)) {
        itemsIndexed(schedule.workouts) { index, workout -> // <-- Use schedule.workouts
            WorkoutItem(
                workout = workout,
                isFirstItem = index == 0,
                isLastItem = index == schedule.workouts.lastIndex,
                onStartClicked = { onStartWorkout(workout) }
            )
        }
    }
}

// ... (All other composables like AuraPointsChip, WorkoutItem, etc., remain the same) ...
@Composable
fun AuraPointsChip(points: Int) {
    Row(modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Star, contentDescription = "Aura Points", tint = Color(0xFFD4B42A), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$points", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
@Composable
fun WorkoutItem(workout: Workout, isFirstItem: Boolean, isLastItem: Boolean, onStartClicked: () -> Unit) {
    val timelineColor = when (workout.status) {
        WorkoutStatus.ACTIVE -> Color(0xFFD4B42A)
        WorkoutStatus.COMPLETED -> Color(0xFF4CAF50)
        WorkoutStatus.PENDING -> Color.Gray
    }
    val circleColor = if (workout.status == WorkoutStatus.ACTIVE) Color(0xFFD4B42A) else Color.Transparent
    Row(modifier = Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.width(30.dp).fillMaxHeight()) {
            val circleRadius = 8.dp.toPx()
            val circleCenterY = size.height / 2
            if (!isFirstItem) {
                drawTimelineLine(timelineColor, Offset(center.x, 0f), Offset(center.x, circleCenterY - circleRadius))
            }
            if (!isLastItem) {
                drawTimelineLine(timelineColor, Offset(center.x, circleCenterY + circleRadius), Offset(center.x, size.height))
            }
            if (workout.status == WorkoutStatus.COMPLETED) {
                drawCircle(color = timelineColor, radius = circleRadius)
            } else {
                drawCircle(color = timelineColor, radius = circleRadius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                drawCircle(color = circleColor, radius = circleRadius - 2.dp.toPx())
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(vertical = 16.dp)) {
            Text(workout.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(workout.description, color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        if (workout.status == WorkoutStatus.COMPLETED) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Completed", tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
        } else {
            Button(onClick = onStartClicked, shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = if (workout.status == WorkoutStatus.ACTIVE) Color(0xFFD4B42A) else Color.DarkGray), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)) {
                Text("Start", color = if (workout.status == WorkoutStatus.ACTIVE) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
private fun DrawScope.drawTimelineLine(color: Color, start: Offset, end: Offset) {
    val isDashed = color == Color.Gray
    drawLine(color = color, start = start, end = end, strokeWidth = 2.dp.toPx(), pathEffect = if (isDashed) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null)
}
@Composable
fun ScreenTimeBarChart(weeklyUsage: List<Long>) {
    val days = listOf("Mon", "Tues", "Wed", "Thurs", "Fri", "Sat", "Sun")
    val maxUsage = (weeklyUsage.maxOrNull() ?: 1L).coerceAtLeast(1L)
    val calendar = Calendar.getInstance()
    val todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7
    Row(modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        days.forEachIndexed { index, day ->
            val usage = weeklyUsage.getOrElse(index) { 0L }
            val barHeightFraction = (usage.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
            val isToday = index == todayIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.height(100.dp).width(20.dp)) {
                    drawRoundRect(color = if (isToday) Color.White else Color.Gray.copy(alpha = 0.5f), topLeft = Offset(x = 0f, y = size.height * (1 - barHeightFraction)), size = Size(width = size.width, height = size.height * barHeightFraction), cornerRadius = CornerRadius(x = 8.dp.toPx(), y = 8.dp.toPx()))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = day, color = if (isToday) Color.White else Color.Gray, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
            }
        }
    }
}
