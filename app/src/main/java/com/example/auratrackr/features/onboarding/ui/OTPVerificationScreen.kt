package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
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
                shape = RoundedCornerShape(16.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpTextField(
    modifier: Modifier = Modifier,
    otpText: String,
    otpCount: Int = 4,
    onOtpTextChange: (String, Boolean) -> Unit
) {
    val focusRequesters = remember {
        (0 until otpCount).map { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        delay(100) // A small delay to ensure the UI is ready
        focusRequesters[0].requestFocus()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until otpCount) {
            OutlinedTextField(
                modifier = Modifier
                    .width(70.dp)
                    .height(60.dp)
                    .focusRequester(focusRequesters[i]),
                value = otpText.getOrNull(i)?.toString() ?: "",
                onValueChange = { value ->
                    if (value.length <= 1) {
                        val newOtp = otpText.toMutableList()
                        if (newOtp.size > i) {
                            if (value.isEmpty()) {
                                newOtp.removeAt(i)
                                if (i > 0) focusRequesters[i - 1].requestFocus()
                            } else {
                                newOtp[i] = value[0]
                                if (i < otpCount - 1) focusRequesters[i + 1].requestFocus()
                            }
                        } else {
                            if (value.isNotEmpty()){
                                newOtp.add(value[0])
                                if (i < otpCount - 1) focusRequesters[i + 1].requestFocus()
                            }
                        }
                        onOtpTextChange(newOtp.joinToString(""), newOtp.size == otpCount)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFB985F1),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            )
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun OTPVerificationScreenPreview() {
    OTPVerificationScreen({}, {}, {})
}
