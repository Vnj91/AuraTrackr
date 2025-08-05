package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OTPVerificationScreen(
    onBackClicked: () -> Unit,
    onVerifyClicked: (String) -> Unit,
    onResendClicked: () -> Unit
) {
    var otpValue by remember { mutableStateOf("") }

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
                text = "OTP Verification",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium // Uses Montserrat Alternates
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter the verification code we just sent on your email address.",
                color = Color.Gray,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input Fields
            OtpTextField(
                otpText = otpValue,
                onOtpTextChange = { value, _ ->
                    otpValue = value
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Verify Button
            Button(
                onClick = { onVerifyClicked(otpValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50), // Fully rounded corners
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                enabled = otpValue.length == 4
            ) {
                Text("Verify", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Resend CTA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Didn't receive code?", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = onResendClicked) {
                    Text("Resend", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 4,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        delay(300) // Small delay to ensure UI is ready before requesting focus
    }

    BasicTextField(
        modifier = modifier,
        value = otpText,
        onValueChange = {
            if (it.length <= otpCount) {
                onOtpTextChange(it, it.length == otpCount)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(otpCount) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = otpText.length == index
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(60.dp)
                            .padding(horizontal = 4.dp)
                            .border(
                                width = 1.dp,
                                color = if (isFocused) Color.White else Color.Gray,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun OTPVerificationScreenPreview() {
    OTPVerificationScreen({}, {}, {})
}
