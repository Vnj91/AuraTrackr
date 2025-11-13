package com.example.auratrackr.features.onboarding.ui

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
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
import androidx.compose.ui.focus.FocusManager
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
import com.example.auratrackr.features.onboarding.viewmodel.AuthState
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.ui.components.PremiumButton
import com.example.auratrackr.ui.components.PremiumGradientButton
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.PremiumAnimations
import com.example.auratrackr.ui.theme.pressAnimation

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
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
}

@Composable
private fun LoginHeader() {
    Column {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .size(LOGIN_LOGO_SIZE)
                .pressAnimation(),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "AuraTrackr Logo",
                    modifier = Modifier.size(LOGIN_LOGO_SIZE * 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome back!",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Glad to see you, Again!",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(LOGIN_TITLE_SPACING))
    }
}

@Composable
private fun ColumnScope.LoginForm(
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

    AnimatedVisibility(
        visible = !state.isLoading,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        PremiumGradientButton(
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
            gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
        ) {
            Text(
                "Login",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    
    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(LOGIN_BUTTON_HEIGHT),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        TextButton(
            onClick = onForgotPasswordClicked,
            modifier = modifier
                .sizeIn(minWidth = ICON_MIN_TOUCH, minHeight = ICON_MIN_TOUCH),
            enabled = !isLoading
        ) {
            Text(
                "Forgot Password?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
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

@Composable
private fun LoginEmailField(
    state: LoginFormState,
    callbacks: LoginFormCallbacks
) {
    val emailError = when {
        state.hasAttemptedSubmit && state.email.isBlank() -> "Email is required"
        state.hasAttemptedSubmit && !Patterns.EMAIL_ADDRESS.matcher(state.email).matches() -> "Invalid email format"
        else -> null
    }
    
    AuthTextField(
        config = AuthTextFieldConfig(
            value = state.email,
            onValueChange = callbacks.onEmailChange,
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            supportingText = emailError,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { callbacks.passwordFocusRequester.requestFocus() })
        )
    )
}

@Composable
private fun LoginPasswordField(
    state: LoginFormState,
    callbacks: LoginFormCallbacks,
    authState: AuthState
) {
    val passwordError = when {
        state.hasAttemptedSubmit && state.password.isBlank() -> "Password is required"
        authState is AuthState.Error -> authState.message
        else -> null
    }
    
    AuthTextField(
        config = AuthTextFieldConfig(
            value = state.password,
            onValueChange = callbacks.onPasswordChange,
            label = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            isError = passwordError != null,
            supportingText = passwordError,
            enabled = !state.isLoading,
            modifier = Modifier.focusRequester(callbacks.passwordFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                callbacks.onAttemptSubmit(true)
                if (state.isButtonEnabled) {
                    callbacks.focusManager.clearFocus()
                    callbacks.onSubmit()
                }
            })
        )
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        LoginScreen({}, {})
    }
}
