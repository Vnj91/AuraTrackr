package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents the data profile for a single user in the AuraTrackr application.
 *
 * This data class is designed for use with Firestore, with default values provided for all
 * properties to ensure seamless deserialization. It serves as the single source of truth
 * for user-specific data stored in the backend.
 *
 * @property uid The unique identifier for the user, automatically populated by Firestore from the document ID.
 * @property email The user's email address. Nullable for guest users.
 * @property username The user's display name. Nullable for guest users until set.
 * @property weightInKg The user's weight in kilograms. Nullable until provided during onboarding.
 * @property heightInCm The user's height in centimeters. Nullable until provided during onboarding.
 * @property hasCompletedOnboarding A flag indicating if the user has finished the initial setup process.
 * @property isGuest A flag to explicitly identify users who are signed in anonymously.
 * @property auraPoints The total points the user has earned through activities.
 * @property friends A list of UIDs representing the user's friends.
 * @property profilePictureUrl The URL for the user's profile picture stored in Firebase Storage.
 * @property createdAt A server-generated timestamp indicating when the user profile was created.
 */
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
)