package com.example.auratrackr.features.focus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

// Layout constants for main blocking screen
private val BLOCKING_SCREEN_PADDING = 32.dp
private val BLOCKING_ICON_SIZE = 80.dp
private val BLOCKING_SPACER_LARGE = 24.dp
private val BLOCKING_SPACER_MEDIUM = 16.dp
private val BLOCKER_DIVIDER_WIDTH = 100.dp

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f)
    ) { paddingValues ->
        BlockingContent(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToTask = onNavigateToTask,
            onClose = onClose,
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun BlockingContent(
    uiState: BlockerUiState,
    viewModel: BlockingViewModel,
    onNavigateToTask: () -> Unit,
    onClose: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(BLOCKING_SCREEN_PADDING),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BLOCKING_SPACER_MEDIUM, Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.TimerOff,
                contentDescription = "Time's Up Icon",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(BLOCKING_ICON_SIZE)
            )

            Text(
                "Time's Up!",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                "You've reached your daily limit for this app. Time to refocus!",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(BLOCKING_SPACER_LARGE))

            when (val state = uiState) {
                is BlockerUiState.Ready -> {
                    BlockerActions(
                        state = state,
                        viewModel = viewModel,
                        onNavigateToTask = onNavigateToTask
                    )
                }
                is BlockerUiState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                }
                is BlockerUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(BLOCKING_SPACER_MEDIUM))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
                modifier = Modifier.width(BLOCKER_DIVIDER_WIDTH)
            )

            OutlinedButton(
                onClick = onClose,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("Close")
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
    SpendPointsButton(
        canAffordExtraTime = state.canAffordExtraTime,
        onSpendPoints = { viewModel.spendPointsForExtraTime() }
    )

    WaitTimerSection(
        waitTimerSecondsRemaining = state.waitTimerSecondsRemaining,
        onStartWaitTimer = { viewModel.startWaitTimer() }
    )

    CompleteTaskButton(onNavigateToTask = onNavigateToTask)
}

@Preview
@Composable
fun BlockingScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        BlockingScreen(onClose = {}, onNavigateToTask = {})
    }
}
