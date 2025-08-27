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
fun RegisterScreen(
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }

    val isUsernameValid by remember(username) { derivedStateOf { username.length >= 3 } }
    val isEmailValid by remember(email) { derivedStateOf { Patterns.EMAIL_ADDRESS.matcher(email).matches() } }
    val isPasswordValid by remember(password) { derivedStateOf { password.length >= 8 } }
    val passwordsMatch by remember(password, confirmPassword) { derivedStateOf { password == confirmPassword } }

    val isLoading = authState is AuthState.Loading
    val isFormValid = isUsernameValid && isEmailValid && isPasswordValid && passwordsMatch
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
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "AuraTrackr App Logo",
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Hello! Register to get started",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            val usernameError = if (hasAttemptedSubmit && !isUsernameValid) "Username must be at least 3 characters" else null
            AuthTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                keyboardType = KeyboardType.Text,
                isError = usernameError != null,
                supportingText = usernameError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { emailFocusRequester.requestFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))

            val emailError = if (hasAttemptedSubmit && !isEmailValid) "Invalid email format" else null
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                modifier = Modifier.focusRequester(emailFocusRequester),
                isError = emailError != null,
                supportingText = emailError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))

            val passwordError = if (hasAttemptedSubmit && !isPasswordValid) "Password must be at least 8 characters" else null
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                modifier = Modifier.focusRequester(passwordFocusRequester),
                isError = passwordError != null,
                supportingText = passwordError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { confirmPasswordFocusRequester.requestFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))

            val confirmPasswordError = when {
                hasAttemptedSubmit && !passwordsMatch -> "Passwords do not match"
                authState is AuthState.Error -> (authState as AuthState.Error).message
                else -> null
            }
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                modifier = Modifier.focusRequester(confirmPasswordFocusRequester),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    hasAttemptedSubmit = true
                    if (isButtonEnabled) {
                        focusManager.clearFocus()
                        viewModel.register(email.trim(), username.trim(), password.trim())
                    }
                })
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (isButtonEnabled) {
                        focusManager.clearFocus()
                        viewModel.register(email.trim(), username.trim(), password.trim())
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
                    Text("Register", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
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
                    "Already have an account?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onLoginClicked, enabled = !isLoading) {
                    Text(
                        "Login Now",
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
fun RegisterScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        RegisterScreen({}, {})
    }
}
