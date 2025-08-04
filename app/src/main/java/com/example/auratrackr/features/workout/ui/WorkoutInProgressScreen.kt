package com.example.auratrackr.features.workout.ui

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.workout.viewmodel.WorkoutViewModel

private val DarkPurple = Color(0xFF1C1B2E)
private val CardPurple = Color(0xFF2C2B3C)
private val AccentYellow = Color(0xFFD4B42A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInProgressScreen(
    viewModel: WorkoutViewModel = hiltViewModel(),
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentWorkout = uiState.currentWorkout

    Scaffold(
        containerColor = DarkPurple,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentWorkout?.title ?: "Loading...", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(currentWorkout?.description ?: "", color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter, // Placeholder
                        contentDescription = "Activity Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardPurple)
                            .padding(8.dp),
                        tint = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkPurple)
            )
        }
    ) { paddingValues ->
        if (currentWorkout != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Text("Start!", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)

                // Timer
                TimerCircle(
                    elapsedTime = uiState.elapsedTime,
                    progress = uiState.progress,
                    onPlayPauseClicked = viewModel::onPlayPauseClicked,
                    isTimerRunning = uiState.isTimerRunning
                )

                // Control Panel
                ControlPanel(
                    onMarkAsDoneClicked = viewModel::onMarkAsDoneClicked,
                    onSkipClicked = viewModel::onSkipClicked,
                    onResetClicked = viewModel::onResetClicked
                )
            }
        } else {
            // This shows while the ViewModel is loading the initial schedule
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
    val formattedTime = remember(elapsedTime) {
        val minutes = (elapsedTime / 60).toString().padStart(2, '0')
        val seconds = (elapsedTime % 60).toString().padStart(2, '0')
        "$minutes:$seconds"
    }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "ProgressAnimation")

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                style = Stroke(width = 20.dp.toPx())
            )
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(
            text = formattedTime,
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = onPlayPauseClicked,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 40.dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentYellow)
        ) {
            Icon(
                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isTimerRunning) "Pause" else "Play",
                tint = Color.Black,
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onMarkAsDoneClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, DarkPurple),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkPurple)
            ) {
                Text("Mark As Done", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = DarkPurple)
                }
                Button(
                    onClick = onSkipClicked,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPurple),
                    modifier = Modifier.weight(1f).height(56.dp).padding(start = 16.dp)
                ) {
                    Text("Skip", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutInProgressScreenPreview() {
    WorkoutInProgressScreen(
        onBackClicked = {}
    )
}
