package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents a single transaction where Aura Points were earned.
 *
 * This will be stored in a "points_history" sub-collection under a user's document.
 *
 * @property id The unique ID of this transaction.
 * @property points The number of points earned in this transaction.
 * @property vibeId The ID of the Vibe that was active when the points were earned.
 * @property source A string indicating how the points were earned (e.g., "COMPLETED_WORKOUT").
 * @property timestamp The server-generated timestamp of when the points were awarded.
 */
data class PointsHistory(
    @DocumentId val id: String = "",
    val points: Int = 0,
    val vibeId: String = "",
    val source: String = "",
    @ServerTimestamp val timestamp: Date? = null
)
