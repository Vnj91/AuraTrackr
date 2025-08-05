package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId

/**
 * Represents the data profile for a single user in the AuraTrackr application.
 * This data will be stored in Firestore.
 */
data class User(
    @DocumentId val uid: String = "",
    val email: String? = null,
    val username: String? = null,
    val weightInKg: Int? = null,
    val heightInCm: Int? = null,
    val hasCompletedOnboarding: Boolean = false,
    val auraPoints: Int = 0,
    val friends: List<String> = emptyList(),
    val profilePictureUrl: String? = null // <-- ADDED THIS LINE
) {
    // Firestore requires a no-argument constructor for deserialization
    constructor() : this("", null, null, null, null, false, 0, emptyList(), null)
}
