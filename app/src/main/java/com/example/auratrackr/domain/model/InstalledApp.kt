package com.example.auratrackr.domain.model

import android.graphics.drawable.Drawable

/**
 * Represents a single application installed on the user's device.
 * This is a simplified model used for displaying apps in the UI.
 *
 * @property name The display name of the application (e.g., "Instagram").
 * @property packageName The unique package name (e.g., "com.instagram.android").
 * @property icon The application's icon as a Drawable.
 */
data class InstalledApp(
    val name: String,
    val packageName: String,
    val icon: Drawable
)
