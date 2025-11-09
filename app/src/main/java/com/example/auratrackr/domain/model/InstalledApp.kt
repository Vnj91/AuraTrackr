package com.example.auratrackr.domain.model

/**
 * Represents a single application installed on the user's device.
 *
 * This is a pure data model, intentionally kept simple to be platform-agnostic,
 * easily testable, and serializable if needed. It contains only the essential,
 * non-UI-specific information about an app.
 *
 * The application's icon, being an Android-specific `Drawable`, should be resolved
 * in the UI/presentation layer (e.g., in a Composable or ViewModel) using the
 * `packageName` to interact with the `PackageManager`.
 *
 * @property name The display name of the application (e.g., "Instagram").
 * @property packageName The unique package name used to identify the app (e.g., "com.instagram.android").
 */
data class InstalledApp(
    val name: String,
    val packageName: String
)
