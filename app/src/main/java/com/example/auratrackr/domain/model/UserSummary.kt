package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a pre-calculated summary of a user's activity over a specific period (e.g., a year).
 *
 * This data class is designed for use with Firestore. It's intended to be generated and updated
 * by a backend process (like a Cloud Function) and stored in a "summaries" sub-collection
 * under the user's document to ensure fast loading for features like "Aura Wrapped".
 *
 * @property year The year this summary represents (e.g., "2025"), used as the document ID for easy lookup.
 * @property totalMinutesFocused The sum of all time spent in focus mode during the year.
 * @property totalWorkoutsCompleted The total number of workouts marked as completed during the year.
 * @property auraPointsEarned The total number of Aura Points earned during the year.
 * @property mostBlockedApp The package name of the app that was blocked the most. Null if no apps were blocked.
 * @property favoriteWorkout The title of the workout that was completed most often. Null if no workouts were completed.
 * @property lastUpdated A server-generated timestamp indicating when this summary was last calculated.
 */
data class UserSummary(
    @DocumentId val year: String = "",
    val totalMinutesFocused: Long = 0L,
    val totalWorkoutsCompleted: Int = 0,
    val auraPointsEarned: Int = 0,
    val mostBlockedApp: String? = null,
    val favoriteWorkout: String? = null,
    @ServerTimestamp val lastUpdated: Date? = null
) {
    /**
     * A computed property to quickly check if the summary contains any meaningful data.
     * @return `true` if at least one metric is greater than zero.
     */
    val hasData: Boolean
        get() = totalMinutesFocused > 0 || totalWorkoutsCompleted > 0 || auraPointsEarned > 0
}
