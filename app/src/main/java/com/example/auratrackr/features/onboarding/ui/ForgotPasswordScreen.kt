package com.example.auratrackr.features.onboarding.ui

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.ui.theme.AuraTrackrTheme

@Composable
fun ForgotPasswordScreen(
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isEmailValid by remember(email) {
        derivedStateOf { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    }
    val isLoading = authState is AuthState.Loading
    val isButtonEnabled = isEmailValid && !isLoading

    LaunchedEffect(authState) {
        val state = authState
        if (state is AuthState.Success && state.successMessage != null) {
            Toast.makeText(context, state.successMessage, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            onBackClicked()
        }
    }

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
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.padding(top = 16.dp),
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Forgot Password?",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Don't worry! It occurs. Please enter the email address linked with your account.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            val emailError = when {
                hasAttemptedSubmit && !isEmailValid -> "Please enter a valid email address."
                authState is AuthState.Error -> (authState as AuthState.Error).message
                else -> null
            }

            AuthTextField(
                config = AuthTextFieldConfig(
                    value = email,
                    onValueChange = { email = it },
                    label = "Enter your email",
                    keyboardType = KeyboardType.Email,
                    isError = emailError != null,
                    supportingText = emailError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        hasAttemptedSubmit = true
                        if (isButtonEnabled) {
                            viewModel.sendPasswordResetEmail(email)
                        }
                    }),
                    enabled = !isLoading
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    hasAttemptedSubmit = true
                    if (isButtonEnabled) {
                        viewModel.sendPasswordResetEmail(email)
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
                    Text("Send Reset Link", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
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
                    "Remember Password?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onLoginClicked, enabled = !isLoading) {
                    Text(
                        "Login",
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
fun ForgotPasswordScreenPreview() {
    // ✅ FIX: Corrected the parameter name from darkTheme to useDarkTheme
    AuraTrackrTheme(useDarkTheme = true) {
        ForgotPasswordScreen({}, {})
    }
}
