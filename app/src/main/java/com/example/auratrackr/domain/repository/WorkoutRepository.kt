package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * An interface that defines the contract for fetching and updating workout and schedule data.
 *
 * This repository abstracts the underlying data source (e.g., Firestore) and provides
 * a clean API for all schedule and workout-related operations.
 */
interface WorkoutRepository {

    // --- Read Operations ---

    /**
     * Retrieves a real-time stream of schedules for a specific user, date, and vibe.
     * @param uid The unique ID of the user.
     * @param date The date for which to fetch schedules.
     * @param vibeId The ID of the vibe to filter schedules by. If empty, no vibe filter is applied.
     * @return A [Flow] that emits a list of matching [Schedule]s.
     */
    fun getSchedulesForDateAndVibe(uid: String, date: Date, vibeId: String): Flow<List<Schedule>>

    /**
     * Retrieves a single schedule by its ID, providing real-time updates.
     * @param uid The unique ID of the user who owns the schedule.
     * @param scheduleId The unique ID of the schedule to fetch.
     * @return A [Flow] that emits the [Schedule] object, or `null` if not found.
     */
    fun getScheduleFlowById(uid: String, scheduleId: String): Flow<Schedule?>

    // --- Write Operations ---

    /**
     * Creates a new, empty schedule for a user.
     * @param uid The unique ID of the user.
     * @param nickname The user-given name for the schedule.
     * @param date The date the schedule is assigned to.
     * @param vibeId The ID of the vibe associated with this schedule.
     * @return A [Result] containing the new schedule's ID upon success.
     */
    suspend fun createNewSchedule(uid: String, nickname: String, date: Date, vibeId: String): Result<String>

    /**
     * Updates the nickname of an existing schedule.
     * @param uid The unique ID of the user.
     * @param scheduleId The ID of the schedule to update.
     * @param newNickname The new nickname to set.
     * @return A [Result] indicating success or failure.
     */
    suspend fun updateScheduleNickname(uid: String, scheduleId: String, newNickname: String): Result<Unit>

    /**
     * Adds a new workout to an existing schedule's workout list.
     * @param uid The unique ID of the user.
     * @param scheduleId The ID of the schedule to modify.
     * @param workout The [Workout] object to add.
     * @return A [Result] indicating success or failure.
     */
    suspend fun addWorkoutToSchedule(uid: String, scheduleId: String, workout: Workout): Result<Unit>

    /**
     * Updates an existing workout within a schedule.
     * @param uid The unique ID of the user.
     * @param scheduleId The ID of the schedule containing the workout.
     * @param workout The updated [Workout] object. Its ID will be used to find the original workout.
     * @return A [Result] indicating success or failure.
     */
    suspend fun updateWorkoutInSchedule(uid: String, scheduleId: String, workout: Workout): Result<Unit>

    /**
     * Deletes a workout from a schedule's workout list.
     * @param uid The unique ID of the user.
     * @param scheduleId The ID of the schedule to modify.
     * @param workoutId The ID of the workout to remove.
     * @return A [Result] indicating success or failure.
     */
    suspend fun deleteWorkoutFromSchedule(uid: String, scheduleId: String, workoutId: String): Result<Unit>

    // --- State Management ---

    /**
     * Updates the status of a specific workout within a schedule.
     * @param uid The unique ID of the user.
     * @param scheduleId The ID of the schedule containing the workout.
     * @param workoutId The ID of the workout to update.
     * @param newStatus The new [WorkoutStatus] to set.
     * @return A [Result] indicating success or failure.
     */
    suspend fun updateWorkoutStatus(uid: String, scheduleId: String, workoutId: String, newStatus: WorkoutStatus): Result<Unit>
}