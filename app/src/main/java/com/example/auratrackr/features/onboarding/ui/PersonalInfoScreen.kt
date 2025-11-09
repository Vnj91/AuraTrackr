package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.Dimensions
import kotlinx.coroutines.launch
import kotlin.math.abs

// Layout constants for PersonalInfoScreen
private val personalTopPadding = Dimensions.Large
private val personalHorizontalPadding = 24.dp
private val personalTopBarPadding = Dimensions.Large
private val personalPagerTopSpacer = 40.dp

object OnboardingDefaults {
    // Reduce sensitivity to make single-finger swipes more responsive but still avoid
    // accidental tiny movements. Previous value (20f) had coarse steps on some devices.
    const val RULER_DRAG_SENSITIVITY = 12f
    const val RULER_VIBRATION_DURATION_MS = 10L
    const val RULER_VIBRATION_AMPLITUDE = 50

    // Unit conversion constants to avoid magic numbers in the composables
    const val KG_TO_POUNDS = 2.20462
    const val CM_TO_INCHES = 2.54

    // Pager animation tuning constants
    const val PAGER_SCALE_FACTOR = 0.1f
    const val PAGER_ALPHA_FACTOR = 0.5f

    sealed class MeasurementUnit(val displayName: String) {
        data object Kilograms : MeasurementUnit("kg")
        data object Pounds : MeasurementUnit("lb")
        data object Centimeters : MeasurementUnit("cm")
        data object Inches : MeasurementUnit("in")
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PersonalInfoScreen(
    onFinished: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    var weightInKg by remember { mutableIntStateOf(70) }
    var heightInCm by remember { mutableIntStateOf(170) }

    var weightUnit by remember {
        mutableStateOf<OnboardingDefaults.MeasurementUnit>(
            OnboardingDefaults.MeasurementUnit.Kilograms
        )
    }
    var heightUnit by remember {
        mutableStateOf<OnboardingDefaults.MeasurementUnit>(
            OnboardingDefaults.MeasurementUnit.Centimeters
        )
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 0) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        },
        animationSpec = tween(durationMillis = 500),
        label = "BackgroundColor"
    )

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = personalTopBarPadding)) {
                TextButton(
                    onClick = { onFinished(weightInKg, heightInCm) },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Skip")
                }
            }
        },
        bottomBar = {
            OnboardingBottomNav(
                currentPage = pagerState.currentPage,
                onBack = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    } else {
                        onBack()
                    }
                },
                onNext = {
                    if (pagerState.currentPage < 1) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onFinished(weightInKg, heightInCm)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .systemBarsPadding()
                .padding(top = personalTopPadding, start = personalHorizontalPadding, end = personalHorizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)) {
                StepIndicator(isActive = pagerState.currentPage == 0)
                StepIndicator(isActive = pagerState.currentPage == 1)
            }
            Spacer(modifier = Modifier.height(personalPagerTopSpacer))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false,
                beyondBoundsPageCount = 1
            ) { page ->
                val pageOffset = abs(pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                val scale = 1f - (pageOffset * OnboardingDefaults.PAGER_SCALE_FACTOR)
                val alpha = 1f - (pageOffset * OnboardingDefaults.PAGER_ALPHA_FACTOR)

                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                ) {
                    when (page) {
                        0 -> WeightInputStep(
                            weightInKg = weightInKg,
                            onWeightChange = { weightInKg = it },
                            selectedUnit = weightUnit,
                            onUnitSelected = { weightUnit = it }
                        )
                        1 -> HeightInputStep(
                            heightInCm = heightInCm,
                            onHeightChange = { heightInCm = it },
                            selectedUnit = heightUnit,
                            onUnitSelected = { heightUnit = it }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonalInfoScreenPreview() {
    AuraTrackrTheme {
        PersonalInfoScreen(onFinished = { _, _ -> }, onBack = {})
    }
}
