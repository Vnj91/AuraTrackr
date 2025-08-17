package com.example.auratrackr.features.onboarding.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Read theme-dependent values directly in the Composable's scope.
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // ✅ FIX: Pass the theme color as a key to remember. This ensures the brush is
    // recreated only if the theme's error color changes.
    val errorBrush = remember(errorColor) { SolidColor(errorColor) }
    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFB985F1), Color(0xFF6F65E1))
        )
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    brush = if (isError) errorBrush else gradientBrush,
                    shape = RoundedCornerShape(16.dp)
                ),
            label = { Text(label) },
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                cursorColor = onSurfaceColor,
                focusedTextColor = onSurfaceColor,
                unfocusedTextColor = onSurfaceColor,
                unfocusedContainerColor = onSurfaceColor.copy(alpha = 0.05f),
                focusedContainerColor = onSurfaceColor.copy(alpha = 0.05f),
                errorContainerColor = onSurfaceColor.copy(alpha = 0.05f),
                disabledContainerColor = onSurfaceColor.copy(alpha = 0.03f),
                disabledTextColor = onSurfaceColor.copy(alpha = 0.5f),
                disabledLabelColor = onSurfaceColor.copy(alpha = 0.5f),
                focusedLabelColor = onSurfaceColor.copy(alpha = 0.6f),
                unfocusedLabelColor = onSurfaceColor.copy(alpha = 0.6f),
                errorLabelColor = errorColor
            ),
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions.copy(keyboardType = keyboardType),
            keyboardActions = keyboardActions,
            isError = isError,
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = if (isError) errorColor else onSurfaceVariantColor
                        )
                    }
                }
            }
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                color = if (isError) errorColor else onSurfaceVariantColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
