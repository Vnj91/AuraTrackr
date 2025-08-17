package com.example.auratrackr.features.schedule.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.R
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.schedule.viewmodel.ScheduleViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                "TODAY IS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.formatFullDate(uiState.selectedDate),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { viewModel.onPreviousDateClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Day")
                }
                IconButton(onClick = { viewModel.onNextDateClicked() }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalCalendar(
            dates = uiState.calendarDates,
            selectedDate = uiState.selectedDate,
            onDateSelected = { viewModel.onDateSelected(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        uiState.selectedSchedule?.nickname ?: "No Schedule",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        uiState.selectedSchedule?.let {
                            navController.navigate(Screen.ScheduleEditor.createRoute(it.id))
                        }
                    }) {
                        Text("Edit", fontWeight = FontWeight.SemiBold)
                    }
                    IconButton(onClick = {
                        navController.navigate(Screen.ScheduleEditor.createRoute())
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Schedule")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    val selectedSchedule = uiState.selectedSchedule
                    if (selectedSchedule != null && selectedSchedule.hasWorkouts) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(
                                items = selectedSchedule.workouts,
                                key = { workout -> workout.id }
                            ) { workout ->
                                ScheduleWorkoutItem(
                                    workout = workout,
                                    onStartClicked = {
                                        navController.navigate(
                                            Screen.WorkoutInProgress.createRoute(
                                                scheduleId = selectedSchedule.id,
                                                workoutId = workout.id
                                            )
                                        )
                                    },
                                    onDeleteClicked = { viewModel.onDeleteWorkoutClicked(workout.id) }
                                )
                            }
                        }
                    } else {
                        EmptyScheduleContent(onAddClicked = {
                            navController.navigate(Screen.ScheduleEditor.createRoute(selectedSchedule?.id))
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleContent(onAddClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_no_schedule),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No activities planned for this day.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClicked) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Add Activity")
        }
    }
}

@Composable
fun ScheduleWorkoutItem(
    workout: Workout,
    onStartClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    val cardColor by animateColorAsState(
        targetValue = when (workout.status) {
            WorkoutStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.secondaryContainer
        }, label = "WorkoutItemColor"
    )
    val contentColor = when (workout.status) {
        WorkoutStatus.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (workout.status == WorkoutStatus.COMPLETED) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = workout.status.name,
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(workout.title, color = contentColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (workout.description.isNotBlank()) {
                    Text(workout.description, color = contentColor.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onDeleteClicked) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_activity_description, workout.title),
                    tint = contentColor.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onStartClicked,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start Workout", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun HorizontalCalendar(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val locale = Locale.getDefault()
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = dates,
            key = { it.toEpochDay() }
        ) { date ->
            val isSelected = selectedDate == date
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "DateBackgroundColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "DateContentColor"
            )

            val dayShort = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(locale)
            val dateNum = date.dayOfMonth.toString()

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(dayShort, color = contentColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(dateNum, color = contentColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    AuraTrackrTheme(darkTheme = true) {
        ScheduleScreen(navController = rememberNavController())
    }
}
