package com.example.auratrackr.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Represents a "Vibe" in the application.
 *
 * A Vibe is a user-selectable mode that changes the app's theme (e.g., background color)
 * and can be used to filter content, such as workout schedules. This class is designed
 * to be pure data, making it suitable for storage and use across different layers of the app.
 *
 * @property id A unique identifier for the vibe (e.g., "gym", "study").
 * @property name The display name of the vibe.
 * @property colorHex The primary background color associated with this vibe, stored as a Long
 * representing the ARGB hex value (e.g., 0xFF2C2B3C). This is serializable
 * and platform-agnostic.
 * @property iconName An optional string key representing an icon. This allows the UI to
 * map this key to a specific `ImageVector` or drawable resource.
 */
data class Vibe(
    val id: String,
    val name: String,
    val colorHex: Long,
    val iconName: String? = null
) {
    /**
     * A convenience extension property to convert the stored hex Long into a Compose Color.
     * This should be used primarily in the UI layer.
     */
    val backgroundColor: Color
        get() = Color(colorHex)
}