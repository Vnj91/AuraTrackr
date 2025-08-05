package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auratrackr.R
import kotlin.math.roundToInt

// Enum to define the drag states for the swipeable button
private enum class DragValue { Start, End }

@Composable
fun FitnessOnboardingScreen(
    onLetsStartClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // --- BACKGROUND IMAGE ---
        // Replace R.drawable.your_background_image with your actual resource
        Image(
            painter = painterResource(id = R.drawable.onboardingscreenbg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- UI CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .systemBarsPadding() // Handles padding for edge-to-edge display
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Title with custom font
            Text(
                text = "Unlock your Fitness Aura",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall, // Uses Montserrat Alternates
                lineHeight = 44.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Start your fitness journey with our app's guidance and support.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Decorative Swirl Graphic
            // Replace R.drawable.swirl_arrow with your actual graphic
            Image(
                painter = painterResource(id = R.drawable.swirl_arrow),
                contentDescription = "Decorative swirl",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Water Tracker Card
            WaterTrackerCard()

            Spacer(modifier = Modifier.height(32.dp))

            // --- SWIPE TO START BUTTON (MATERIAL 3) ---
            SwipeToStartButtonM3(
                onSwiped = onLetsStartClicked
            )
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
            onSwiped()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF3A3850))
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
        } else {
            0f
        }
        val textAlpha by animateFloatAsState(targetValue = 1f - (dragProgress * 2), label = "")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
        }

        Text(
            text = "Lets start",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(textAlpha)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(state.requireOffset().roundToInt(), 0) }
                .size(60.dp)
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1B2E))
                .anchoredDraggable(state, Orientation.Horizontal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Swipe to Start",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


@Composable
fun WaterTrackerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.7f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Nightlight,
                contentDescription = "Drink goal",
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Drink",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
                Text(
                    text = "150 ml",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FitnessOnboardingScreenPreview() {
    FitnessOnboardingScreen {}
}
