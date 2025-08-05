package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents a pre-calculated summary of a user's activity over a specific period (e.g., a year).
 * This data is calculated by a backend Cloud Function and stored in a "summaries" sub-collection
 * under the user's document in Firestore to ensure fast loading.
 *
 * @property year The year this summary represents (e.g., 2025). This can be part of the document ID.
 * @property totalMinutesFocused The sum of all time spent in focus mode.
 * @property totalWorkoutsCompleted The total number of workouts marked as "COMPLETED".
 * @property auraPointsEarned The total number of Aura Points earned during the period.
 * @property mostBlockedApp The package name of the app that was blocked the most.
 * @property favoriteWorkout The title of the workout that was completed most often.
 */
data class UserSummary(
    @DocumentId val year: String = "",
    val totalMinutesFocused: Long = 0L,
    val totalWorkoutsCompleted: Int = 0,
    val auraPointsEarned: Int = 0,
    val mostBlockedApp: String = "N/A",
    val favoriteWorkout: String = "N/A"
) {
    // Firestore requires a no-argument constructor
    constructor() : this("", 0L, 0, 0, "N/A", "N/A")
}
