package com.example.auratrackr.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A robust sealed interface to represent all possible loading states for a screen or component.
 * This replaces simple boolean flags for better, more descriptive state management.
 */
sealed interface LoadState {
    object Idle : LoadState
    object Loading : LoadState
    object Refreshing : LoadState
    object Submitting : LoadState
    data class Error(val error: UiError) : LoadState
    object Success : LoadState
}

/**
 * A structured data class for representing UI errors.
 *
 * @param message The user-friendly error message to be displayed.
 * @param isCritical If true, indicates a fatal error that prevents the screen from functioning.
 */
data class UiError(
    val message: String,
    val isCritical: Boolean = false
)
