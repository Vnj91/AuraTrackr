package com.example.auratrackr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.auratrackr.R

// Defines the custom FontFamily by referencing the exact font file names
// from the res/font directory.
val MontserratAlternates = FontFamily(
    Font(R.font.montserratalternates_regular, FontWeight.Normal),
    Font(R.font.montserratalternates_medium, FontWeight.Medium),
    Font(R.font.montserratalternates_semibold, FontWeight.SemiBold),
    Font(R.font.montserratalternates_bold, FontWeight.Bold),
    Font(R.font.montserratalternates_black, FontWeight.Black),
    Font(R.font.montserratalternates_extrabold, FontWeight.ExtraBold),
    Font(R.font.montserratalternates_light, FontWeight.Light),
    Font(R.font.montserratalternates_extralight, FontWeight.ExtraLight),
    Font(R.font.montserratalternates_thin, FontWeight.Thin)
)

// Defines the app's complete typography, using the new font for headlines and titles.
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = MontserratAlternates,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    // Use a default, more readable font for body and label text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
