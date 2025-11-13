package com.example.auratrackr.features.onboarding.ui

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import com.example.auratrackr.ui.theme.AuraTrackrTheme

private data class RegisterValidation(
    val isLoading: Boolean,
    val isButtonEnabled: Boolean
)

@Composable
private fun rememberRegisterValidation(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    authState: AuthState
): RegisterValidation {
    val isUsernameValid by remember(username) { derivedStateOf { username.length >= 3 } }
    val isEmailValid by remember(email) { derivedStateOf { Patterns.EMAIL_ADDRESS.matcher(email).matches() } }
    val isPasswordValid by remember(password) { derivedStateOf { password.length >= 8 } }
    val passwordsMatch by remember(password, confirmPassword) { derivedStateOf { password == confirmPassword } }
    val isLoading = authState is AuthState.Loading
    val isFormValid = isUsernameValid && isEmailValid && isPasswordValid && passwordsMatch
    val isButtonEnabled = isFormValid && !isLoading
    
    return RegisterValidation(isLoading, isButtonEnabled)
}

// Layout constants for register screen
private val registerHorizontalPadding = 24.dp
private val registerTopSpacer = 32.dp
private val registerLogoSize = 60.dp
private val registerButtonHeight = 56.dp
private val registerCorner = 16.dp
private val registerFieldSpacing = 16.dp
private val registerBottomPadding = 24.dp

// Minimum touch target for small tappable controls
private val ICON_MIN_TOUCH = 48.dp

@Composable
fun RegisterScreen(
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

    val validation = rememberRegisterValidation(username, email, password, confirmPassword, authState)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = registerHorizontalPadding)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(registerTopSpacer))
            RegisterHeader()

            RegisterForm(
                state = RegisterFormState(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    hasAttemptedSubmit = hasAttemptedSubmit,
                    isLoading = validation.isLoading,
                    isButtonEnabled = validation.isButtonEnabled
                ),
                focusers = RegisterFormFocusers(
                    emailFocusRequester = emailFocusRequester,
                    passwordFocusRequester = passwordFocusRequester,
                    confirmPasswordFocusRequester = confirmPasswordFocusRequester
                ),
                callbacks = RegisterFormCallbacks(
                    onUsernameChange = { username = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    onSubmit = {
                        hasAttemptedSubmit = true
                        if (validation.isButtonEnabled) {
                            focusManager.clearFocus()
                            viewModel.register(email.trim(), username.trim(), password.trim())
                        }
                    }
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = registerBottomPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                RegisterFooter(onLoginClicked = onLoginClicked, isLoading = validation.isLoading)
            }
        }
    }
}

@Composable
private fun ColumnScope.RegisterHeader(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_logo),
        contentDescription = "AuraTrackr App Logo",
        modifier = modifier
            .size(registerLogoSize)
            .align(Alignment.Start)
    )

    Spacer(modifier = Modifier.height(registerFieldSpacing))

    Text(
        text = "Hello! Register to get started",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(registerTopSpacer))
}

@Composable
private fun RegisterFooter(onLoginClicked: () -> Unit, isLoading: Boolean) {
    TextButton(
        onClick = onLoginClicked,
        enabled = !isLoading,
        modifier = Modifier.sizeIn(minWidth = ICON_MIN_TOUCH, minHeight = ICON_MIN_TOUCH)
    ) {
        Text(
            "Login Now",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private data class RegisterFormState(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val hasAttemptedSubmit: Boolean,
    val isLoading: Boolean,
    val isButtonEnabled: Boolean
)

private data class RegisterFormFocusers(
    val emailFocusRequester: FocusRequester,
    val passwordFocusRequester: FocusRequester,
    val confirmPasswordFocusRequester: FocusRequester
)

private data class RegisterFormCallbacks(
    val onUsernameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit
)

@Composable
private fun ColumnScope.RegisterForm(
    state: RegisterFormState,
    focusers: RegisterFormFocusers,
    callbacks: RegisterFormCallbacks,
    modifier: Modifier = Modifier
) {
    val usernameError = if (state.hasAttemptedSubmit && state.username.length < 3) "Username must be at least 3 characters" else null

    AuthTextField(
        AuthTextFieldConfig(
            value = state.username,
            onValueChange = callbacks.onUsernameChange,
            label = "Username",
            keyboardType = KeyboardType.Text,
            isError = usernameError != null,
            supportingText = usernameError,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusers.emailFocusRequester.requestFocus() })
        )
    )

    Spacer(modifier = Modifier.height(registerFieldSpacing))

    val emailError = if (state.hasAttemptedSubmit && !Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) "Invalid email format" else null
    AuthTextField(
        AuthTextFieldConfig(
            value = state.email,
            onValueChange = callbacks.onEmailChange,
            label = "Email",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.focusRequester(focusers.emailFocusRequester),
            isError = emailError != null,
            supportingText = emailError,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusers.passwordFocusRequester.requestFocus() })
        )
    )

    Spacer(modifier = Modifier.height(registerFieldSpacing))

    val passwordError = if (state.hasAttemptedSubmit && state.password.length < 8) "Password must be at least 8 characters" else null
    AuthTextField(
        AuthTextFieldConfig(
            value = state.password,
            onValueChange = callbacks.onPasswordChange,
            label = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            modifier = Modifier.focusRequester(focusers.passwordFocusRequester),
            isError = passwordError != null,
            supportingText = passwordError,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusers.confirmPasswordFocusRequester.requestFocus() })
        )
    )

    Spacer(modifier = Modifier.height(registerFieldSpacing))

    val confirmPasswordError = when {
        state.hasAttemptedSubmit && state.confirmPassword != state.password -> "Passwords do not match"
        else -> null
    }
    AuthTextField(
        AuthTextFieldConfig(
            value = state.confirmPassword,
            onValueChange = callbacks.onConfirmPasswordChange,
            label = "Confirm password",
            keyboardType = KeyboardType.Password,
            isPassword = true,
            modifier = Modifier.focusRequester(focusers.confirmPasswordFocusRequester),
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { callbacks.onSubmit() })
        )
    )

    Spacer(modifier = Modifier.weight(1f, fill = false))
    Spacer(modifier = Modifier.height(registerTopSpacer))

    Button(
        onClick = callbacks.onSubmit,
        enabled = state.isButtonEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(registerButtonHeight),
        shape = RoundedCornerShape(registerCorner)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp
            )
        } else {
            Text("Register", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        RegisterScreen({})
    }
}
