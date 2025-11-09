package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.R
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme

// Layout & animation constants for WelcomeScreen
private val WELCOME_HORIZONTAL_PADDING = 32.dp
private val WELCOME_IMAGE_SIZE = 100.dp
private val WELCOME_SMALL_SPACER = 24.dp
private val WELCOME_LARGE_SPACER = 48.dp
private val WELCOME_BUTTON_HEIGHT = 56.dp

@Composable
fun WelcomeScreen(
    onLoginClicked: () -> Unit,
    onRegisterClicked: () -> Unit,
    onContinueAsGuestClicked: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val isLoading = authState is AuthState.Loading

    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, delayMillis = 300)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = WELCOME_HORIZONTAL_PADDING)
                .systemBarsPadding()
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "AuraTrackr App Logo",
                modifier = Modifier.size(WELCOME_IMAGE_SIZE)
            )
            Spacer(modifier = Modifier.height(WELCOME_SMALL_SPACER))

            Text(
                text = buildAnnotatedString {
                    append("Unlock your\n")
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Fitness Aura")
                    }
                },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1.5f))

            WelcomeButtons(
                isLoading = isLoading,
                onLoginClicked = onLoginClicked,
                onRegisterClicked = onRegisterClicked
            )

            Spacer(modifier = Modifier.height(WELCOME_SMALL_SPACER))

            TextButton(onClick = onContinueAsGuestClicked, enabled = !isLoading) {
                Text(
                    "Continue as a guest",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = TextDecoration.Underline
                )
            }

            Spacer(modifier = Modifier.height(WELCOME_LARGE_SPACER))
        }
    }
}

@Composable
private fun WelcomeButtons(
    isLoading: Boolean,
    onLoginClicked: () -> Unit,
    onRegisterClicked: () -> Unit
) {
    OutlinedButton(
        onClick = onLoginClicked,
        modifier = Modifier
            .fillMaxWidth()
            .height(WELCOME_BUTTON_HEIGHT),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            "Login",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(WELCOME_SMALL_SPACER))

    Button(
        onClick = onRegisterClicked,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
        } else {
            Text(
                "Register",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    // ✅ FIX: Corrected the parameter name from darkTheme to useDarkTheme
    AuraTrackrTheme(useDarkTheme = true) {
        WelcomeScreen({}, {}, {})
    }
}
