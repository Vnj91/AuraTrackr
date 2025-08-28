package com.example.auratrackr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Primary Brand Colors ---
val RoyalBlue = Color(0xFF4285F4)
val SoftCoral = Color(0xFFFF8A65)

// --- Dark Theme Palette ---
val NightBlue = Color(0xFF121826)
val SlateGray = Color(0xFF1D2434)
val LightGrayText = Color(0xFFA8B2C3)
val PureWhite = Color(0xFFFFFFFF)

// --- Light Theme Palette ---
val CloudWhite = Color(0xFFF5F7FA)
val Charcoal = Color(0xFF2F3B4D)

// --- Common Colors ---
val SuccessGreen = Color(0xFF34C759)
val ErrorRed = Color(0xFFFF453A)

// --- Vibe Colors ---
val VibeGymColor = Color(0xFF89CFF0)
val VibeStudyColor = Color(0xFFB0C4DE)
val VibeHomeColor = Color(0xFFB2D8B2)
val VibeWorkColor = Color(0xFFF5DEB3)


// --- Material 3 Color Schemes ---

val DarkColors = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = RoyalBlue.copy(alpha = 0.2f),
    onPrimaryContainer = RoyalBlue,
    secondary = SoftCoral,
    onSecondary = PureWhite,
    secondaryContainer = SoftCoral.copy(alpha = 0.2f),
    onSecondaryContainer = SoftCoral,
    tertiary = LightGrayText,
    background = NightBlue,
    onBackground = PureWhite,
    surface = SlateGray,
    onSurface = PureWhite,
    surfaceVariant = SlateGray.copy(alpha = 0.7f),
    // ✅ FIX: Increased contrast for better readability in dark mode.
    onSurfaceVariant = LightGrayText.copy(alpha = 0.8f),
    error = ErrorRed,
    onError = PureWhite,
    outline = LightGrayText.copy(alpha = 0.5f)
)

val LightColors = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = PureWhite,
    primaryContainer = RoyalBlue.copy(alpha = 0.1f),
    onPrimaryContainer = RoyalBlue,
    secondary = SoftCoral,
    onSecondary = PureWhite,
    secondaryContainer = SoftCoral.copy(alpha = 0.2f),
    onSecondaryContainer = SoftCoral,
    tertiary = Charcoal,
    background = CloudWhite,
    onBackground = Charcoal,
    surface = PureWhite,
    onSurface = Charcoal,
    surfaceVariant = Color.Gray.copy(alpha = 0.1f),
    // ✅ FIX: Increased contrast for better readability in light mode.
    onSurfaceVariant = Charcoal.copy(alpha = 0.8f),
    error = ErrorRed,
    onError = PureWhite,
    outline = LightGrayText
)
