package com.example.auratrackr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// --- Primary Brand Colors ---
val BrandDarkPurple = Color(0xFF1C1B2E)
val BrandCardPurple = Color(0xFF2C2B3C)
val BrandAccentYellow = Color(0xFFD4B42A)
val BrandAccentPink = Color(0xFFFF70C4)
val BrandOffWhite = Color(0xFFF8F8F8)
val BrandHighContrastGray = Color(0xFFB0B3B8)
val BrandErrorRed = Color(0xFFCF6679) // A more theme-friendly red

// --- Vibe Colors ---
// These are dynamic and will be applied separately, not part of the main theme.
val VibeGymColor = Color(0xFF89CFF0)
val VibeStudyColor = Color(0xFFB0C4DE)
val VibeHomeColor = Color(0xFFB2D8B2)
val VibeWorkColor = Color(0xFFF5DEB3)

// --- Material 3 Color Schemes ---

val DarkColors = darkColorScheme(
    primary = BrandAccentYellow,
    onPrimary = BrandDarkPurple,
    primaryContainer = BrandAccentYellow.copy(alpha = 0.3f),
    onPrimaryContainer = BrandAccentYellow,
    secondary = BrandAccentPink,
    onSecondary = BrandDarkPurple,
    secondaryContainer = BrandAccentPink.copy(alpha = 0.3f),
    onSecondaryContainer = BrandAccentPink,
    tertiary = Color.White,
    background = BrandDarkPurple,
    onBackground = BrandOffWhite,
    surface = BrandCardPurple,
    onSurface = BrandOffWhite,
    surfaceVariant = BrandCardPurple.copy(alpha = 0.7f),
    onSurfaceVariant = BrandHighContrastGray,
    error = BrandErrorRed,
    onError = BrandDarkPurple,
    outline = BrandHighContrastGray.copy(alpha = 0.5f)
)

val LightColors = lightColorScheme(
    primary = BrandDarkPurple,
    onPrimary = BrandOffWhite,
    primaryContainer = BrandDarkPurple.copy(alpha = 0.1f),
    onPrimaryContainer = BrandDarkPurple,
    secondary = BrandAccentPink,
    onSecondary = BrandOffWhite,
    secondaryContainer = BrandAccentPink.copy(alpha = 0.2f),
    onSecondaryContainer = BrandAccentPink,
    tertiary = BrandDarkPurple,
    background = BrandOffWhite,
    onBackground = BrandDarkPurple,
    surface = Color.White,
    onSurface = BrandDarkPurple,
    surfaceVariant = Color.Gray.copy(alpha = 0.2f),
    onSurfaceVariant = Color.DarkGray,
    error = BrandErrorRed,
    onError = BrandOffWhite,
    outline = BrandHighContrastGray.copy(alpha = 0.8f)
)
