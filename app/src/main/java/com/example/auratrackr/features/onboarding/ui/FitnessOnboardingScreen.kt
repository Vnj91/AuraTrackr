package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class DragValue { Start, End }

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

            // A dark scrim ensures text is readable over any background image.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 16.dp)
                    .systemBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(64.dp))

                // Text on overlays often uses static colors for maximum contrast.
                Text(
                    text = "Unlock your Fitness Aura",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Start your fitness journey with our app's guidance and support.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Image(
                    painter = painterResource(id = R.drawable.swirl_arrow),
                    contentDescription = null, // Decorative
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                WaterTrackerCard()
                Spacer(modifier = Modifier.height(32.dp))

                SwipeToStartButtonM3(
                    onSwiped = onLetsStartClicked
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToStartButtonM3(
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
            animationSpec = androidx.compose.animation.core.spring(),
        )
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

    val swipeButtonShape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(swipeButtonShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .onSizeChanged { size ->
                val anchors = DraggableAnchors {
                    DragValue.Start at 0f
                    DragValue.End at (size.width.toFloat() - with(density) { 60.dp.toPx() })
                }
                state.updateAnchors(anchors)
            }
    ) {
        val dragProgress = if (state.anchors.hasAnchorFor(DragValue.End)) {
            (state.offset / (state.anchors.positionOf(DragValue.End))).coerceIn(0f, 1f)
        } else 0f

        val textAlpha by animateFloatAsState(
            targetValue = (1f - dragProgress * 1.5f).coerceIn(0f, 1f),
            label = "TextAlphaAnimation"
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        }

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
                .size(60.dp)
                .shadow(4.dp, CircleShape)
                .padding(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .anchoredDraggable(state, Orientation.Horizontal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Swipe to Start",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
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
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Nightlight,
                contentDescription = "Drink goal icon",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
    AuraTrackrTheme(darkTheme = true) {
        FitnessOnboardingScreen {}
    }
}
