package com.example.auratrackr.features.focus.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.focus.viewmodel.BlockerEvent
import com.example.auratrackr.features.focus.viewmodel.BlockerUiState
import com.example.auratrackr.features.focus.viewmodel.BlockingViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BlockingScreen(
    onClose: () -> Unit,
    onNavigateToTask: () -> Unit,
    viewModel: BlockingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Observe one-off events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BlockerEvent.SpendResult -> {
                    val message = if (event.result.isSuccess) "5 minutes added!" else "Failed: Not enough points."
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                is BlockerEvent.UnblockGracePeriod -> {
                    onClose()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.TimerOff,
                contentDescription = "Time's Up Icon",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(80.dp)
            )

            Text(
                "Time's Up!",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "You've reached your daily limit for this app. Time to refocus!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Render buttons based on the current UI state
            when (val state = uiState) {
                is BlockerUiState.Ready -> {
                    BlockerActions(
                        state = state,
                        viewModel = viewModel,
                        onNavigateToTask = onNavigateToTask
                    )
                }
                is BlockerUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is BlockerUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp, modifier = Modifier.width(100.dp))

            TextButton(onClick = onClose) {
                Text("Back to Home", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BlockerActions(
    state: BlockerUiState.Ready,
    viewModel: BlockingViewModel,
    onNavigateToTask: () -> Unit
) {
    // Spend Points Button
    Button(
        onClick = { viewModel.spendPointsForExtraTime() },
        enabled = state.canAffordExtraTime,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = "Spend Points Icon")
            Text("Spend ${BlockingViewModel.EXTRA_TIME_COST} for 5 mins", fontWeight = FontWeight.Bold)
        }
    }

    // "Wait It Out" Button and Timer
    AnimatedContent(
        targetState = state.waitTimerSecondsRemaining,
        transitionSpec = {
            (slideInVertically { h -> h } + fadeIn(animationSpec = tween(220, 90)))
                .togetherWith(slideOutVertically { h -> -h } + fadeOut(animationSpec = tween(90)))
                .using(SizeTransform(clip = false))
        },
        label = "TimerAnimation"
    ) { timerValue ->
        if (timerValue == null) {
            OutlinedButton(
                onClick = { viewModel.startWaitTimer() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = "Wait Timer Icon")
                    Text("Wait for ${BlockingViewModel.WAIT_TIMER_DURATION_SECONDS} seconds", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Text(
                "Unlocking in: $timerValue seconds",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }

    // Aura Task Button
    OutlinedButton(
        onClick = onNavigateToTask,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Quiz, contentDescription = "Aura Task Icon")
            Text("Complete a task for 5 mins", fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
fun BlockingScreenPreview() {
    AuraTrackrTheme(darkTheme = true) {
        BlockingScreen(onClose = {}, onNavigateToTask = {})
    }
}