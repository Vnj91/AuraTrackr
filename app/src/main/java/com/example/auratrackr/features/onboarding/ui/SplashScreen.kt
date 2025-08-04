package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auratrackr.R
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AnimatedSplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 1.5f,
        animationSpec = tween(durationMillis = 1000)
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 500) // Delay fade-in
    )

    // Trigger the animation and navigation
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500) // Total splash screen time
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B2E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon (Spash1 and Spash2)
            Image(
                painter = painterResource(id = R.drawable.ic_logo), // Your logo
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title Text (Spash2)
            Text(
                modifier = Modifier.alpha(alpha),
                text = buildAnnotatedString {
                    append("Unlock your\n")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                        append("fitness Aura")
                    }
                },
                color = Color.White,
                fontSize = 36.sp,
                lineHeight = 44.sp,
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
