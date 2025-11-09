package com.example.auratrackr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private const val ROYAL_BLUE_HEX = 0xFF4285F4
private const val SOFT_CORAL_HEX = 0xFFFF8A65

private const val NIGHT_BLUE_HEX = 0xFF121826
private const val SLATE_GRAY_HEX = 0xFF1D2434
private const val LIGHT_GRAY_TEXT_HEX = 0xFFA8B2C3
private const val PURE_WHITE_HEX = 0xFFFFFFFF

private const val CLOUD_WHITE_HEX = 0xFFF5F7FA
private const val CHARCOAL_HEX = 0xFF2F3B4D

private const val SUCCESS_GREEN_HEX = 0xFF34C759
private const val ERROR_RED_HEX = 0xFFFF453A

private const val VIBE_GYM_HEX = 0xFF89CFF0
private const val VIBE_STUDY_HEX = 0xFFB0C4DE
private const val VIBE_HOME_HEX = 0xFFB2D8B2
private const val VIBE_WORK_HEX = 0xFFF5DEB3

val RoyalBlue = Color(ROYAL_BLUE_HEX)
val SoftCoral = Color(SOFT_CORAL_HEX)

val NightBlue = Color(NIGHT_BLUE_HEX)
val SlateGray = Color(SLATE_GRAY_HEX)
val LightGrayText = Color(LIGHT_GRAY_TEXT_HEX)
val PureWhite = Color(PURE_WHITE_HEX)

val CloudWhite = Color(CLOUD_WHITE_HEX)
val Charcoal = Color(CHARCOAL_HEX)

val SuccessGreen = Color(SUCCESS_GREEN_HEX)
val ErrorRed = Color(ERROR_RED_HEX)

val VibeGymColor = Color(VIBE_GYM_HEX)
val VibeStudyColor = Color(VIBE_STUDY_HEX)
val VibeHomeColor = Color(VIBE_HOME_HEX)
val VibeWorkColor = Color(VIBE_WORK_HEX)

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
    surfaceContainer = SlateGray.copy(alpha = 0.9f),
    surfaceContainerHigh = SlateGray.copy(alpha = 1.0f),
    surfaceContainerHighest = PureWhite.copy(alpha = 0.1f),
    inversePrimary = RoyalBlue.copy(alpha = 0.8f),
    inverseSurface = LightGrayText,
    inverseOnSurface = NightBlue
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
    onSurfaceVariant = Charcoal.copy(alpha = 0.8f),
    error = ErrorRed,
    onError = PureWhite,
    outline = LightGrayText,
    surfaceContainer = Color(0xFFF0F2F5),
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    inversePrimary = RoyalBlue.copy(alpha = 0.8f),
    inverseSurface = Charcoal,
    inverseOnSurface = CloudWhite
)
