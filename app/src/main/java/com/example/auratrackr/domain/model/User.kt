package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    @DocumentId val uid: String = "",
    val email: String? = null,
    val username: String? = null,
    val weightInKg: Int? = null,
    val heightInCm: Int? = null,
    val hasCompletedOnboarding: Boolean = false,
    val isGuest: Boolean = false,
    val auraPoints: Int = 0,
    val friends: List<String> = emptyList(),
    val profilePictureUrl: String? = null,
    @ServerTimestamp val createdAt: Date? = null
) {
    /**
     * ✅ FIX: Exclude this computed property from Firestore serialization.
     */
    @get:Exclude
    val isProfileComplete: Boolean
        get() = weightInKg != null && heightInCm != null
}
