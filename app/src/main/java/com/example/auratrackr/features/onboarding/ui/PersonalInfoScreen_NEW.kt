package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auratrackr.ui.components.PremiumGradientButton
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@Composable
fun PersonalInfoScreenNew(
    onFinished: (weight: Int, height: Int) -> Unit,
    onBack: () -> Unit
) {
    var weightInKg by remember { mutableIntStateOf(70) }
    var heightInCm by remember { mutableIntStateOf(170) }
    var currentStep by remember { mutableIntStateOf(0) } // 0 = weight, 1 = height
    
    // Entrance animations
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val titleAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "title_alpha"
    )

    val titleOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else -50f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "title_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                    )
                )
            )
            .systemBarsPadding()
    ) {
        // Back button
        IconButton(
            onClick = {
                if (currentStep == 0) {
                    onBack()
                } else {
                    currentStep = 0
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title section with animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = titleAlpha
                        translationY = titleOffset
                    }
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                // Icon with pulse animation
                val iconScale by animateFloatAsState(
                    targetValue = if (isVisible) 1f else 0.5f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "icon_scale"
                )
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentStep == 0) Icons.Default.FitnessCenter else Icons.Default.Height,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (currentStep == 0) "What's your weight?" else "How tall are you?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (currentStep == 0) 
                        "Swipe up or down to adjust" 
                    else 
                        "Almost there! Set your height",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Picker section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = currentStep == 0,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it }
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { -it }
                    ) + fadeOut()
                ) {
                    WeightPicker(
                        weight = weightInKg,
                        onWeightChange = { weightInKg = it }
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = currentStep == 1,
                    enter = slideInVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetY = { it }
                    ) + fadeIn(),
                    exit = slideOutVertically(
                        animationSpec = tween(300),
                        targetOffsetY = { -it }
                    ) + fadeOut()
                ) {
                    HeightPicker(
                        height = heightInCm,
                        onHeightChange = { heightInCm = it }
                    )
                }
            }

            // Continue button
            val buttonScale by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "button_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    }
            ) {
                PremiumGradientButton(
                    onClick = {
                        if (currentStep == 0) {
                            currentStep = 1
                        } else {
                            onFinished(weightInKg, heightInCm)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentStep == 0) "Continue" else "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WeightPicker(
    weight: Int,
    onWeightChange: (Int) -> Unit
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    
    val displayWeight by animateFloatAsState(
        targetValue = weight.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "weight_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        dragOffset = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        dragOffset += dragAmount
                        
                        val sensitivity = 15f
                        if (dragOffset.absoluteValue >= sensitivity) {
                            val newWeight = if (dragOffset < 0) {
                                (weight + 1).coerceIn(30, 200)
                            } else {
                                (weight - 1).coerceIn(30, 200)
                            }
                            if (newWeight != weight) {
                                onWeightChange(newWeight)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            dragOffset = 0f
                        }
                    }
                )
            }
    ) {
        // Scale indicators
        WeightScaleIndicators(
            currentWeight = displayWeight.roundToInt(),
            range = (displayWeight.roundToInt() - 2)..(displayWeight.roundToInt() + 2)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Main display
        Surface(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer {
                    val scale = 1f + (dragOffset.absoluteValue / 1000f).coerceIn(0f, 0.1f)
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = displayWeight.roundToInt().toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "kg",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Helper text with bounce animation
        Text(
            text = "↕ Swipe to adjust",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(0.6f)
        )
    }
}

@Composable
private fun HeightPicker(
    height: Int,
    onHeightChange: (Int) -> Unit
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    
    val displayHeight by animateFloatAsState(
        targetValue = height.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "height_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        dragOffset = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        dragOffset += dragAmount
                        
                        val sensitivity = 15f
                        if (dragOffset.absoluteValue >= sensitivity) {
                            val newHeight = if (dragOffset < 0) {
                                (height + 1).coerceIn(100, 250)
                            } else {
                                (height - 1).coerceIn(100, 250)
                            }
                            if (newHeight != height) {
                                onHeightChange(newHeight)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            dragOffset = 0f
                        }
                    }
                )
            }
    ) {
        // Scale indicators
        HeightScaleIndicators(
            currentHeight = displayHeight.roundToInt(),
            range = (displayHeight.roundToInt() - 3)..(displayHeight.roundToInt() + 3)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Main display
        Surface(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer {
                    val scale = 1f + (dragOffset.absoluteValue / 1000f).coerceIn(0f, 0.1f)
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = displayHeight.roundToInt().toString(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "cm",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "↕ Swipe to adjust",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(0.6f)
        )
    }
}

@Composable
private fun WeightScaleIndicators(
    currentWeight: Int,
    range: IntRange
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        range.forEach { weight ->
            val isSelected = weight == currentWeight
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.6f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "scale_$weight"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(200),
                label = "alpha_$weight"
            )
            
            Box(
                modifier = Modifier
                    .size(if (isSelected) 12.dp else 8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun HeightScaleIndicators(
    currentHeight: Int,
    range: IntRange
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        range.forEach { height ->
            val isSelected = height == currentHeight
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.6f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "scale_$height"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(200),
                label = "alpha_$height"
            )
            
            Box(
                modifier = Modifier
                    .size(if (isSelected) 12.dp else 8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = alpha))
            )
        }
    }
}
