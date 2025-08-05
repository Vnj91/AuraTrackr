package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a group challenge that users can participate in with their friends.
 * This will be stored in a top-level "challenges" collection in Firestore.
 *
 * @property id The unique ID of the challenge document.
 * @property title The name of the challenge (e.g., "Weekly Step Goal", "100km Run Club").
 * @property description A short description of the challenge's objective.
 * @property creatorId The UID of the user who created the challenge.
 * @property participants A list of UIDs of all users participating in the challenge.
 * @property goal The target value for the challenge (e.g., 100000 steps).
 * @property currentProgress The current combined progress of all participants.
 * @property metric The unit being tracked (e.g., "steps", "km", "minutes").
 * @property startDate The server timestamp of when the challenge begins.
 * @property endDate The server timestamp of when the challenge ends.
 */
data class Challenge(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val participants: List<String> = emptyList(),
    val goal: Long = 0,
    val currentProgress: Long = 0,
    val metric: String = "steps",
    @ServerTimestamp val startDate: Date? = null,
    val endDate: Date? = null
) {
    // Firestore requires a no-argument constructor
    constructor() : this("", "", "", "", emptyList(), 0, 0, "steps", null, null)
}
