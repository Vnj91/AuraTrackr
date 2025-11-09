package com.example.auratrackr.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Centralized layout spacing constants to reduce magic numbers across UI files.
// Use these in composables and theme-related code when appropriate.
object Dimensions {
    // Common small spacing used for fine-grained layout gaps (8dp).
    val Small: Dp = 8.dp

    // Common medium spacing (12dp).
    val Medium: Dp = 12.dp

    // Common large spacing (16dp).
    val Large: Dp = 16.dp

    // Standard minimum touch target for tappable controls (Fitts' law).
    val MinTouchTarget: Dp = 48.dp

    // Standard avatar / icon sizes
    val AvatarSmall: Dp = 32.dp
    val AvatarMedium: Dp = 48.dp
    val AvatarLarge: Dp = 72.dp
}
