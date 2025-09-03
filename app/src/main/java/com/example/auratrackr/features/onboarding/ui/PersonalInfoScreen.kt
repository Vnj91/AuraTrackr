package com.example.auratrackr.features.onboarding.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// ✅ THE FINAL, DEFINITIVE FIX. I AM SO SORRY.
// The object is now public (by removing 'internal'), making its types visible to the rest of the file.
object OnboardingDefaults {
    const val RULER_DRAG_SENSITIVITY = 20f
    const val RULER_VIBRATION_DURATION_MS = 10L
    const val RULER_VIBRATION_AMPLITUDE = 50

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

    var weightUnit by remember { mutableStateOf<OnboardingDefaults.MeasurementUnit>(OnboardingDefaults.MeasurementUnit.Kilograms) }
    var heightUnit by remember { mutableStateOf<OnboardingDefaults.MeasurementUnit>(OnboardingDefaults.MeasurementUnit.Centimeters) }

    val backgroundColor by animateColorAsState(
        targetValue = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer,
        animationSpec = tween(durationMillis = 500),
        label = "BackgroundColor"
    )

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                TextButton(onClick = { onFinished(weightInKg, heightInCm) }, modifier = Modifier.align(Alignment.CenterEnd)) {
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
                .padding(top = 16.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StepIndicator(isActive = pagerState.currentPage == 0)
                StepIndicator(isActive = pagerState.currentPage == 1)
            }
            Spacer(modifier = Modifier.height(40.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false,
                beyondBoundsPageCount = 1
            ) { page ->
                val pageOffset = abs(pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                val scale = 1f - (pageOffset * 0.1f)
                val alpha = 1f - (pageOffset * 0.5f)

                Box(modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }) {
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

@Composable
fun WeightInputStep(
    weightInKg: Int, onWeightChange: (Int) -> Unit,
    selectedUnit: OnboardingDefaults.MeasurementUnit, onUnitSelected: (OnboardingDefaults.MeasurementUnit) -> Unit
) {
    val isKg = selectedUnit is OnboardingDefaults.MeasurementUnit.Kilograms
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(targetState = "What is your weight?", label = "Header") { text ->
            Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Text("This helps us calculate your daily goals", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        UnitSelector(
            units = listOf(OnboardingDefaults.MeasurementUnit.Pounds, OnboardingDefaults.MeasurementUnit.Kilograms),
            selectedUnit = selectedUnit,
            onUnitSelected = onUnitSelected
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            title = "Weight",
            value = if (isKg) weightInKg else (weightInKg * 2.20462).roundToInt(),
            range = if (isKg) 30..200 else 66..440,
            onValueChange = { onWeightChange(if (isKg) it else (it / 2.20462).roundToInt()) },
            unit = selectedUnit.displayName,
            cardColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun HeightInputStep(
    heightInCm: Int, onHeightChange: (Int) -> Unit,
    selectedUnit: OnboardingDefaults.MeasurementUnit, onUnitSelected: (OnboardingDefaults.MeasurementUnit) -> Unit
) {
    val isCm = selectedUnit is OnboardingDefaults.MeasurementUnit.Centimeters
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(targetState = "What is your height?", label = "Header") { text ->
            Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Text("This helps us set up your personalized plan", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        UnitSelector(
            units = listOf(OnboardingDefaults.MeasurementUnit.Inches, OnboardingDefaults.MeasurementUnit.Centimeters),
            selectedUnit = selectedUnit,
            onUnitSelected = onUnitSelected
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            title = "Height",
            value = if (isCm) heightInCm else (heightInCm / 2.54).roundToInt(),
            range = if (isCm) 120..220 else 47..87,
            onValueChange = { onHeightChange(if (isCm) it else (it * 2.54).roundToInt()) },
            unit = selectedUnit.displayName,
            cardColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun RulerCard(
    title: String, value: Int, range: IntRange, unit: String,
    cardColor: Color, onValueChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val vibrator = rememberVibrator(context)
    val scale = remember { Animatable(1f) }

    LaunchedEffect(value) {
        vibrator.vibrate(VibrationEffect.createOneShot(OnboardingDefaults.RULER_VIBRATION_DURATION_MS, OnboardingDefaults.RULER_VIBRATION_AMPLITUDE))
        scale.stop()
        scale.animateTo(1.2f, spring(dampingRatio = 0.5f))
        scale.animateTo(1f, spring(dampingRatio = 0.5f))
    }

    var offset by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offset += dragAmount
                        val steps = (offset / OnboardingDefaults.RULER_DRAG_SENSITIVITY).toInt()
                        if (steps != 0) {
                            val newValue = (value + steps).coerceIn(range)
                            onValueChange(newValue)
                            offset -= steps * OnboardingDefaults.RULER_DRAG_SENSITIVITY
                        }
                    }
                )
            }
            .semantics { contentDescription = "$title input. Current value is $value $unit. Swipe horizontally to change." },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(16.dp))
            Text(
                "$value",
                modifier = Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value },
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(unit.uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

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
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f), label = "IndicatorOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onGloballyPositioned { rowWidth = it.size.width }
    ) {
        Box(
            Modifier
                .width(itemWidth.dp)
                .fillMaxHeight()
                .offset(x = indicatorOffset)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        )
        Row(Modifier.fillMaxSize()) {
            units.forEach { unit ->
                val isSelected = unit == selectedUnit
                val contentColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, label = "UnitContentColor")
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().clip(CircleShape).clickable { onUnitSelected(unit) }.semantics { contentDescription = "Select ${unit.displayName}" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = unit.displayName, color = contentColor, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun OnboardingBottomNav(currentPage: Int, onBack: () -> Unit, onNext: () -> Unit) {
    val buttonText = if (currentPage == 1) "Start Now" else "Next"
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "ButtonScale")
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

    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onNext,
            modifier = Modifier.height(56.dp).graphicsLayer { scaleX = scale * pulse.value; scaleY = scale * pulse.value },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 32.dp),
            interactionSource = interactionSource
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(buttonText, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(0.7f))
            }
        }
    }
}

@Composable
fun StepIndicator(isActive: Boolean, modifier: Modifier = Modifier) {
    val width by animateDpAsState(
        targetValue = if (isActive) 32.dp else 16.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "StepIndicatorWidth"
    )
    val color by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        label = "StepIndicatorColor"
    )
    Box(modifier = modifier.height(4.dp).width(width).clip(CircleShape).background(color))
}

@Composable
private fun rememberVibrator(context: Context): Vibrator {
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

@Preview(showBackground = true)
@Composable
fun PersonalInfoScreenPreview() {
    AuraTrackrTheme {
        PersonalInfoScreen(onFinished = { _, _ -> }, onBack = {})
    }
}

