package com.example.auratrackr.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * Premium iOS-inspired animation specifications
 */
object PremiumAnimations {
    
    // Spring animations - iOS-style bouncy feel
    val spring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val smoothSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val snappySpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    // Easing curves inspired by Apple's animations
    val appleEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
    val appleEaseInOut = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    val appleEaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    
    // Standard durations
    const val DURATION_SHORT = 250
    const val DURATION_MEDIUM = 350
    const val DURATION_LONG = 500
    
    // Tween animations with custom easing
    val smoothTween: AnimationSpec<Float> = tween(
        durationMillis = DURATION_MEDIUM,
        easing = appleEasing
    )
    
    val quickTween: AnimationSpec<Float> = tween(
        durationMillis = DURATION_SHORT,
        easing = FastOutSlowInEasing
    )
    
    val slowTween: AnimationSpec<Float> = tween(
        durationMillis = DURATION_LONG,
        easing = LinearOutSlowInEasing
    )
}

/**
 * Premium screen transition animations
 */
object PremiumTransitions {
    
    // Slide from right (iOS push)
    fun slideInFromRight(): EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEasing)
    ) + fadeIn(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    fun slideOutToLeft(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEasing)
    ) + fadeOut(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    // Slide from left (iOS pop)
    fun slideInFromLeft(): EnterTransition = slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEasing)
    ) + fadeIn(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    fun slideOutToRight(): ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEasing)
    ) + fadeOut(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    // Modal presentation (iOS modal)
    fun slideInFromBottom(): EnterTransition = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEaseOut)
    ) + fadeIn(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    fun slideOutToBottom(): ExitTransition = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEaseInOut)
    ) + fadeOut(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    // Scale transitions
    fun scaleInTransition(): EnterTransition = scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEasing)
    ) + fadeIn(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
    
    fun scaleOutTransition(): ExitTransition = scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(PremiumAnimations.DURATION_MEDIUM, easing = PremiumAnimations.appleEasing)
    ) + fadeOut(animationSpec = tween(PremiumAnimations.DURATION_SHORT))
}

/**
 * Haptic-style button press animation
 * Scales down the element on press, giving iOS-like tactile feedback
 */
@Suppress("ModifierFactoryUnreferencedReceiver")
fun Modifier.pressAnimation(
    pressScale: Float = 0.95f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    
    this.graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                awaitFirstDown(requireUnconsumed = false)
                scope.launch {
                    scale.animateTo(
                        targetValue = pressScale,
                        animationSpec = tween(
                            durationMillis = 100,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                
                val up = waitForUpOrCancellation()
                scope.launch {
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
            }
        }
    }
}

/**
 * Smooth hover effect for interactive elements
 */
@Suppress("ModifierFactoryUnreferencedReceiver")
fun Modifier.hoverEffect(
    scale: Float = 1.02f,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled) return@composed this
    
    val interactionSource = remember { MutableInteractionSource() }
    val animatedScale = remember { Animatable(1f) }
    
    this.graphicsLayer {
        scaleX = animatedScale.value
        scaleY = animatedScale.value
    }
}

/**
 * Shimmer loading effect
 */
@Composable
fun rememberShimmerAnimation(): Float {
    val shimmer = remember { Animatable(0f) }
    rememberCoroutineScope().launch {
        shimmer.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                animation = tween(1500, easing = LinearOutSlowInEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart
            )
        )
    }
    return shimmer.value
}
