package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateNewPasswordScreen(
    onBackClicked: () -> Unit,
    onResetPasswordClicked: (String, String) -> Unit
) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B2E)
    ) {
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
                text = "Create new password",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium // Uses Montserrat Alternates
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your new password must be unique from those previously used.",
                color = Color.Gray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Input Fields
            AuthTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "New Password",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Reset Password Button
            Button(
                onClick = { onResetPasswordClicked(newPassword, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 48.dp),
                shape = RoundedCornerShape(50), // Fully rounded corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("Reset Password", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun CreateNewPasswordScreenPreview() {
    CreateNewPasswordScreen({}, { _, _ -> })
}
