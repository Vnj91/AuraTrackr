package com.example.auratrackr.features.onboarding.ui

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.R
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.ui.theme.AuraTrackrTheme

@Composable
fun LoginScreen(
    onRegisterClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    val isEmailValid by remember(email) {
        derivedStateOf { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    }
    val isPasswordValid by remember(password) {
        derivedStateOf { password.length >= 8 }
    }
    val isLoading = authState is AuthState.Loading
    val isFormValid = isEmailValid && isPasswordValid
    val isButtonEnabled = isFormValid && !isLoading

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "AuraTrackr Logo",
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome back! Glad to see you, Again!",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            val emailError = if (hasAttemptedSubmit && !isEmailValid) "Invalid email format" else null
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Enter your email",
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                supportingText = emailError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    passwordFocusRequester.requestFocus()
                })
            )

            Spacer(modifier = Modifier.height(16.dp))

            val passwordError = when {
                hasAttemptedSubmit && !isPasswordValid -> "Password must be at least 8 characters"
                authState is AuthState.Error -> (authState as AuthState.Error).message
                else -> null
            }
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Enter your password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                modifier = Modifier.focusRequester(passwordFocusRequester),
                isError = passwordError != null,
                supportingText = passwordError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hasAttemptedSubmit = true
                    if (isButtonEnabled) {
                        focusManager.clearFocus()
                        viewModel.login(email.trim(), password.trim())
                    }
                })
            )

            TextButton(
                onClick = onForgotPasswordClicked,
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text(
                    "Forgot Password?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isButtonEnabled) {
                        focusManager.clearFocus()
                        viewModel.login(email.trim(), password.trim())
                    }
                },
                enabled = isButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Login", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onRegisterClicked, enabled = !isLoading) {
                    Text(
                        "Register Now",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // ✅ FIX: Corrected the parameter name from darkTheme to useDarkTheme
    AuraTrackrTheme(useDarkTheme = true) {
        LoginScreen({}, {})
    }
}
