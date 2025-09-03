package com.example.auratrackr.features.live.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.live.viewmodel.LiveActivityEvent
import com.example.auratrackr.features.live.viewmodel.LiveActivityState
import com.example.auratrackr.features.live.viewmodel.LiveActivityViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * ✅ This screen has been completely rewritten to remove all Health Connect logic.
 * It now only reflects the simple Idle/Tracking state from the ViewModel.
 */
@Composable
fun LiveActivityScreen(
    viewModel: LiveActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // This effect listens for snackbar messages from the ViewModel.
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LiveActivityEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // The UI is now much simpler and directly shows the tracking content.
            LiveTrackingContent(
                liveActivityState = uiState.liveActivityState,
                onStartStopClicked = { viewModel.onStartStopClicked() }
            )

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun LiveTrackingContent(
    liveActivityState: LiveActivityState,
    onStartStopClicked: () -> Unit
) {
    val isTracking = liveActivityState == LiveActivityState.Tracking

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Informational Text
        Text(
            text = if (isTracking) "Live Activity in Progress" else "Ready to Go Live?",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isTracking) "AuraTrackr is tracking your session." else "Press the button below to start a live activity.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(64.dp))

        // Start/Stop Button
        Button(
            onClick = onStartStopClicked,
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTracking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isTracking) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isTracking) "Stop Tracking" else "Start Tracking",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isTracking) "Stop" else "Go Live",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Live Activity - Idle")
@Composable
fun LiveActivityScreenPreview_Idle() {
    AuraTrackrTheme {
        LiveTrackingContent(
            liveActivityState = LiveActivityState.Idle,
            onStartStopClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Live Activity - Tracking")
@Composable
fun LiveActivityScreenPreview_Tracking() {
    AuraTrackrTheme {
        LiveTrackingContent(
            liveActivityState = LiveActivityState.Tracking,
            onStartStopClicked = {}
        )
    }
}
