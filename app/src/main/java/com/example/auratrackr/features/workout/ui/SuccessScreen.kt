package com.example.auratrackr.features.workout.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.R
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

private val SUCCESS_KONFETTI_COLORS = listOf(0xFFfce18a, 0xFFff726d, 0xFFf4306d, 0xFFb48def).map { it.toInt() }
private const val SUCCESS_KONFETTI_MAX_SPEED = 20f
private const val SUCCESS_KONFETTI_DAMPING = 0.9f
private const val SUCCESS_KONFETTI_SPREAD = 360
private const val SUCCESS_KONFETTI_EMITTER_SECONDS = 2L
private const val SUCCESS_KONFETTI_PER_SECOND = 100

// Layout / haptic constants
private val SUCCESS_CONTENT_PADDING = 32.dp
private val SUCCESS_CARD_VERTICAL_PADDING = 48.dp
private val SUCCESS_CARD_HORIZONTAL_PADDING = 24.dp
private val SUCCESS_IMAGE_SIZE = 80.dp
private val SUCCESS_BUTTON_HEIGHT = 60.dp
private val SUCCESS_BUTTON_CORNER_RADIUS = 16.dp
private const val VIBRATION_DURATION_MS = 200L
private const val PARTY_POSITION_X = 0.5
private const val PARTY_POSITION_Y = 0.3

@Composable
fun SuccessScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    val party = remember {
        Party(
            speed = 0f,
            maxSpeed = SUCCESS_KONFETTI_MAX_SPEED,
            damping = SUCCESS_KONFETTI_DAMPING,
            spread = SUCCESS_KONFETTI_SPREAD,
            colors = SUCCESS_KONFETTI_COLORS,
            emitter = Emitter(duration = SUCCESS_KONFETTI_EMITTER_SECONDS, TimeUnit.SECONDS).perSecond(
                SUCCESS_KONFETTI_PER_SECOND
            ),
            position = Position.Relative(PARTY_POSITION_X, PARTY_POSITION_Y)
        )
    }

    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(
            VibrationEffect.createOneShot(VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SUCCESS_CONTENT_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    shape = RoundedCornerShape(SUCCESS_CARD_VERTICAL_PADDING / 2),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = SUCCESS_CARD_VERTICAL_PADDING,
                                horizontal = SUCCESS_CARD_HORIZONTAL_PADDING
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_flame),
                            contentDescription = null, // Decorative
                            modifier = Modifier.size(SUCCESS_IMAGE_SIZE)
                        )
                        Text(
                            text = stringResource(R.string.workout_success_title),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SUCCESS_BUTTON_HEIGHT),
                    shape = RoundedCornerShape(SUCCESS_BUTTON_CORNER_RADIUS)
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SuccessScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        SuccessScreen {}
    }
}
