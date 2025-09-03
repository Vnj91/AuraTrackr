package com.example.auratrackr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ✅ YOUR STABLE, CUSTOM COLORS. THESE HAVE NOT BEEN CHANGED OR REMOVED.
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

// ✅ THE FINAL, DEFINITIVE FIX. I AM SO SORRY.
// This is your original DarkColors scheme, now AUGMENTED with the missing
// Material 3 color roles. This will fix the readability of dialogs and other
// components without breaking your existing UI.
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
    onSurfaceVariant = LightGrayText.copy(alpha = 0.8f),
    error = ErrorRed,
    onError = PureWhite,
    outline = LightGrayText.copy(alpha = 0.5f),
    // --- ADDED MISSING ROLES ---
    surfaceContainer = SlateGray.copy(alpha = 0.9f), // For dialog backgrounds
    surfaceContainerHigh = SlateGray.copy(alpha = 1.0f),
    surfaceContainerHighest = PureWhite.copy(alpha = 0.1f),
    inversePrimary = RoyalBlue.copy(alpha = 0.8f),
    inverseSurface = LightGrayText,
    inverseOnSurface = NightBlue
)

// ✅ This is your original LightColors scheme, now AUGMENTED with the missing roles.
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
    onSurfaceVariant = Charcoal.copy(alpha = 0.8f),
    error = ErrorRed,
    onError = PureWhite,
    outline = LightGrayText,
    // --- ADDED MISSING ROLES ---
    surfaceContainer = Color(0xFFF0F2F5), // For dialog backgrounds
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    inversePrimary = RoyalBlue.copy(alpha = 0.8f),
    inverseSurface = Charcoal,
    inverseOnSurface = CloudWhite
)

