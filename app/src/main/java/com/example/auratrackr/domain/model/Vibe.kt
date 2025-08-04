package com.example.auratrackr.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a "Vibe" in the application.
 * A Vibe is a user-selectable mode that changes the app's theme and can influence
 * which schedules are shown.
 *
 * @property id A unique identifier for the vibe.
 * @property name The display name of the vibe (e.g., "Gym", "Study").
 * @property backgroundColor The primary background color associated with this vibe.
 */
data class Vibe(
    val id: String,
    val name: String,
    val backgroundColor: Color
    // We can add an icon property here later if needed
)
