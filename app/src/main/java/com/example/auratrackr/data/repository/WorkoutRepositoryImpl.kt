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
import timber.log.Timber
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkoutRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val SCHEDULES_COLLECTION = "schedules"
        private const val FIELD_VIBE_ID = "vibeId"
        private const val FIELD_ASSIGNED_DATE = "assignedDate"
        private const val FIELD_NICKNAME = "nickname"
        private const val FIELD_WORKOUTS = "workouts"
    }

    private fun schedulesCollection(uid: String) =
        firestore.collection(USERS_COLLECTION).document(uid).collection(SCHEDULES_COLLECTION)

    override fun getSchedulesForDateAndVibe(uid: String, date: Date, vibeId: String): Flow<List<Schedule>> = callbackFlow {
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val startOfDay = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val endOfDay = Date.from(localDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())

        var query = schedulesCollection(uid)
            .whereGreaterThanOrEqualTo(FIELD_ASSIGNED_DATE, startOfDay)
            .whereLessThan(FIELD_ASSIGNED_DATE, endOfDay)

        if (vibeId.isNotEmpty()) {
            query = query.whereEqualTo(FIELD_VIBE_ID, vibeId)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "getSchedulesForDateAndVibe failed")
                close(error)
                return@addSnapshotListener
            }
            val schedules = snapshot?.toObjects(Schedule::class.java) ?: emptyList()
            trySend(schedules)
        }
        awaitClose { listener.remove() }
    }

    override fun getScheduleFlowById(uid: String, scheduleId: String): Flow<Schedule?> = callbackFlow {
        val listener = schedulesCollection(uid).document(scheduleId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "getScheduleFlowById failed")
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Schedule::class.java))
            }
        awaitClose { listener.remove() }
    }

    private suspend fun getScheduleById(uid: String, scheduleId: String): Schedule? {
        return try {
            schedulesCollection(uid).document(scheduleId).get().await().toObject(Schedule::class.java)
        } catch (e: Exception) {
            Timber.e(e, "getScheduleById failed")
            null
        }
    }

    override suspend fun createNewSchedule(uid: String, nickname: String, date: Date, vibeId: String): Result<String> {
        return try {
            val newSchedule = Schedule(nickname = nickname, assignedDate = date, vibeId = vibeId)
            val documentRef = schedulesCollection(uid).add(newSchedule).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Timber.e(e, "createNewSchedule failed")
            Result.failure(e)
        }
    }

    override suspend fun updateScheduleNickname(uid: String, scheduleId: String, newNickname: String): Result<Unit> = runCatching {
        schedulesCollection(uid).document(scheduleId).update(FIELD_NICKNAME, newNickname).await()
    }.map { // ✅ FIX: Explicitly map the success type from Java's Void to Kotlin's Unit.
    }.onFailure { Timber.e(it, "updateScheduleNickname failed") }

    override suspend fun addWorkoutToSchedule(uid: String, scheduleId: String, workout: Workout): Result<Unit> {
        return try {
            val workoutWithId = if (workout.id.isEmpty()) {
                workout.copy(id = UUID.randomUUID().toString())
            } else {
                workout
            }
            schedulesCollection(uid).document(scheduleId).update(FIELD_WORKOUTS, FieldValue.arrayUnion(workoutWithId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "addWorkoutToSchedule failed")
            Result.failure(e)
        }
    }

    override suspend fun updateWorkoutInSchedule(uid: String, scheduleId: String, workout: Workout): Result<Unit> {
        return try {
            val schedule = getScheduleById(uid, scheduleId)
                ?: return Result.failure(IllegalStateException("Schedule with ID $scheduleId not found."))

            val updatedWorkouts = schedule.workouts.map { if (it.id == workout.id) workout else it }

            schedulesCollection(uid).document(scheduleId).update(FIELD_WORKOUTS, updatedWorkouts).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "updateWorkoutInSchedule failed")
            Result.failure(e)
        }
    }

    override suspend fun deleteWorkoutFromSchedule(uid: String, scheduleId: String, workoutId: String): Result<Unit> {
        return try {
            val schedule = getScheduleById(uid, scheduleId)
                ?: return Result.failure(IllegalStateException("Schedule with ID $scheduleId not found."))

            val workoutToDelete = schedule.workouts.find { it.id == workoutId }
                ?: return Result.failure(IllegalStateException("Workout with ID $workoutId not found in schedule."))

            schedulesCollection(uid).document(scheduleId).update(FIELD_WORKOUTS, FieldValue.arrayRemove(workoutToDelete)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "deleteWorkoutFromSchedule failed")
            Result.failure(e)
        }
    }

    override suspend fun updateWorkoutStatus(uid: String, scheduleId: String, workoutId: String, newStatus: WorkoutStatus): Result<Unit> {
        return try {
            val schedule = getScheduleById(uid, scheduleId)
                ?: return Result.failure(IllegalStateException("Schedule with ID $scheduleId not found."))

            val updatedWorkouts = schedule.workouts.map {
                if (it.id == workoutId) it.copy(status = newStatus) else it
            }

            schedulesCollection(uid).document(scheduleId).update(FIELD_WORKOUTS, updatedWorkouts).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "updateWorkoutStatus failed")
            Result.failure(e)
        }
    }
}
