package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * An interface that defines the contract for fetching and updating workout and schedule data from Firestore.
 * All functions are now user-specific (require a UID) and vibe-aware where necessary.
 */
interface WorkoutRepository {

    // --- Read Operations ---
    fun getSchedulesForDateAndVibe(uid: String, date: Date, vibeId: String): Flow<List<Schedule>>
    suspend fun getWorkoutById(uid: String, scheduleId: String, workoutId: String): Workout?
    suspend fun getScheduleById(uid: String, scheduleId: String): Schedule?

    // --- Write Operations ---
    suspend fun createNewSchedule(uid: String, nickname: String, date: Date, vibeId: String): Result<String>
    suspend fun updateScheduleNickname(uid: String, scheduleId: String, newNickname: String): Result<Unit>
    suspend fun addWorkoutToSchedule(uid: String, scheduleId: String, title: String, description: String): Result<Unit>
    suspend fun deleteWorkoutFromSchedule(uid: String, scheduleId: String, workoutId: String): Result<Unit>

    // --- State Management ---
    suspend fun updateWorkoutStatus(uid: String, scheduleId: String, workoutId: String, newStatus: String): Result<Unit>
}
