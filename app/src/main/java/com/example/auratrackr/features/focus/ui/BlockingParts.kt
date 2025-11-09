package com.example.auratrackr.features.focus.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.auratrackr.features.focus.viewmodel.BlockingViewModel
import com.example.auratrackr.ui.theme.Dimensions

// Layout constants for blocking UI parts
private val BLOCKER_BUTTON_CORNER = 16.dp
private val BLOCKER_BUTTON_VERTICAL_PADDING = Dimensions.Small
private val BLOCKER_CIRCULAR_SIZE = 80.dp
private val BLOCKER_TIMER_BOX_HEIGHT = 56.dp
private val BLOCKER_CIRCULAR_STROKE = 2.dp
private const val BLOCKER_TIMER_ANIM_MS = 1000
private const val BLOCKER_FADE_IN_DELAY_MS = 90
private const val BLOCKER_SLIDE_ANIM_MS = 220
private const val BLOCKER_FADE_OUT_MS = 90

/**
 * Button to spend aura points for extra time on the blocked app.
 */
@Composable
fun SpendPointsButton(
    canAffordExtraTime: Boolean,
    onSpendPoints: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSpendPoints,
        enabled = canAffordExtraTime,
        shape = RoundedCornerShape(BLOCKER_BUTTON_CORNER),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = BLOCKER_BUTTON_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)
        ) {
            Icon(Icons.Default.Star, contentDescription = "Spend Points Icon")
            Text("Spend ${BlockingViewModel.EXTRA_TIME_COST} for 5 mins", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Animated wait timer section with countdown progress indicator.
 * Shows start button when timer is not active, countdown when running.
 */
@Composable
fun WaitTimerSection(
    waitTimerSecondsRemaining: Int?,
    onStartWaitTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = waitTimerSecondsRemaining,
        transitionSpec = {
            (
                slideInVertically { h -> h } + fadeIn(
                    animationSpec = tween(BLOCKER_SLIDE_ANIM_MS, BLOCKER_FADE_IN_DELAY_MS)
                )
                )
                .togetherWith(slideOutVertically { h -> -h } + fadeOut(animationSpec = tween(BLOCKER_FADE_OUT_MS)))
                .using(SizeTransform(clip = false))
        },
        label = "TimerAnimation",
        modifier = modifier
    ) { timerValue ->
        if (timerValue == null) {
            OutlinedButton(
                onClick = onStartWaitTimer,
                shape = RoundedCornerShape(BLOCKER_BUTTON_CORNER),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = BLOCKER_BUTTON_VERTICAL_PADDING),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(BLOCKER_BUTTON_VERTICAL_PADDING)
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Wait Timer Icon")
                    Text(
                        "Take a short pause (${BlockingViewModel.WAIT_TIMER_DURATION_SECONDS}s)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.height(BLOCKER_TIMER_BOX_HEIGHT)) {
                val progress by animateFloatAsState(
                    targetValue = timerValue.toFloat() / BlockingViewModel.WAIT_TIMER_DURATION_SECONDS,
                    animationSpec = tween(BLOCKER_TIMER_ANIM_MS),
                    label = "CountdownProgress"
                )
                CircularProgressIndicator(
                    progress = { 1f - progress },
                    modifier = Modifier.size(BLOCKER_CIRCULAR_SIZE),
                    strokeWidth = BLOCKER_CIRCULAR_STROKE,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    "$timerValue",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Button to navigate to task completion for earning extra time.
 */
@Composable
fun CompleteTaskButton(
    onNavigateToTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onNavigateToTask,
        shape = RoundedCornerShape(BLOCKER_BUTTON_CORNER),
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary)
    ) {
        Row(
            modifier = Modifier.padding(vertical = Dimensions.Small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)
        ) {
            Icon(Icons.Default.Quiz, contentDescription = "Aura Task Icon")
            Text("Complete a task for 5 mins", fontWeight = FontWeight.Bold)
        }
    }
}
