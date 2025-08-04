package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents the status of a workout in the user's schedule.
 */
enum class WorkoutStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

/**
 * Represents a single workout session in a schedule.
 * This data class is designed to be stored as part of a Schedule document in Firestore.
 *
 * @property id A unique identifier for this workout instance.
 * @property title The name of the workout (e.g., "WarmUp", "Muscle Up").
 * @property description A summary of the workout details (e.g., "Run 02 km", "10 reps, 3 sets...").
 * @property status The current status of the workout.
 */
data class Workout(
    @DocumentId val id: String = "", // Firestore will automatically populate this with the document ID if needed
    val title: String = "",
    val description: String = "",
    val status: WorkoutStatus = WorkoutStatus.PENDING
) {
    // Firestore requires a no-argument constructor for deserialization
    constructor() : this("", "", "", WorkoutStatus.PENDING)
}
