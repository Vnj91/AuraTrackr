package com.example.auratrackr.features.onboarding.viewmodel

/**
 * ✅ This file now contains your stable state definitions, moved here from the ViewModel.
 * This makes them top-level classes, which resolves the "Unresolved reference" error
 * in your MainActivity and your unit test file.
 */

/**
 * Represents the possible navigation destinations from the authentication flow.
 */
sealed interface AuthNavigationTarget {
    data object GoToDashboard : AuthNavigationTarget
    data object GoToFitnessOnboarding : AuthNavigationTarget
    data object GoToLogin : AuthNavigationTarget
}

/**
 * Represents the overall state of the authentication UI, combining loading,
 * success (with navigation), and error states into a single, type-safe object.
 */
sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Success(
        val navigationTarget: AuthNavigationTarget? = null,
        val successMessage: String? = null
    ) : AuthState

    data class Error(val message: String) : AuthState
}
