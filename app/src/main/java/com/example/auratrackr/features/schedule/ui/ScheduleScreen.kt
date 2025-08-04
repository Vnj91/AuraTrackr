package com.example.auratrackr.features.schedule.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.schedule.viewmodel.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

private val DarkPurple = Color(0xFF1C1B2E)
private val AccentPink = Color(0xFFFFD6F5)
private val AccentYellow = Color(0xFFF7F6CF)
private val OffWhite = Color(0xFFF8F8F8)

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // --- Calendar Header ---
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text("TODAY IS", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.formatFullDate(uiState.selectedDate),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day", tint = Color.White)
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Horizontal Calendar ---
        HorizontalCalendar(
            dates = uiState.calendarDates,
            selectedDate = uiState.selectedDate,
            onDateSelected = { viewModel.onDateSelected(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Schedule Card ---
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = OffWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        uiState.selectedSchedule?.nickname ?: "No Schedule",
                        color = DarkPurple,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text("Edit", color = DarkPurple, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Add, contentDescription = "Add Schedule", tint = DarkPurple)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.selectedSchedule != null) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.selectedSchedule!!.workouts) { workout ->
                            ScheduleWorkoutItem(workout = workout)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No activities planned for this day.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleWorkoutItem(workout: Workout) {
    val cardColor = when (workout.status) {
        WorkoutStatus.COMPLETED -> AccentPink
        else -> AccentYellow
    }
    val timeFormat = SimpleDateFormat("h:mma", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (workout.status == WorkoutStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Status",
                tint = DarkPurple
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${timeFormat.format(Date())}", // Placeholder time
                    color = DarkPurple.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Text(workout.title, color = DarkPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(workout.description, color = DarkPurple.copy(alpha = 0.8f), fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkPurple.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { /* TODO: Navigate to workout screen */ },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = DarkPurple),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (workout.status == WorkoutStatus.PENDING) Icons.AutoMirrored.Filled.PlayArrow else Icons.Default.Pause,
                    contentDescription = "Start",
                    tint = Color.White
                )
            }
        }
    }
}

// ... (HorizontalCalendar and other helpers remain the same) ...
@Composable
fun HorizontalCalendar(dates: List<Date>, selectedDate: Date, onDateSelected: (Date) -> Unit) {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d", Locale.getDefault())
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(dates) { date ->
            val isSelected = areDatesSameDay(selectedDate, date)
            val backgroundColor by animateColorAsState(if (isSelected) Color(0xFFFF70C4) else Color.Transparent)
            val contentColor by animateColorAsState(if (isSelected) Color.White else Color.Gray)
            Column(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(backgroundColor).clickable { onDateSelected(date) }.padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(dayFormat.format(date).take(2), color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(dateFormat.format(date), color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
private fun areDatesSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B2E)
@Composable
fun ScheduleScreenPreview() {
    ScheduleScreen()
}
