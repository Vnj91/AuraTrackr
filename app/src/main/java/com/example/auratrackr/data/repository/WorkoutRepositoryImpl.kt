package com.example.auratrackr.data.repository

import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkoutRepository {

    private fun schedulesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("schedules")

    // --- Read Operations ---

    override fun getSchedulesForDateAndVibe(uid: String, date: Date, vibeId: String): Flow<List<Schedule>> = callbackFlow {
        // Create start and end timestamps for the selected day
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
        val startOfDay = calendar.time
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59)
        val endOfDay = calendar.time

        // This query now filters by both the vibeId and the date range
        val listener = schedulesCollection(uid)
            .whereEqualTo("vibeId", vibeId)
            .whereGreaterThanOrEqualTo("assignedDate", startOfDay)
            .whereLessThanOrEqualTo("assignedDate", endOfDay)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
                trySend(schedules).isSuccess
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getWorkoutById(uid: String, scheduleId: String, workoutId: String): Workout? {
        return getScheduleById(uid, scheduleId)?.workouts?.find { it.id == workoutId }
    }

    override suspend fun getScheduleById(uid: String, scheduleId: String): Schedule? {
        return try {
            schedulesCollection(uid).document(scheduleId).get().await().toObject(Schedule::class.java)
        } catch (e: Exception) { null }
    }

    // --- Write Operations ---

    override suspend fun createNewSchedule(uid: String, nickname: String, date: Date, vibeId: String): Result<String> {
        return try {
            val newSchedule = Schedule(nickname = nickname, assignedDate = date, vibeId = vibeId)
            val documentRef = schedulesCollection(uid).add(newSchedule).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateScheduleNickname(uid: String, scheduleId: String, newNickname: String): Result<Unit> {
        return try {
            schedulesCollection(uid).document(scheduleId).update("nickname", newNickname).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addWorkoutToSchedule(uid: String, scheduleId: String, title: String, description: String): Result<Unit> {
        return try {
            val newWorkout = Workout(id = UUID.randomUUID().toString(), title = title, description = description, status = WorkoutStatus.PENDING)
            schedulesCollection(uid).document(scheduleId).update("workouts", FieldValue.arrayUnion(newWorkout)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteWorkoutFromSchedule(uid: String, scheduleId: String, workoutId: String): Result<Unit> {
        return try {
            val schedule = getScheduleById(uid, scheduleId) ?: throw IllegalStateException("Schedule not found")
            val workoutToDelete = schedule.workouts.find { it.id == workoutId } ?: throw IllegalStateException("Workout not found")
            schedulesCollection(uid).document(scheduleId).update("workouts", FieldValue.arrayRemove(workoutToDelete)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- State Management ---

    override suspend fun updateWorkoutStatus(uid: String, scheduleId: String, workoutId: String, newStatus: String): Result<Unit> {
        return try {
            val schedule = getScheduleById(uid, scheduleId) ?: throw IllegalStateException("Schedule not found")
            val updatedWorkouts = schedule.workouts.map {
                if (it.id == workoutId) {
                    it.copy(status = WorkoutStatus.valueOf(newStatus))
                } else {
                    it
                }
            }
            schedulesCollection(uid).document(scheduleId).update("workouts", updatedWorkouts).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
