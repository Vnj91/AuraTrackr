package com.example.auratrackr.features.onboarding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.Dimensions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Layout constants for OTP components
private val OTP_BOX_BORDER_WIDTH = 1.dp
private val OTP_BOX_CORNER = 12.dp
private val OTP_BUTTON_HEIGHT = 56.dp
private val OTP_BUTTON_CORNER = 16.dp
private val OTP_PROGRESS_SIZE = 24.dp
private val OTP_PROGRESS_STROKE = 3.dp
private val OTP_RESEND_PADDING = 24.dp
private const val OTP_DEFAULT_COUNT = 4
private const val OTP_FOCUS_DELAY_MS = 300L

/**
 * Data class to hold OTP text field configuration
 */
data class OtpTextFieldConfig(
    val otpText: String,
    val otpCount: Int = OTP_DEFAULT_COUNT,
    val isError: Boolean = false,
    val enabled: Boolean = true,
    val onOtpTextChange: (String, Boolean) -> Unit
)

/**
 * Custom OTP input field with individual digit boxes.
 * Extracted from OTPVerificationScreen to fix LongParameterList violation.
 */
@Composable
fun OtpTextField(
    config: OtpTextFieldConfig,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(OTP_FOCUS_DELAY_MS)
        focusRequester.requestFocus()
    }

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = config.otpText,
        onValueChange = {
            if (it.length <= config.otpCount && it.all { char -> char.isDigit() }) {
                config.onOtpTextChange(it, it.length == config.otpCount)
            }
        },
        enabled = config.enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        keyboardActions = KeyboardActions.Default,
        decorationBox = {
            OtpDigitBoxes(
                otpText = config.otpText,
                otpCount = config.otpCount,
                isError = config.isError
            )
        }
    )
}

/**
 * Row of individual digit input boxes for OTP.
 */
@Composable
private fun OtpDigitBoxes(
    otpText: String,
    otpCount: Int,
    isError: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)
    ) {
        repeat(otpCount) { index ->
            OtpDigitBox(
                char = otpText.getOrNull(index)?.toString() ?: "",
                isFocused = otpText.length == index,
                isError = isError
            )
        }
    }
}

/**
 * Single digit box for OTP input.
 */
@Composable
private fun OtpDigitBox(
    char: String,
    isFocused: Boolean,
    isError: Boolean
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        label = "OtpBorderColor"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .border(
                width = OTP_BOX_BORDER_WIDTH,
                color = borderColor,
                shape = RoundedCornerShape(OTP_BOX_CORNER)
            )
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(OTP_BOX_CORNER)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Verify button with loading state.
 */
@Composable
fun OtpVerifyButton(
    otpLength: Int,
    isLoading: Boolean,
    onVerifyClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onVerifyClicked,
        modifier = modifier
            .fillMaxWidth()
            .height(OTP_BUTTON_HEIGHT),
        shape = RoundedCornerShape(OTP_BUTTON_CORNER),
        enabled = otpLength == OTP_DEFAULT_COUNT && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(OTP_PROGRESS_SIZE),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = OTP_PROGRESS_STROKE
            )
        } else {
            Text(
                "Verify",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Resend code section with countdown timer.
 */
@Composable
fun OtpResendSection(
    timer: Int,
    isLoading: Boolean,
    onResendClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isResendEnabled = timer == 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(OTP_RESEND_PADDING),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Didn't receive code?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(
            onClick = onResendClicked,
            enabled = isResendEnabled && !isLoading
        ) {
            val resendText = if (isResendEnabled) "Resend" else "Resend in $timer s"
            Text(
                resendText,
                color = if (isResendEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Main OTP verification screen content with back button, title, OTP input, and action buttons.
 */
@Composable
private fun OtpHeader(
    onBackClicked: () -> Unit,
    isLoading: Boolean,
    otpTopSpacer: Dp
) {
    IconButton(
        onClick = onBackClicked,
        modifier = Modifier.padding(top = 16.dp).sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        enabled = !isLoading
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }

    Spacer(modifier = Modifier.height(otpTopSpacer))

    Text(
        text = "OTP Verification",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Enter the verification code we just sent to your email address.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyLarge
    )

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
fun OtpVerificationContent(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    error: String?,
    isLoading: Boolean,
    timer: Int,
    coroutineScope: CoroutineScope,
    onBackClicked: () -> Unit,
    onVerifyClicked: (String) -> Unit,
    onResendClicked: () -> Unit,
    otpTopSpacer: Dp,
    otpHorizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = otpHorizontalPadding)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        OtpHeader(
            onBackClicked = onBackClicked,
            isLoading = isLoading,
            otpTopSpacer = otpTopSpacer
        )

        OtpTextField(
            config = OtpTextFieldConfig(
                otpText = otpValue,
                onOtpTextChange = { value, isComplete ->
                    onOtpChange(value)
                    if (isComplete) {
                        focusManager.clearFocus()
                        onVerifyClicked(value)
                    }
                },
                isError = error != null,
                enabled = !isLoading
            )
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = Dimensions.Small)
                    .align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        OtpVerifyButton(
            otpLength = otpValue.length,
            isLoading = isLoading,
            onVerifyClicked = {
                focusManager.clearFocus()
                onVerifyClicked(otpValue)
            }
        )

        OtpResendSection(
            timer = timer,
            isLoading = isLoading,
            onResendClicked = {
                onResendClicked()
                coroutineScope.launch {
                    // Timer reset handled by parent
                }
            },
            modifier = Modifier.padding(vertical = 24.dp)
        )
    }
}
