package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import java.util.Date

data class Schedule(
    @DocumentId val id: String = "",
    val nickname: String = "",
    val workouts: List<Workout> = emptyList(),
    val vibeId: String = "",
    val repeatDays: List<String> = emptyList(),
    val assignedDate: Date? = null,
    val completedOn: Map<String, Boolean> = emptyMap()
) {
    /**
     * ✅ FIX: Exclude this computed property from Firestore serialization.
     */
    @get:Exclude
    val hasWorkouts: Boolean
        get() = workouts.isNotEmpty()

    /**
     * ✅ FIX: Exclude this computed property from Firestore serialization.
     */
    @get:Exclude
    val isRecurring: Boolean
        get() = repeatDays.isNotEmpty()
}
