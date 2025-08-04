package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * An interface that defines the contract for fetching and updating workout and schedule data.
 */
interface WorkoutRepository {

    /**
     * Fetches the user's scheduled workouts for the current day as a real-time Flow.
     * This will be used by the main dashboard.
     */
    fun getTodaysSchedule(): Flow<List<Workout>>

    /**
     * Fetches the user's schedule for a specific date.
     * This will be used by the new Schedule screen.
     *
     * @param date The specific date to fetch the schedule for.
     * @return A Flow that emits the Schedule object for that day, or null if none exists.
     */
    fun getScheduleForDate(date: Date): Flow<Schedule?> // <-- ADDED THIS NEW FUNCTION

    /**
     * Finds a specific workout by its ID.
     */
    suspend fun getWorkoutById(id: String): Workout?

    /**
     * Sets a specific workout's status to ACTIVE and all others to PENDING.
     */
    suspend fun startWorkout(id: String)

    /**
     * Sets a specific workout's status to COMPLETED.
     */
    suspend fun finishWorkout(id: String)
}
