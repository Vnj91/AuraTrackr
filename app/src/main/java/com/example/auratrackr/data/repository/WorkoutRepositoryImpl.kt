package com.example.auratrackr.data.repository

import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The concrete implementation of the WorkoutRepository.
 * This implementation now holds a stateful, updatable map of schedules for different days.
 */
@Singleton
class WorkoutRepositoryImpl @Inject constructor() : WorkoutRepository {

    // Helper to format dates into a consistent key format (YYYY-MM-DD)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // --- MOCK DATA ---
    // We'll create a few sample schedules for different days.
    private val today = Date()
    private val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
    private val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time

    private val mockSchedules = mutableMapOf(
        dateFormat.format(today) to Schedule(
            id = UUID.randomUUID().toString(),
            nickname = "Today's Grind",
            workouts = listOf(
                Workout(id = UUID.randomUUID().toString(), title = "WarmUp", description = "Run 02 km", status = WorkoutStatus.ACTIVE),
                Workout(id = UUID.randomUUID().toString(), title = "Muscle Up", description = "10 reps, 3 sets", status = WorkoutStatus.PENDING),
                Workout(id = UUID.randomUUID().toString(), title = "Cool Down", description = "10 min stretching", status = WorkoutStatus.PENDING)
            ),
            assignedDate = today
        ),
        dateFormat.format(yesterday) to Schedule(
            id = UUID.randomUUID().toString(),
            nickname = "Yesterday's Session",
            workouts = listOf(
                Workout(id = UUID.randomUUID().toString(), title = "Full Body Workout", description = "1 hour session", status = WorkoutStatus.COMPLETED)
            ),
            assignedDate = yesterday
        ),
        dateFormat.format(tomorrow) to Schedule(
            id = UUID.randomUUID().toString(),
            nickname = "Leg Day Prep",
            workouts = listOf(
                Workout(id = UUID.randomUUID().toString(), title = "Squats", description = "12 reps, 4 sets", status = WorkoutStatus.PENDING),
                Workout(id = UUID.randomUUID().toString(), title = "Leg Press", description = "10 reps, 4 sets", status = WorkoutStatus.PENDING)
            ),
            assignedDate = tomorrow
        )
    )
    // --- END MOCK DATA ---

    // A MutableStateFlow to hold the current, live state of all schedules.
    private val _schedulesStateFlow = MutableStateFlow(mockSchedules)

    override fun getTodaysSchedule(): Flow<List<Workout>> {
        // The dashboard still needs today's workouts, so we find them in our map.
        return _schedulesStateFlow.asStateFlow().map { schedules ->
            schedules[dateFormat.format(Date())]?.workouts ?: emptyList()
        }
    }

    override fun getScheduleForDate(date: Date): Flow<Schedule?> {
        // The new function for the Schedule screen.
        return _schedulesStateFlow.asStateFlow().map { schedules ->
            schedules[dateFormat.format(date)]
        }
    }

    override suspend fun getWorkoutById(id: String): Workout? {
        // Find the workout across all schedules.
        return _schedulesStateFlow.value.values.flatMap { it.workouts }.find { it.id == id }
    }

    override suspend fun startWorkout(id: String) {
        val currentSchedules = _schedulesStateFlow.value.toMutableMap()
        for ((date, schedule) in currentSchedules) {
            val newList = schedule.workouts.map { workout ->
                when (workout.id) {
                    id -> workout.copy(status = WorkoutStatus.ACTIVE)
                    else -> workout.copy(status = if (workout.status != WorkoutStatus.COMPLETED) WorkoutStatus.PENDING else WorkoutStatus.COMPLETED)
                }
            }
            currentSchedules[date] = schedule.copy(workouts = newList)
        }
        _schedulesStateFlow.value = currentSchedules
    }

    override suspend fun finishWorkout(id: String) {
        val currentSchedules = _schedulesStateFlow.value.toMutableMap()
        for ((date, schedule) in currentSchedules) {
            val newList = schedule.workouts.map { workout ->
                if (workout.id == id) {
                    workout.copy(status = WorkoutStatus.COMPLETED)
                } else {
                    workout
                }
            }
            currentSchedules[date] = schedule.copy(workouts = newList)
        }
        _schedulesStateFlow.value = currentSchedules
    }
}
