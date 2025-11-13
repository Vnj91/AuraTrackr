package com.example.auratrackr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.PremiumAnimations
import com.example.auratrackr.ui.theme.pressAnimation

/**
 * Premium card with glassmorphism effect and elevated shadow
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

/**
 * Glassmorphism card with blur effect
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(1.dp, borderColor, shape)
            .clip(shape),
        color = backgroundColor,
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .blur(radius = 10.dp)
                .background(backgroundColor)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

/**
 * Premium gradient button with press animation
 */
@Composable
fun PremiumGradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    ),
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .pressAnimation(enabled = enabled)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(if (enabled) gradient else Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                ))
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}

/**
 * Premium elevated button with iOS-style design
 */
@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    elevation: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    val animatedElevation by animateDpAsState(
        targetValue = if (enabled) elevation else 0.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "ButtonElevation"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .pressAnimation(enabled = enabled)
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = backgroundColor.copy(alpha = 0.3f)
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding,
        shape = RoundedCornerShape(16.dp)
    ) {
        content()
    }
}

/**
 * Animated gradient background
 */
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
    )
) {
    var animationProgress by remember { mutableStateOf(0f) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = PremiumAnimations.smoothSpring,
        label = "GradientAnimation"
    )
    
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = colors,
                startY = 0f,
                endY = 1000f * (1 + animatedProgress * 0.2f)
            )
        )
    )
}

/**
 * Shimmer loading placeholder
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = shimmerColors
                )
            )
    )
}

/**
 * Premium divider with gradient
 */
@Composable
fun PremiumDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            Color.Transparent
        )
    )
) {
    Box(
        modifier = modifier
            .background(gradient)
            .padding(vertical = thickness / 2)
    )
}

/**
 * Floating action button with shadow and animation
 */
@Composable
fun PremiumFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .pressAnimation()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = backgroundColor.copy(alpha = 0.4f),
                spotColor = backgroundColor.copy(alpha = 0.4f)
            ),
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

/**
 * Premium chip/tag component
 */
@Composable
fun PremiumChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = if (selected) MaterialTheme.colorScheme.primaryContainer 
        else MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer 
        else MaterialTheme.colorScheme.onSurfaceVariant
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 350,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "ChipBackgroundColor"
    )
    
    val animatedContentColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 350,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "ChipContentColor"
    )
    
    Surface(
        onClick = onClick ?: {},
        modifier = modifier
            .then(if (onClick != null) Modifier.pressAnimation() else Modifier),
        color = animatedBackgroundColor,
        contentColor = animatedContentColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
