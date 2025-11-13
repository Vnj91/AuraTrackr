package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.R
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.PremiumAnimations
import kotlinx.coroutines.async
import kotlinx.coroutines.delay

// Layout & animation constants for splash
private val SPLASH_HORIZONTAL_PADDING = 32.dp
private val SPLASH_IMAGE_SIZE = 100.dp
private val SPLASH_SPACER = 24.dp
private const val SPLASH_ANIM_DURATION = 1500

@Composable
fun AnimatedSplashScreen(
    splashDurationMillis: Long = 3000L,
    onTimeout: () -> Unit
) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.5f) }
    val rotation = remember { Animatable(0f) }
    
    // Shimmer effect
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LaunchedEffect(key1 = true) {
        // Launch all animations in parallel
        async {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        async {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        async {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(
                    durationMillis = 2000,
                    easing = FastOutSlowInEasing
                )
            )
        }
        delay(splashDurationMillis)
        currentOnTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SPLASH_HORIZONTAL_PADDING)
                    .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Outer glow container
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Glow effect
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .size(SPLASH_IMAGE_SIZE + 40.dp)
                            .scale(scale.value)
                            .alpha(shimmerAlpha * alpha.value),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    ) {}
                    
                    // Logo with rotation and scale
                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .size(SPLASH_IMAGE_SIZE + 20.dp)
                            .scale(scale.value)
                            .alpha(alpha.value),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 12.dp
                    ) {
                        androidx.compose.foundation.layout.Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "AuraTrackr Logo",
                                modifier = Modifier
                                    .size(SPLASH_IMAGE_SIZE * 0.6f)
                                    .rotate(rotation.value)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SPLASH_SPACER + 8.dp))

                Text(
                    modifier = Modifier
                        .alpha(alpha.value)
                        .scale(scale.value),
                    text = buildAnnotatedString {
                        append("Unlock your\n")
                        withStyle(
                            style = SpanStyle(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append("Fitness Aura")
                        }
                    },
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
fun AnimatedSplashScreenPreview() {
    // ✅ FIX: Corrected the parameter name from darkTheme to useDarkTheme
    AuraTrackrTheme(useDarkTheme = true) {
        AnimatedSplashScreen {}
    }
}
