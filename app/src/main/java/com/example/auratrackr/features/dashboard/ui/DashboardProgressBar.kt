package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PROGRESS_BAR_HEIGHT = 6.dp
private val PROGRESS_BAR_CORNER = 3.dp
private const val PROGRESS_BAR_ANIM_MS = 800
private const val PROGRESS_BG_ALPHA = 0.3f

@Composable
fun WorkoutProgressBar(progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(PROGRESS_BAR_ANIM_MS),
        label = "ProgressBarAnimation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PROGRESS_BAR_HEIGHT)
            .clip(RoundedCornerShape(PROGRESS_BAR_CORNER))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = PROGRESS_BG_ALPHA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(PROGRESS_BAR_HEIGHT)
                .background(color, RoundedCornerShape(PROGRESS_BAR_CORNER))
        )
    }
}
