package com.example.auratrackr.features.onboarding.ui

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.Dimensions
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Configuration data class for RulerCard component.
 */
data class RulerCardConfig(
    val title: String,
    val value: Int,
    val range: IntRange,
    val unit: String,
    val cardColor: Color,
    val onValueChange: (Int) -> Unit
)

/**
 * Weight input step for personal info onboarding.
 */
@Composable
fun WeightInputStep(
    weightInKg: Int,
    onWeightChange: (Int) -> Unit,
    selectedUnit: OnboardingDefaults.MeasurementUnit,
    onUnitSelected: (OnboardingDefaults.MeasurementUnit) -> Unit
) {
    val isKg = selectedUnit is OnboardingDefaults.MeasurementUnit.Kilograms
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(targetState = "What is your weight?", label = "Header") { text ->
            Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            "This helps us calculate your daily goals",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        UnitSelector(
            units = listOf(OnboardingDefaults.MeasurementUnit.Pounds, OnboardingDefaults.MeasurementUnit.Kilograms),
            selectedUnit = selectedUnit,
            onUnitSelected = onUnitSelected
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            RulerCardConfig(
                title = "Weight",
                value = if (isKg) weightInKg else (weightInKg * OnboardingDefaults.KG_TO_POUNDS).roundToInt(),
                range = if (isKg) 30..200 else 66..440,
                onValueChange = {
                    onWeightChange(
                        if (isKg) it else (it / OnboardingDefaults.KG_TO_POUNDS).roundToInt()
                    )
                },
                unit = selectedUnit.displayName,
                cardColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

/**
 * Height input step for personal info onboarding.
 */
@Composable
fun HeightInputStep(
    heightInCm: Int,
    onHeightChange: (Int) -> Unit,
    selectedUnit: OnboardingDefaults.MeasurementUnit,
    onUnitSelected: (OnboardingDefaults.MeasurementUnit) -> Unit
) {
    val isCm = selectedUnit is OnboardingDefaults.MeasurementUnit.Centimeters
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(targetState = "What is your height?", label = "Header") { text ->
            Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            "This helps us set up your personalized plan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        UnitSelector(
            units = listOf(OnboardingDefaults.MeasurementUnit.Inches, OnboardingDefaults.MeasurementUnit.Centimeters),
            selectedUnit = selectedUnit,
            onUnitSelected = onUnitSelected
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            RulerCardConfig(
                title = "Height",
                value = if (isCm) heightInCm else (heightInCm / OnboardingDefaults.CM_TO_INCHES).roundToInt(),
                range = if (isCm) 120..220 else 47..87,
                onValueChange = {
                    onHeightChange(
                        if (isCm) it else (it * OnboardingDefaults.CM_TO_INCHES).roundToInt()
                    )
                },
                unit = selectedUnit.displayName,
                cardColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

/**
 * Interactive ruler card with drag gesture for value selection.
 */
@Composable
private fun RulerCardContent(config: RulerCardConfig) {
    val title = config.title
    val value = config.value
    val range = config.range
    val unit = config.unit
    val onValueChange = config.onValueChange

    val vibrator = rememberVibrator(androidx.compose.ui.platform.LocalContext.current)
    var currentScale by remember { mutableFloatStateOf(1f) }
    val scaleValue by animateFloatAsState(currentScale, label = "Scale")

    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(220.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { currentScale = 1.02f },
                    onDragEnd = { currentScale = 1f },
                    onDragCancel = { currentScale = 1f }
                ) { change, dragAmount ->
                    change.consume()
                    val deltaX = dragAmount.x
                    if (abs(deltaX) > OnboardingDefaults.RULER_DRAG_SENSITIVITY) {
                        val newValue = value + (if (deltaX > 0) 1 else -1)
                        if (newValue in range) {
                            onValueChange(newValue)
                            vibrator.vibrate(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    android.os.VibrationEffect.createOneShot(
                                        OnboardingDefaults.RULER_VIBRATION_DURATION_MS,
                                        OnboardingDefaults.RULER_VIBRATION_AMPLITUDE
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator.vibrate(OnboardingDefaults.RULER_VIBRATION_DURATION_MS)
                                    null
                                } ?: return@detectDragGestures
                            )
                        }
                    }
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = config.cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        RulerCardDisplay(title, value, unit, scaleValue)
    }
}

/**
 * Display content for ruler card showing current value.
 */
@Composable
private fun RulerCardDisplay(title: String, value: Int, unit: String, scaleValue: Float) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                value.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    scaleX = scaleValue
                    scaleY = scaleValue
                }
            )
            Text(
                unit.uppercase(),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Public wrapper for RulerCard component.
 */
@Composable
fun RulerCard(config: RulerCardConfig) {
    RulerCardContent(config)
}

/**
 * Unit selector with animated indicator for measurement units.
 */
@Composable
fun UnitSelector(
    units: List<OnboardingDefaults.MeasurementUnit>,
    selectedUnit: OnboardingDefaults.MeasurementUnit,
    onUnitSelected: (OnboardingDefaults.MeasurementUnit) -> Unit
) {
    var rowWidth by remember { mutableStateOf(0) }
    val itemWidth = if (units.isNotEmpty()) rowWidth / units.size else 0
    val selectedIndex = units.indexOf(selectedUnit)

    val indicatorOffset by animateDpAsState(
        targetValue = (itemWidth * selectedIndex).dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "IndicatorOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onGloballyPositioned { rowWidth = it.size.width }
    ) {
        val density = LocalDensity.current
        val itemWidthDp = with(density) { (if (units.isNotEmpty()) rowWidth / units.size else 0).toDp() }
        Box(
            Modifier
                .width(itemWidthDp)
                .fillMaxHeight()
                .offset(x = indicatorOffset)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        )
        Row(Modifier.fillMaxSize()) {
            units.forEach { unit ->
                val isSelected = unit == selectedUnit
                val contentColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    label = "UnitContentColor"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable { onUnitSelected(unit) }
                        .semantics { contentDescription = "Select ${unit.displayName}" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = unit.displayName, color = contentColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Bottom navigation for onboarding with back and next buttons.
 */
@Composable
fun OnboardingBottomNav(currentPage: Int, onBack: () -> Unit, onNext: () -> Unit) {
    val buttonText = if (currentPage == 1) "Start Now" else "Next"
    val pulse = rememberButtonPulseAnimation(currentPage)

    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OnboardingBackButton(onBack)
        OnboardingNextButton(onNext, buttonText, pulse)
    }
}

@Composable
private fun rememberButtonPulseAnimation(currentPage: Int): Animatable<Float, AnimationVector1D> {
    val pulse = remember { Animatable(1f) }
    LaunchedEffect(currentPage) {
        if (currentPage == 1) {
            repeat(2) {
                pulse.animateTo(1.05f, tween(400))
                pulse.animateTo(1f, tween(400))
            }
        } else {
            pulse.snapTo(1f)
        }
    }
    return pulse
}

@Composable
private fun OnboardingBackButton(onBack: () -> Unit) {
    IconButton(
        onClick = onBack,
        modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Go back",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnboardingNextButton(
    onNext: () -> Unit,
    buttonText: String,
    pulse: Animatable<Float, AnimationVector1D>
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "ButtonScale")

    Button(
        onClick = onNext,
        modifier = Modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale * pulse.value
                scaleY = scale * pulse.value
            },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 32.dp),
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)
        ) {
            Text(
                buttonText,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(0.7f)
            )
        }
    }
}

/**
 * Step indicator dot with animated width and color.
 */
@Composable
fun StepIndicator(isActive: Boolean, modifier: Modifier = Modifier) {
    val width by animateDpAsState(
        targetValue = if (isActive) 32.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "StepIndicatorWidth"
    )
    val color by animateColorAsState(
        targetValue = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.8f
            )
        },
        label = "StepIndicatorColor"
    )
    Box(modifier = modifier.height(4.dp).width(width).clip(CircleShape).background(color))
}

/**
 * Helper to get system vibrator for haptic feedback.
 */
@Composable
fun rememberVibrator(context: Context): Vibrator {
    return remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
