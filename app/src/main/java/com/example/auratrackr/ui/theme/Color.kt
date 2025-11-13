package com.example.auratrackr.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium iOS-inspired color palette
private const val ELECTRIC_BLUE_HEX = 0xFF007AFF
private const val DEEP_PURPLE_HEX = 0xFF5856D6
private const val SOFT_PINK_HEX = 0xFFFF2D55
private const val VIBRANT_TEAL_HEX = 0xFF5AC8FA

// Dark theme - inspired by iOS dark mode
private const val RICH_BLACK_HEX = 0xFF000000
private const val DEEP_CHARCOAL_HEX = 0xFF1C1C1E
private const val ELEVATED_DARK_HEX = 0xFF2C2C2E
private const val SOFT_GRAY_HEX = 0xFF3A3A3C
private const val LIGHT_GRAY_TEXT_HEX = 0xFF98989D
private const val PURE_WHITE_HEX = 0xFFFFFFFF

// Light theme - inspired by iOS light mode
private const val SNOW_WHITE_HEX = 0xFFFFFFFF
private const val LIGHT_BACKGROUND_HEX = 0xFFF2F2F7
private const val ELEVATED_LIGHT_HEX = 0xFFFFFFFF
private const val SOFT_BORDER_HEX = 0xFFD1D1D6
private const val DARK_TEXT_HEX = 0xFF000000

// System colors
private const val SUCCESS_GREEN_HEX = 0xFF34C759
private const val ERROR_RED_HEX = 0xFFFF3B30
private const val WARNING_ORANGE_HEX = 0xFFFF9500

// Vibe colors - vibrant and modern
private const val VIBE_GYM_HEX = 0xFFFF375F
private const val VIBE_STUDY_HEX = 0xFF5E5CE6
private const val VIBE_HOME_HEX = 0xFF32D74B
private const val VIBE_WORK_HEX = 0xFFFF9F0A

// Primary colors
val ElectricBlue = Color(ELECTRIC_BLUE_HEX)
val DeepPurple = Color(DEEP_PURPLE_HEX)
val SoftPink = Color(SOFT_PINK_HEX)
val VibrantTeal = Color(VIBRANT_TEAL_HEX)

// Dark theme colors
val RichBlack = Color(RICH_BLACK_HEX)
val DeepCharcoal = Color(DEEP_CHARCOAL_HEX)
val ElevatedDark = Color(ELEVATED_DARK_HEX)
val SoftGray = Color(SOFT_GRAY_HEX)
val LightGrayText = Color(LIGHT_GRAY_TEXT_HEX)
val PureWhite = Color(PURE_WHITE_HEX)

// Light theme colors
val SnowWhite = Color(SNOW_WHITE_HEX)
val LightBackground = Color(LIGHT_BACKGROUND_HEX)
val ElevatedLight = Color(ELEVATED_LIGHT_HEX)
val SoftBorder = Color(SOFT_BORDER_HEX)
val DarkText = Color(DARK_TEXT_HEX)

// System colors
val SuccessGreen = Color(SUCCESS_GREEN_HEX)
val ErrorRed = Color(ERROR_RED_HEX)
val WarningOrange = Color(WARNING_ORANGE_HEX)

// Vibe colors
val VibeGymColor = Color(VIBE_GYM_HEX)
val VibeStudyColor = Color(VIBE_STUDY_HEX)
val VibeHomeColor = Color(VIBE_HOME_HEX)
val VibeWorkColor = Color(VIBE_WORK_HEX)

// Premium gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, DeepPurple)
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(SoftPink, VibrantTeal)
)

val DarkGradient = Brush.verticalGradient(
    colors = listOf(DeepCharcoal, RichBlack)
)

val LightGradient = Brush.verticalGradient(
    colors = listOf(SnowWhite, LightBackground)
)

val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = PureWhite,
    primaryContainer = ElectricBlue.copy(alpha = 0.15f),
    onPrimaryContainer = ElectricBlue,
    secondary = DeepPurple,
    onSecondary = PureWhite,
    secondaryContainer = DeepPurple.copy(alpha = 0.15f),
    onSecondaryContainer = VibrantTeal,
    tertiary = SoftPink,
    onTertiary = PureWhite,
    background = RichBlack,
    onBackground = PureWhite,
    surface = DeepCharcoal,
    onSurface = PureWhite,
    surfaceVariant = ElevatedDark,
    onSurfaceVariant = LightGrayText,
    surfaceTint = ElectricBlue,
    error = ErrorRed,
    onError = PureWhite,
    outline = SoftGray,
    outlineVariant = SoftGray.copy(alpha = 0.5f),
    scrim = RichBlack.copy(alpha = 0.5f),
    inverseSurface = SnowWhite,
    inverseOnSurface = RichBlack,
    inversePrimary = ElectricBlue,
    surfaceContainer = ElevatedDark,
    surfaceContainerHigh = SoftGray,
    surfaceContainerHighest = SoftGray.copy(alpha = 0.8f),
    surfaceContainerLow = DeepCharcoal,
    surfaceContainerLowest = RichBlack
)

val LightColors = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = SnowWhite,
    primaryContainer = ElectricBlue.copy(alpha = 0.1f),
    onPrimaryContainer = ElectricBlue,
    secondary = DeepPurple,
    onSecondary = SnowWhite,
    secondaryContainer = DeepPurple.copy(alpha = 0.1f),
    onSecondaryContainer = DeepPurple,
    tertiary = SoftPink,
    onTertiary = SnowWhite,
    background = LightBackground,
    onBackground = DarkText,
    surface = ElevatedLight,
    onSurface = DarkText,
    surfaceVariant = LightBackground,
    onSurfaceVariant = DarkText.copy(alpha = 0.7f),
    surfaceTint = ElectricBlue,
    error = ErrorRed,
    onError = SnowWhite,
    outline = SoftBorder,
    outlineVariant = SoftBorder.copy(alpha = 0.5f),
    scrim = RichBlack.copy(alpha = 0.3f),
    inverseSurface = DeepCharcoal,
    inverseOnSurface = SnowWhite,
    inversePrimary = VibrantTeal,
    surfaceContainer = ElevatedLight,
    surfaceContainerHigh = SnowWhite,
    surfaceContainerHighest = SnowWhite,
    surfaceContainerLow = LightBackground,
    surfaceContainerLowest = SnowWhite
)

