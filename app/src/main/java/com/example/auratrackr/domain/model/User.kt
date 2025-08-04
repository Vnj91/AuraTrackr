package com.example.auratrackr.domain.model

/**
 * Represents the data profile for a single user in the AuraTrackr application.
 * This data will be stored in Firestore.
 *
 * @property uid The unique user ID, typically from Firebase Authentication. This is the document ID in Firestore.
 * @property email The user's email address. Can be null if they sign in through other means.
 * @property username The user's chosen display name.
 * @property weightInKg The user's weight, stored in kilograms.
 * @property heightInCm The user's height, stored in centimeters.
 * @property hasCompletedOnboarding A crucial flag that determines if the user has completed the initial setup.
 * @property auraPoints The user's in-app currency, earned through fitness and spent on screen time.
 */
data class User(
    val uid: String = "",
    val email: String? = null,
    val username: String? = null,
    val weightInKg: Int? = null,
    val heightInCm: Int? = null,
    val hasCompletedOnboarding: Boolean = false,
    val auraPoints: Int = 0 // <-- ADDED THIS LINE
)
