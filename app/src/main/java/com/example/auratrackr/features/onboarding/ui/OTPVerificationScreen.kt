package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.delay

// Layout constants
private val OTP_TOP_SPACER = 80.dp
private val OTP_HORIZONTAL_PADDING = 24.dp

@Composable
fun OTPVerificationScreen(
    isLoading: Boolean,
    error: String?,
    onBackClicked: () -> Unit,
    onVerifyClicked: (String) -> Unit,
    onResendClicked: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var timer by remember { mutableStateOf(30) }

    LaunchedEffect(key1 = timer) {
        if (timer > 0) {
            delay(1000L)
            timer--
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        OtpVerificationContent(
            otpValue = otpValue,
            onOtpChange = { otpValue = it },
            error = error,
            isLoading = isLoading,
            timer = timer,
            coroutineScope = coroutineScope,
            onBackClicked = onBackClicked,
            onVerifyClicked = onVerifyClicked,
            onResendClicked = {
                onResendClicked()
                timer = 30
            },
            otpTopSpacer = OTP_TOP_SPACER,
            otpHorizontalPadding = OTP_HORIZONTAL_PADDING
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OTPVerificationScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        OTPVerificationScreen(false, null, {}, {}, {})
    }
}
