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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.R
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.schedule.viewmodel.ScheduleViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.SuccessGreen
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // ✅ FIX: Removed the redundant "SCHEDULE" label for a cleaner header.
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

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.schedulesForSelectedDate.isEmpty() -> {
                    EmptyScheduleContent(onAddClicked = {
                        navController.navigate(Screen.ScheduleEditor.createRoute())
                    })
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(uiState.schedulesForSelectedDate, key = { it.id }) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                navController = navController,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    navController: NavController,
    viewModel: ScheduleViewModel
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                schedule.nickname,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            // ✅ FIX: Removed the redundant "+" icon to avoid confusion.
            TextButton(onClick = {
                navController.navigate(Screen.ScheduleEditor.createRoute(schedule.id))
            }) {
                Text("Edit", fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (schedule.hasWorkouts) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                schedule.workouts.forEach { workout ->
                    ScheduleWorkoutItem(
                        workout = workout,
                        onStartClicked = {
                            navController.navigate(
                                Screen.WorkoutInProgress.createRoute(
                                    scheduleId = schedule.id,
                                    workoutId = workout.id
                                )
                            )
                        },
                        onDeleteClicked = { viewModel.onDeleteWorkoutClicked(schedule.id, workout.id) }
                    )
                }
            }
        } else {
            Text("No activities in this schedule.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        // ✅ FIX: Changed button text for a more motivating call to action.
        Button(onClick = onAddClicked) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Plan Activity")
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
            WorkoutStatus.COMPLETED -> SuccessGreen.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }, label = "WorkoutItemColor"
    )
    val contentColor = when (workout.status) {
        WorkoutStatus.COMPLETED -> SuccessGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                Text(workout.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (workout.description.isNotBlank()) {
                    Text(workout.description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onDeleteClicked) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_activity_description, workout.title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // ✅ FIX: Replaced the small FAB with a more prominent FilledTonalButton.
            FilledTonalButton(
                onClick = onStartClicked,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Start")
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
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                label = "DateBackgroundColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "DateContentColor"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                modifier = Modifier.clickable { onDateSelected(date) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).uppercase(locale),
                        color = contentColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        date.dayOfMonth.toString(),
                        color = contentColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        ScheduleScreen(navController = rememberNavController())
    }
}
