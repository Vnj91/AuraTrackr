package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.auratrackr.R
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.Dimensions
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class DragValue { Start, End }

private val SWIPE_THUMB_SIZE = 60.dp

// Onboarding layout constants
private val ONBOARD_HORIZONTAL_PADDING = 32.dp
private val ONBOARD_VERTICAL_PADDING = 16.dp
private val TOP_SPACER_LARGE = 64.dp
private val ARROW_ICON_SIZE = 28.dp

// Water card constants
private val waterCardPadding = 20.dp
private val waterIconSize = 36.dp
private val waterIconInnerPadding = 6.dp
private val waterSpacerWidth = 12.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FitnessOnboardingScreen(
    onLetsStartClicked: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.onboardingscreenbg),
                contentDescription = "Woman working out in a gym",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)) // Slightly darker for better contrast
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = ONBOARD_HORIZONTAL_PADDING, vertical = ONBOARD_VERTICAL_PADDING)
                    .systemBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(TOP_SPACER_LARGE))

                Text(
                    text = "Unlock your Fitness Aura",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Start your fitness journey with our app's guidance and support.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                WaterTrackerCard()
                Spacer(modifier = Modifier.height(32.dp))

                SwipeToStartButton(
                    onSwiped = onLetsStartClicked
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToStartButton(
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val state = remember {
        AnchoredDraggableState(
            initialValue = DragValue.Start,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = spring()
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isDragging by interactionSource.collectIsDraggedAsState()

    LaunchedEffect(isDragging) {
        if (isDragging) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    LaunchedEffect(state.currentValue) {
        if (state.currentValue == DragValue.End) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSwiped()
            scope.launch {
                state.animateTo(DragValue.Start)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SWIPE_THUMB_SIZE)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .onSizeChanged { size ->
                val anchors = DraggableAnchors {
                    DragValue.Start at 0f
                    DragValue.End at (size.width.toFloat() - with(density) { SWIPE_THUMB_SIZE.toPx() })
                }
                state.updateAnchors(anchors)
            }
    ) {
        val dragProgress = if (state.anchors.hasAnchorFor(DragValue.End)) {
            (state.requireOffset() / (state.anchors.positionOf(DragValue.End))).coerceIn(0f, 1f)
        } else {
            0f
        }

        val textAlpha by animateFloatAsState(
            targetValue = (1f - dragProgress * 1.5f).coerceIn(0f, 1f),
            label = "TextAlphaAnimation"
        )

        Text(
            text = "Let’s start",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(textAlpha)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
                .size(SWIPE_THUMB_SIZE)
                .shadow(4.dp, CircleShape)
                .padding(Dimensions.Small)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .anchoredDraggable(state, Orientation.Horizontal, interactionSource = interactionSource),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Swipe to Start",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(ARROW_ICON_SIZE)
            )
        }
    }
}

@Composable
fun WaterTrackerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(0.7f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(waterCardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Nightlight,
                contentDescription = "Drink goal icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(waterIconSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .padding(waterIconInnerPadding)
            )
            Spacer(modifier = Modifier.width(waterSpacerWidth))
            Column {
                Text(
                    text = "Drink",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "150 ml",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FitnessOnboardingScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        FitnessOnboardingScreen {}
    }
}
