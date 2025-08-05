package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.R
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    // Animate the alpha (transparency) of the icon
    val iconAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), label = "IconAlpha"
    )

    // Animate the alpha of the text, but with a delay
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 500), label = "TextAlpha"
    )

    // Trigger the animation and the navigation timeout
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000) // Total time the splash screen is visible
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B2E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .systemBarsPadding(), // Handles padding for edge-to-edge display
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon (Stage 1)
            Image(
                painter = painterResource(id = R.drawable.ic_logo), // Ensure you have this drawable
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .alpha(iconAlpha)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title Text (Stage 2)
            Text(
                modifier = Modifier.alpha(textAlpha),
                text = buildAnnotatedString {
                    append("Unlock your\n")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Fitness Aura")
                    }
                },
                style = MaterialTheme.typography.displaySmall, // Uses Montserrat Alternates
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    AnimatedSplashScreen {}
}
