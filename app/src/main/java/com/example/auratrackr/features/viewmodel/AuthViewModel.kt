package com.example.auratrackr.features.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Represents the possible states of the UI during authentication.
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

// Represents the different navigation destinations after an auth event.
sealed class AuthNavigationState {
    object Unauthenticated : AuthNavigationState()
    object GoToFitnessOnboarding : AuthNavigationState()
    object GoToDashboard : AuthNavigationState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    private val _navigationState = MutableStateFlow<AuthNavigationState?>(null)
    val navigationState: StateFlow<AuthNavigationState?> = _navigationState

    init {
        // When the ViewModel is created, immediately check the current user's status.
        checkCurrentUser()
    }

    /**
     * Checks the currently signed-in user and determines their onboarding status.
     * This function drives the "Smart Onboarding" logic.
     */
    private fun checkCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            // No user is logged in.
            _navigationState.value = AuthNavigationState.Unauthenticated
            return
        }

        // A user is logged in, now check their profile in our database.
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            userRepository.getUserProfile(firebaseUser.uid).collect { user ->
                _uiState.value = AuthUiState(isLoading = false)
                if (user == null) {
                    // This is a rare case: user is authenticated with Firebase, but has no
                    // profile in Firestore. We treat them as unauthenticated and needing to log in.
                    _navigationState.value = AuthNavigationState.Unauthenticated
                } else if (user.hasCompletedOnboarding) {
                    // User exists and has completed onboarding, go to the main app.
                    _navigationState.value = AuthNavigationState.GoToDashboard
                } else {
                    // User exists but hasn't finished onboarding, send them to the fitness setup.
                    _navigationState.value = AuthNavigationState.GoToFitnessOnboarding
                }
            }
        }
    }

    /**
     * Handles the user registration process.
     */
    fun register(email: String, username: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                // 1. Create user in Firebase Authentication
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // 2. Create the user profile in Firestore
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = email,
                        username = username,
                        hasCompletedOnboarding = false // Default to false
                    )
                    userRepository.createUserProfile(newUser).getOrThrow()
                    // 3. Trigger navigation to the onboarding flow
                    _navigationState.value = AuthNavigationState.GoToFitnessOnboarding
                } else {
                    throw IllegalStateException("Firebase user was null after registration.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Handles the user login process.
     */
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                // 1. Sign in the user with Firebase Authentication
                auth.signInWithEmailAndPassword(email, pass).await()
                // 2. The `checkCurrentUser` logic will automatically be triggered by the auth state change,
                // but we can also call it directly to ensure immediate navigation.
                checkCurrentUser()
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message)
            }
            // Loading state will be handled by checkCurrentUser
        }
    }

    /**
     * Finalizes the onboarding process by saving the user's fitness data.
     */
    fun completeOnboarding(weightInKg: Int, heightInCm: Int) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val uid = auth.currentUser?.uid
            if (uid == null) {
                _uiState.value = AuthUiState(error = "User not logged in.")
                return@launch
            }
            try {
                userRepository.completeOnboarding(uid, weightInKg, heightInCm).getOrThrow()
                // Trigger navigation to the main dashboard
                _navigationState.value = AuthNavigationState.GoToDashboard
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Resets any error messages so they are not shown again on configuration changes.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
