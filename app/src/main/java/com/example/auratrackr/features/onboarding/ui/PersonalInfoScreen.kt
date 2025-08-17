package com.example.auratrackr.features.onboarding.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val RULER_DRAG_SENSITIVITY = 20f
private const val RULER_VIBRATION_DURATION_MS = 10L
private const val RULER_VIBRATION_AMPLITUDE = 50

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PersonalInfoScreen(
    onFinished: (Int, Int) -> Unit,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState { 2 } // The number of pages
    val coroutineScope = rememberCoroutineScope()

    var weightInKg by remember { mutableStateOf(70) }
    var heightInCm by remember { mutableStateOf(170) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            OnboardingBottomNav(
                currentPage = pagerState.currentPage,
                onBack = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else {
                        onBack()
                    }
                },
                onNext = {
                    if (pagerState.currentPage < 1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
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
                .padding(top = 32.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StepIndicator(isActive = pagerState.currentPage == 0)
                StepIndicator(isActive = pagerState.currentPage == 1)
            }
            Spacer(modifier = Modifier.height(48.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WeightInputStep(
                        initialWeight = weightInKg,
                        onWeightChange = { weightInKg = it }
                    )
                    1 -> HeightInputStep(
                        initialHeight = heightInCm,
                        onHeightChange = { heightInCm = it }
                    )
                }
            }
        }
    }
}

@Composable
fun WeightInputStep(initialWeight: Int, onWeightChange: (Int) -> Unit) {
    var selectedUnit by remember { mutableStateOf("kg") }
    val isKg = selectedUnit == "kg"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "What is your weight?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        UnitSelector(
            units = listOf("lb", "kg"),
            selectedUnit = selectedUnit,
            onUnitSelected = { selectedUnit = it }
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            title = "Weight",
            value = if (isKg) initialWeight else (initialWeight * 2.20462).roundToInt(),
            range = if (isKg) 30..200 else 66..440,
            onValueChange = {
                onWeightChange(if (isKg) it else (it / 2.20462).roundToInt())
            },
            unit = selectedUnit,
            cardColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
fun HeightInputStep(initialHeight: Int, onHeightChange: (Int) -> Unit) {
    var selectedUnit by remember { mutableStateOf("cm") }
    val isCm = selectedUnit == "cm"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "What is your height?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))
        UnitSelector(
            units = listOf("in", "cm"),
            selectedUnit = selectedUnit,
            onUnitSelected = { selectedUnit = it }
        )
        Spacer(modifier = Modifier.height(32.dp))
        RulerCard(
            title = "Height",
            value = if (isCm) initialHeight else (initialHeight / 2.54).roundToInt(),
            range = if (isCm) 120..220 else 47..87,
            onValueChange = {
                onHeightChange(if (isCm) it else (it * 2.54).roundToInt())
            },
            unit = selectedUnit,
            cardColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    }
}

@Composable
fun RulerCard(
    title: String,
    value: Int,
    range: IntRange,
    unit: String,
    cardColor: Color,
    onValueChange: (Int) -> Unit
) {
    val context = LocalContext.current
    // ✅ FIX: Use the modern VibratorManager on API 31+ and fallback to the deprecated
    // Vibrator service on older versions to resolve the warning.
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    var offset by remember { mutableFloatStateOf(0f) }
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(300),
        label = "animatedValue"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { offset = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offset += dragAmount
                        val steps = (offset / RULER_DRAG_SENSITIVITY).roundToInt()
                        if (steps != 0) {
                            val newValue = (value + steps).coerceIn(range)
                            if (newValue != value) {
                                onValueChange(newValue)
                                vibrator.vibrate(
                                    VibrationEffect.createOneShot(
                                        RULER_VIBRATION_DURATION_MS,
                                        RULER_VIBRATION_AMPLITUDE
                                    )
                                )
                            }
                            offset = 0f
                        }
                    }
                )
            }
            .semantics {
                contentDescription = "$title input. Current value is $value $unit. Swipe horizontally to change."
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(16.dp))

            AnimatedContent(
                targetState = animatedValue,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 2 } + fadeOut())
                }, label = "valueAnimation"
            ) { target ->
                Text(
                    "$target",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                unit.uppercase(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun UnitSelector(
    units: List<String>,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp)
    ) {
        units.forEach { unit ->
            val isSelected = unit == selectedUnit
            val backgroundColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "UnitSelectorBackground"
            )
            val contentColor by animateColorAsState(
                if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, label = "UnitSelectorContent"
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable { onUnitSelected(unit) }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .semantics { contentDescription = "Select $unit" },
                contentAlignment = Alignment.Center
            ) {
                Text(text = unit, color = contentColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun OnboardingBottomNav(
    currentPage: Int,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val buttonText = if (currentPage == 1) "Start Now" else "Next"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go to previous step",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(
            onClick = onNext,
            modifier = Modifier.height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    buttonText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary.copy(0.7f))
            }
        }
    }
}

@Composable
fun StepIndicator(isActive: Boolean) {
    val width by animateDpAsState(targetValue = if (isActive) 32.dp else 16.dp, label = "StepIndicatorWidth")
    val color by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "StepIndicatorColor"
    )
    Box(
        modifier = Modifier
            .height(4.dp)
            .width(width)
            .clip(CircleShape)
            .background(color)
    )
}

@Preview(showBackground = true)
@Composable
fun PersonalInfoScreenPreview() {
    AuraTrackrTheme {
        PersonalInfoScreen(onFinished = { _, _ -> }, onBack = {})
    }
}
