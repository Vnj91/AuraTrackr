package com.example.auratrackr.features.workout.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.workout.viewmodel.WorkoutViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInProgressScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentWorkout = uiState.currentWorkout

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentWorkout?.title ?: "Loading...", fontWeight = FontWeight.Bold)
                        if (currentWorkout?.description?.isNotBlank() == true) {
                            Text(
                                currentWorkout.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Activity Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading || currentWorkout == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = "Start!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TimerCircle(
                        elapsedTime = uiState.elapsedTime,
                        progress = uiState.progress,
                        onPlayPauseClicked = viewModel::onPlayPauseClicked,
                        isTimerRunning = uiState.isTimerRunning
                    )

                    ControlPanel(
                        onMarkAsDoneClicked = viewModel::onMarkAsDoneClicked,
                        onSkipClicked = viewModel::onSkipClicked,
                        onResetClicked = viewModel::onResetClicked
                    )
                }
            }
        }
    }
}

@Composable
fun TimerCircle(
    elapsedTime: Long,
    progress: Float,
    isTimerRunning: Boolean,
    onPlayPauseClicked: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(1000), repeatMode = RepeatMode.Reverse),
        label = "PulseAnimation"
    )
    val buttonScale = if (isTimerRunning) pulse else 1f

    // ✅ FIX: Read theme colors here, in the composable scope.
    val progressTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val progressIndicatorColor = MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Use the color variables inside the non-composable DrawScope.
                drawCircle(
                    color = progressTrackColor,
                    style = Stroke(width = 20.dp.toPx())
                )
                drawArc(
                    color = progressIndicatorColor,
                    startAngle = -90f,
                    sweepAngle = 360 * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "%02d:%02d".format(elapsedTime / 60, elapsedTime % 60),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(
            onClick = onPlayPauseClicked,
            modifier = Modifier
                .scale(buttonScale)
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun ControlPanel(
    onMarkAsDoneClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = onMarkAsDoneClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "Mark As Done",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onResetClicked,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Timer")
                }
                Button(
                    onClick = onSkipClicked,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 16.dp)
                ) {
                    Text("Skip", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutInProgressScreenPreview() {
    AuraTrackrTheme(darkTheme = true) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    "Start!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TimerCircle(
                    elapsedTime = 75,
                    progress = 0.5f,
                    onPlayPauseClicked = {},
                    isTimerRunning = true
                )
                ControlPanel(
                    onMarkAsDoneClicked = {},
                    onSkipClicked = {},
                    onResetClicked = {}
                )
            }
        }
    }
}
