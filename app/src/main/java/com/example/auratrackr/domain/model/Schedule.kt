package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a single, named workout schedule created by the user.
 * This class is designed to be stored as a document in a "schedules" sub-collection in Firestore.
 *
 * @property id A unique identifier for the schedule, automatically populated by Firestore from the document ID.
 * @property nickname The user-given name for the schedule (e.g., "Morning Routine", "Weightlifting Day").
 * @property workouts The list of individual workout activities that make up this schedule.
 * @property assignedDate The specific date this schedule is planned for.
 * @property vibeId The ID of the Vibe this schedule is associated with (e.g., "1" for "Gym").
 */
data class Schedule(
    @DocumentId val id: String = "",
    val nickname: String = "",
    val workouts: List<Workout> = emptyList(),
    @ServerTimestamp val assignedDate: Date? = null,
    val vibeId: String = "" // <-- ADDED THIS LINE
) {
    // Firestore requires a no-argument constructor for deserialization
    constructor() : this("", "", emptyList(), null, "")
}
