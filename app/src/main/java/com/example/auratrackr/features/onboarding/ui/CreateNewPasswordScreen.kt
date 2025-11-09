package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.ui.theme.AuraTrackrTheme

// Layout constants for CreateNewPasswordScreen
private val CREATE_PASSWORD_HORIZONTAL_PADDING = 24.dp
private val CREATE_PASSWORD_ICON_TOP_PADDING = 16.dp
private val CREATE_PASSWORD_TOP_SPACER = 80.dp
private val CREATE_PASSWORD_FIELD_SPACING = 16.dp
private val CREATE_PASSWORD_BUTTON_HEIGHT = 56.dp
private val CREATE_PASSWORD_BUTTON_BOTTOM_PADDING = 48.dp

@Composable
fun CreateNewPasswordScreen(
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onResetPasswordClicked: (String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val isPasswordLongEnough = newPassword.length >= 8
    val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()

    val newPasswordError = hasAttemptedSubmit && !isPasswordLongEnough
    val confirmPasswordError = hasAttemptedSubmit && !passwordsMatch

    val isButtonEnabled = isPasswordLongEnough && passwordsMatch && !isLoading

    val focusManager = LocalFocusManager.current
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CREATE_PASSWORD_HORIZONTAL_PADDING)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.padding(top = CREATE_PASSWORD_ICON_TOP_PADDING)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(CREATE_PASSWORD_TOP_SPACER))

            Text(
                text = "Create new password",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(CREATE_PASSWORD_FIELD_SPACING))

            Text(
                text = "Your new password must be at least 8 characters long.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            AuthTextField(
                AuthTextField(
                    AuthTextFieldConfig(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New Password",
                        keyboardType = KeyboardType.Password,
                        isPassword = true,
                        isError = newPasswordError,
                        supportingText = if (newPasswordError) "Password must be at least 8 characters" else null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { confirmPasswordFocusRequester.requestFocus() })
                    )
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                AuthTextFieldConfig(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    modifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                    isError = confirmPasswordError,
                    supportingText = if (confirmPasswordError) "Passwords do not match" else null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        hasAttemptedSubmit = true
                        if (isButtonEnabled) {
                            focusManager.clearFocus()
                            onResetPasswordClicked(newPassword)
                        }
                    })
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isButtonEnabled) {
                        onResetPasswordClicked(newPassword)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CREATE_PASSWORD_BUTTON_HEIGHT)
                    .padding(bottom = CREATE_PASSWORD_BUTTON_BOTTOM_PADDING),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        "Reset Password",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateNewPasswordScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        CreateNewPasswordScreen(isLoading = false, onBackClicked = {}, onResetPasswordClicked = {})
    }
}
