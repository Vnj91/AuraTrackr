package com.example.auratrackr.domain.model

import com.google.firebase.firestore.Exclude
import java.util.Date

enum class WorkoutStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

data class Workout(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val status: WorkoutStatus = WorkoutStatus.PENDING,
    val durationInSeconds: Long = 0L,
    val reps: Int = 0,
    val sets: Int = 0,
    val lastCompleted: Date? = null
) {
    /**
     * ✅ FIX: Exclude this computed property from Firestore serialization.
     */
    @get:Exclude
    val isCompleted: Boolean
        get() = status == WorkoutStatus.COMPLETED
}
