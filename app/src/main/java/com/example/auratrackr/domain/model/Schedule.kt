package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

/**
 * Represents a single, named workout schedule created by the user.
 *
 * This data class is designed for use with Firestore. Default values are provided for all
 * properties to allow Firestore to deserialize documents into this object seamlessly.
 *
 * @property id A unique identifier for the schedule, automatically populated by Firestore from the document ID.
 * @property nickname The user-given name for the schedule (e.g., "Morning Routine", "Weightlifting Day").
 * @property workouts The list of individual workout activities that make up this schedule. This list is
 * embedded directly in the document. For very long or complex workout lists, a sub-collection
 * might be a more scalable approach.
 * @property assignedDate The specific date this schedule is planned for. This is set by the client
 * and is not a server-generated timestamp.
 * @property vibeId The ID of the Vibe this schedule is associated with (e.g., "gym", "home").
 * @property isCompleted A flag indicating if all workouts in this schedule have been completed.
 */
data class Schedule(
    @DocumentId val id: String = "",
    val nickname: String = "",
    val workouts: List<Workout> = emptyList(),
    val assignedDate: Date? = null, // Removed @ServerTimestamp as this is a user-set date.
    val vibeId: String = "",
    val isCompleted: Boolean = false
) {
    /**
     * A computed property to quickly check if the schedule has any workouts.
     * @return `true` if the workouts list is not empty.
     */
    val hasWorkouts: Boolean
        get() = workouts.isNotEmpty()
}