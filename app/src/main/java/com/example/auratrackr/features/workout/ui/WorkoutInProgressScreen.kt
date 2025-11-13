package com.example.auratrackr.features.workout.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.auratrackr.features.workout.viewmodel.WorkoutSessionUiState
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.Dimensions

private val TIMER_CANVAS_SIZE_DP = 280.dp
private val TIMER_STROKE_WIDTH_DP = 20.dp
private const val TIMER_ANIM_MS = 1000
private val TIMER_BUTTON_SIZE_DP = 80.dp
private val TIMER_ICON_SIZE_DP = 48.dp
private val screenContentPadding = 32.dp
private val controlPanelPadding = 24.dp
private val defaultActionHeight = 56.dp
private val ICON_MIN_TOUCH = 48.dp

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
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.sizeIn(minWidth = ICON_MIN_TOUCH, minHeight = ICON_MIN_TOUCH)
                    ) {
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
                            .padding(Dimensions.Small)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        WorkoutContent(
            uiState = uiState,
            paddingValues = paddingValues,
            onPlayPauseClicked = viewModel::onPlayPauseClicked,
            onMarkAsDoneClicked = viewModel::onMarkAsDoneClicked,
            onSkipClicked = viewModel::onSkipClicked,
            onResetClicked = viewModel::onResetClicked
        )
    }
}

@Composable
fun WorkoutContent(
    uiState: WorkoutSessionUiState,
    paddingValues: PaddingValues,
    onPlayPauseClicked: () -> Unit,
    onMarkAsDoneClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    onResetClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            uiState.currentWorkout?.title ?: "Start!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        TimerCircle(
            elapsedTime = uiState.elapsedTime,
            progress = uiState.progress,
            onPlayPauseClicked = onPlayPauseClicked,
            isTimerRunning = uiState.isTimerRunning
        )
        ControlPanel(
            onMarkAsDoneClicked = onMarkAsDoneClicked,
            onSkipClicked = onSkipClicked,
            onResetClicked = onResetClicked
        )
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
        animationSpec = tween(durationMillis = TIMER_ANIM_MS),
        label = "ProgressAnimation"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(TIMER_ANIM_MS), repeatMode = RepeatMode.Reverse),
        label = "PulseAnimation"
    )
    val buttonScale = if (isTimerRunning) pulse else 1f

    val progressTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val progressIndicatorColor = MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(TIMER_CANVAS_SIZE_DP), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = progressTrackColor,
                    style = Stroke(width = TIMER_STROKE_WIDTH_DP.toPx())
                )
                drawArc(
                    color = progressIndicatorColor,
                    startAngle = -90f,
                    sweepAngle = 360 * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = TIMER_STROKE_WIDTH_DP.toPx(), cap = StrokeCap.Round)
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
                .size(TIMER_BUTTON_SIZE_DP)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(TIMER_ICON_SIZE_DP)
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
            modifier = Modifier.padding(controlPanelPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = onMarkAsDoneClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(defaultActionHeight),
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
                    modifier = Modifier.size(defaultActionHeight),
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
                        .height(defaultActionHeight)
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
    AuraTrackrTheme(useDarkTheme = true) {
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
