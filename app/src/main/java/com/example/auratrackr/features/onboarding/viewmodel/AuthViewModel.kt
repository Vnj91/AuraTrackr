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
    val error: String? = null,
    val isPasswordResetEmailSent: Boolean = false // <-- ADDED THIS LINE
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
        auth.addAuthStateListener { firebaseAuth ->
            checkCurrentUser(firebaseAuth.currentUser)
        }
    }

    private fun checkCurrentUser(firebaseUser: com.google.firebase.auth.FirebaseUser?) {
        if (firebaseUser == null) {
            _navigationState.value = AuthNavigationState.Unauthenticated
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            userRepository.getUserProfile(firebaseUser.uid).collect { user ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (user == null) {
                    _navigationState.value = AuthNavigationState.GoToFitnessOnboarding
                } else if (user.hasCompletedOnboarding) {
                    _navigationState.value = AuthNavigationState.GoToDashboard
                } else {
                    _navigationState.value = AuthNavigationState.GoToFitnessOnboarding
                }
            }
        }
    }

    fun register(email: String, username: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val newUser = User(uid = firebaseUser.uid, email = email, username = username)
                    userRepository.createUserProfile(newUser).getOrThrow()
                } else {
                    throw IllegalStateException("Firebase user was null after registration.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val authResult = auth.signInAnonymously().await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val newUser = User(uid = firebaseUser.uid, username = "Guest")
                    userRepository.createUserProfile(newUser).getOrThrow()
                } else {
                    throw IllegalStateException("Firebase anonymous user was null.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    /**
     * Sends a password reset email to the specified address.
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = _uiState.value.copy(isLoading = false, isPasswordResetEmailSent = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun completeOnboarding(weightInKg: Int, heightInCm: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                userRepository.completeOnboarding(uid, weightInKg, heightInCm).getOrThrow()
                _navigationState.value = AuthNavigationState.GoToDashboard
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetPasswordResetEmailSentState() {
        _uiState.value = _uiState.value.copy(isPasswordResetEmailSent = false)
    }
}
