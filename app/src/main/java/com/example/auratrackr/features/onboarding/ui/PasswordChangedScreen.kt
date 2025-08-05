package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auratrackr.R

@Composable
fun PasswordChangedScreen(
    onBackToLoginClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B2E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding(), // Handles padding for edge-to-edge display
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar with Back Button
            IconButton(
                onClick = onBackToLoginClicked,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.Start)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Success Icon from design
            Image(
                painter = painterResource(id = R.drawable.ic_success_check), // Ensure you have this drawable
                contentDescription = "Success",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Confirmation Text with custom font
            Text(
                text = "Password Changed!",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium // Uses Montserrat Alternates
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your password has been changed successfully.",
                color = Color.Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.weight(1.5f))

            // Back to Login Button
            Button(
                onClick = onBackToLoginClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50), // Fully rounded corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text("Back to Login", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun PasswordChangedScreenPreview() {
    PasswordChangedScreen({})
}
