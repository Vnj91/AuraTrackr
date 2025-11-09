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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private val AUTH_GRADIENT_START = Color(0xFFB985F1)
private val AUTH_GRADIENT_END = Color(0xFF6F65E1)
private val fieldCornerRadius = 16.dp

// Layout constants
private val supportingTextPaddingStart = 16.dp
private val supportingTextPaddingTop = 4.dp
private val outlinedBorderWidth = 1.dp

// Minimum touch target for small icons inside text fields
private val ICON_MIN_TOUCH = 48.dp

data class AuthTextFieldConfig(
    val value: String,
    val onValueChange: (String) -> Unit,
    val label: String,
    val modifier: Modifier = Modifier,
    val enabled: Boolean = true,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    val keyboardActions: KeyboardActions = KeyboardActions.Default,
    val isPassword: Boolean = false,
    val isError: Boolean = false,
    val supportingText: String? = null
)

@Composable
private fun rememberAuthTextFieldColors(onSurfaceColor: Color, errorColor: Color) = OutlinedTextFieldDefaults.colors(
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
)

@Composable
fun AuthTextField(config: AuthTextFieldConfig) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Read theme-dependent values directly in the Composable's scope.
    val errorColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val errorBrush = remember(errorColor) { SolidColor(errorColor) }
    val gradientBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(AUTH_GRADIENT_START, AUTH_GRADIENT_END)
        )
    }

    Column(modifier = config.modifier) {
        OutlinedTextField(
            value = config.value,
            onValueChange = config.onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = outlinedBorderWidth,
                    brush = if (config.isError) errorBrush else gradientBrush,
                    shape = RoundedCornerShape(fieldCornerRadius)
                ),
            label = { Text(config.label) },
            enabled = config.enabled,
            shape = RoundedCornerShape(fieldCornerRadius),
            colors = rememberAuthTextFieldColors(onSurfaceColor, errorColor),
            singleLine = true,
            visualTransformation = if (config.isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = config.keyboardOptions.copy(keyboardType = config.keyboardType),
            keyboardActions = config.keyboardActions,
            isError = config.isError,
            trailingIcon = {
                if (config.isPassword) {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.sizeIn(minWidth = ICON_MIN_TOUCH, minHeight = ICON_MIN_TOUCH)
                    ) {
                        Icon(
                            imageVector = image,
                            contentDescription = description,
                            tint = if (config.isError) errorColor else onSurfaceVariantColor
                        )
                    }
                }
            }
        )
        if (config.supportingText != null) {
            Text(
                text = config.supportingText,
                color = if (config.isError) errorColor else onSurfaceVariantColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = supportingTextPaddingStart, top = supportingTextPaddingTop)
            )
        }
    }
}
