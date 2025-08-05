package com.example.auratrackr.features.onboarding.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onBackClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // This effect listens for the success state and shows a message
    LaunchedEffect(uiState.isPasswordResetEmailSent) {
        if (uiState.isPasswordResetEmailSent) {
            Toast.makeText(context, "Password reset email sent!", Toast.LENGTH_LONG).show()
            viewModel.resetPasswordResetEmailSentState() // Reset the state to prevent re-triggering
            onBackClicked() // Navigate back after success
        }
    }

    // This effect listens for errors
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B2E)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .systemBarsPadding() // Handles padding for edge-to-edge display
            ) {
                // Top Bar with Back Button
                IconButton(
                    onClick = onBackClicked,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))

                // Header Section
                Text(
                    text = "Forgot Password?",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium // Uses Montserrat Alternates
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Don't worry! It occurs. Please enter the email address linked with your account.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Input Field
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.weight(1f))

                // Send Code Button
                Button(
                    onClick = { viewModel.sendPasswordResetEmail(email) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50), // Fully rounded corners
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Send Code", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Login CTA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Remember Password?", color = Color.Gray, fontSize = 14.sp)
                    TextButton(onClick = onLoginClicked) {
                        Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun ForgotPasswordScreenPreview() {
    ForgotPasswordScreen({}, {})
}
