package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Enum representing the possible metrics a challenge can track.
 * Using an enum improves type safety and prevents typos.
 */
enum class ChallengeMetric(val unit: String) {
    STEPS("steps"),
    KILOMETERS("km"),
    MINUTES("minutes")
}

/**
 * Represents a group challenge that users can participate in with their friends.
 * This data class is designed for use with Firestore. Default values are provided
 * for all properties to allow Firestore to deserialize documents into this object seamlessly.
 *
 * @property id The unique ID of the challenge document, automatically populated by Firestore.
 * @property title The name of the challenge (e.g., "Weekly Step Goal").
 * @property description A short description of the challenge's objective.
 * @property creatorId The UID of the user who created the challenge.
 * @property participants A list of UIDs of all users participating in the challenge.
 * @property goal The target value for the challenge (e.g., 100000 steps).
 * @property currentProgress The current combined progress of all participants.
 * @property metric The unit being tracked, represented by the [ChallengeMetric] enum.
 * @property startDate The server timestamp of when the challenge begins.
 * @property endDate The client-set timestamp of when the challenge ends.
 */
data class Challenge(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val participants: List<String> = emptyList(),
    val goal: Long = 0L,
    val currentProgress: Long = 0L,
    val metric: ChallengeMetric = ChallengeMetric.STEPS,
    @ServerTimestamp val startDate: Date? = null,
    val endDate: Date? = null
) {
    /**
     * A computed property to determine if the challenge is completed.
     * @return `true` if the current progress has met or exceeded the goal.
     */
    val isCompleted: Boolean
        get() = currentProgress >= goal

    /**
     * A computed property that calculates the completion progress as a percentage.
     * The value is coerced to be between 0.0 and 1.0.
     * @return The progress as a Double (e.g., 0.75 for 75%).
     */
    val progressPercentage: Float
        get() = if (goal > 0) (currentProgress.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
}
