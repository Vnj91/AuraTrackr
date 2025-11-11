package com.example.auratrackr.features.onboarding.ui

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.FocusManager
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
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme

// Layout constants for login screen
private val LOGIN_HORIZONTAL_PADDING = 24.dp
private val LOGIN_TOP_SPACER = 48.dp
private val LOGIN_LOGO_SIZE = 60.dp
private val LOGIN_TITLE_SPACING = 16.dp
private val LOGIN_SECTION_SPACING = 32.dp
private val LOGIN_FIELD_SPACING = 16.dp
private val LOGIN_BUTTON_HEIGHT = 56.dp
private val LOGIN_BOTTOM_VERTICAL_PADDING = 24.dp

// Minimum touch target for small tappable controls
private val ICON_MIN_TOUCH = 48.dp

private data class LoginFormState(
    val email: String,
    val password: String,
    val hasAttemptedSubmit: Boolean,
    val isLoading: Boolean,
    val isButtonEnabled: Boolean
)

private data class LoginFormCallbacks(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onAttemptSubmit: (Boolean) -> Unit,
    val passwordFocusRequester: FocusRequester,
    val focusManager: FocusManager,
    val onSubmit: () -> Unit
)

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
                .padding(horizontal = LOGIN_HORIZONTAL_PADDING)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(LOGIN_TOP_SPACER))
            LoginHeader()

            Spacer(modifier = Modifier.height(LOGIN_SECTION_SPACING))

            LoginForm(
                state = LoginFormState(
                    email = email,
                    password = password,
                    hasAttemptedSubmit = hasAttemptedSubmit,
                    isLoading = isLoading,
                    isButtonEnabled = isButtonEnabled
                ),
                callbacks = LoginFormCallbacks(
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onAttemptSubmit = { hasAttemptedSubmit = it },
                    passwordFocusRequester = passwordFocusRequester,
                    focusManager = focusManager,
                    onSubmit = { viewModel.login(email.trim(), password.trim()) }
                ),
                authState = authState
            )
            LoginFooter(
                onRegisterClicked = onRegisterClicked,
                onForgotPasswordClicked = onForgotPasswordClicked,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun LoginHeader() {
    Column {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "AuraTrackr Logo",
            modifier = Modifier.size(LOGIN_LOGO_SIZE)
        )

        Spacer(modifier = Modifier.height(LOGIN_TITLE_SPACING))

        Text(
            text = "Welcome back! Glad to see you, Again!",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(LOGIN_TITLE_SPACING))
    }
}

@Composable
private fun LoginForm(
    state: LoginFormState,
    callbacks: LoginFormCallbacks,
    authState: AuthState,
    modifier: Modifier = Modifier
) {
    LoginEmailField(
        state = state,
        callbacks = callbacks
    )

    Spacer(modifier = Modifier.height(LOGIN_FIELD_SPACING))

    LoginPasswordField(
        state = state,
        callbacks = callbacks,
        authState = authState
    )

    Spacer(modifier = Modifier.weight(1f))

    Button(
        onClick = {
            callbacks.onAttemptSubmit(true)
            if (state.isButtonEnabled) {
                callbacks.focusManager.clearFocus()
                callbacks.onSubmit()
            }
        },
        enabled = state.isButtonEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(LOGIN_BUTTON_HEIGHT),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
        } else {
            Text("Login", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoginFooter(
    onRegisterClicked: () -> Unit,
    onForgotPasswordClicked: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onForgotPasswordClicked,
        modifier = modifier
            .align(Alignment.End)
            .sizeIn(minWidth = ICON_MIN_TOUCH, minHeight = ICON_MIN_TOUCH),
        enabled = !isLoading
    ) {
        Text(
            "Forgot Password?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LOGIN_BOTTOM_VERTICAL_PADDING),
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        LoginScreen({}, {})
    }
}
