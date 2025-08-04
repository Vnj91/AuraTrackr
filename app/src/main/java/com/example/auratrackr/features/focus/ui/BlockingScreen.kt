package com.example.auratrackr.features.focus.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TimerOff
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.focus.viewmodel.BlockingViewModel

@Composable
fun BlockingScreen(
    onClose: () -> Unit,
    viewModel: BlockingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // This effect listens for the result of the spend operation
    LaunchedEffect(uiState.spendResult) {
        uiState.spendResult?.let { result ->
            if (result.isSuccess) {
                Toast.makeText(context, "5 minutes added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed: Not enough points.", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearSpendResult()
        }
    }

    // This effect listens for the one-time unblock event
    LaunchedEffect(uiState.unblockEvent) {
        if (uiState.unblockEvent) {
            // TODO: Logic to unblock the app for 5 minutes
            viewModel.consumeUnblockEvent() // Reset the event
            onClose() // Close the overlay
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.TimerOff,
                contentDescription = "Time's Up",
                tint = Color.White,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Time's Up!",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "You've reached your daily limit for this app. Time to refocus!",
                color = Color.Gray,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Spend Points Button
            Button(
                onClick = { viewModel.spendPointsForExtraTime() },
                enabled = uiState.canAffordExtraTime,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4B42A), // Yellow
                    disabledContainerColor = Color.DarkGray
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spend ${BlockingViewModel.EXTRA_TIME_COST} for 5 mins",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // *** THIS IS THE UPDATE ***
            // "Wait It Out" Button and Timer
            AnimatedContent(
                targetState = uiState.waitTimerSecondsRemaining,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn()).togetherWith(
                        slideOutVertically { height -> -height } + fadeOut())
                        .using(SizeTransform(clip = false))
                }, label = "TimerAnimation"
            ) { timerValue ->
                if (timerValue == null) {
                    // State when timer is not started
                    Button(
                        onClick = { viewModel.startWaitTimer() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wait for ${BlockingViewModel.WAIT_TIMER_DURATION_SECONDS} seconds", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // State when timer is running
                    Text(
                        text = "Unlocking in: $timerValue",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Back to Home Button
            TextButton(onClick = onClose) {
                Text(
                    "Back to Home",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
fun BlockingScreenPreview() {
    BlockingScreen {}
}
