package com.example.auratrackr.features.focus.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
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
import kotlinx.coroutines.launch

@Composable
fun BlockingScreen(
    onClose: () -> Unit,
    onNavigateToTask: () -> Unit,
    viewModel: BlockingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BlockerEvent.SpendResult -> {
                    val message = if (event.result.isSuccess) "5 minutes added!" else "Failed: Not enough points."
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
                is BlockerEvent.UnblockGracePeriod -> {
                    onClose()
                }
            }
        }
    }

    // ✅ FIX: Added a Scaffold to host the Snackbar.
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    "Time's Up!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                // ✅ FIX: Improved text contrast for better readability.
                Text(
                    "You've reached your daily limit for this app. Time to refocus!",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

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

                // ✅ FIX: Replaced TextButton with a more prominent OutlinedButton.
                OutlinedButton(onClick = onClose) {
                    Text("Back to Home", fontWeight = FontWeight.Bold)
                }
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
    Button(
        onClick = { viewModel.spendPointsForExtraTime() },
        enabled = state.canAffordExtraTime,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
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
                    // ✅ FIX: Improved UX copy for the wait button.
                    Text("Take a short pause (${BlockingViewModel.WAIT_TIMER_DURATION_SECONDS}s)", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // ✅ FIX: Added a visual progress indicator to the countdown.
            Box(contentAlignment = Alignment.Center) {
                val progress by animateFloatAsState(
                    targetValue = timerValue.toFloat() / BlockingViewModel.WAIT_TIMER_DURATION_SECONDS,
                    animationSpec = tween(1000), label = "CountdownProgress"
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 2.dp
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
    AuraTrackrTheme(useDarkTheme = true) {
        BlockingScreen(onClose = {}, onNavigateToTask = {})
    }
}
