package com.example.auratrackr.features.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// --- State Definitions ---

/**
 * Represents the possible navigation destinations from the authentication flow.
 */
sealed interface AuthNavigationTarget {
    object GoToDashboard : AuthNavigationTarget
    object GoToFitnessOnboarding : AuthNavigationTarget
    object GoToLogin : AuthNavigationTarget
}

/**
 * Represents the overall state of the authentication UI, combining loading,
 * success (with navigation), and error states into a single, type-safe object.
 */
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(
        val navigationTarget: AuthNavigationTarget? = null,
        val successMessage: String? = null
    ) : AuthState

    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            _authState.value = AuthState.Success(navigationTarget = AuthNavigationTarget.GoToLogin)
        } else {
            checkUserProfile(firebaseUser.uid)
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private fun checkUserProfile(uid: String) {
        viewModelScope.launch {
            try {
                userRepository.getUserProfile(uid).collect { user ->
                    _authState.value = when {
                        user == null -> AuthState.Success(AuthNavigationTarget.GoToFitnessOnboarding)
                        user.hasCompletedOnboarding -> AuthState.Success(AuthNavigationTarget.GoToDashboard)
                        else -> AuthState.Success(AuthNavigationTarget.GoToFitnessOnboarding)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to check user profile.")
            }
        }
    }

    fun register(email: String, username: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user was null after registration.")
                val newUser = User(uid = firebaseUser.uid, email = email, username = username)
                userRepository.createUserProfile(newUser).getOrThrow()
                // Auth state listener will handle navigation automatically
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthException(e))
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                // Auth state listener will handle navigation
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthException(e))
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInAnonymously().await()
                val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase anonymous user was null.")
                val newUser = User(uid = firebaseUser.uid, username = "Guest", isGuest = true)
                userRepository.createUserProfile(newUser).getOrThrow()
                // Auth state listener will handle navigation
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthException(e))
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success(successMessage = "Password reset email sent!")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(parseAuthException(e))
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Success(navigationTarget = AuthNavigationTarget.GoToLogin)
    }

    fun completeOnboarding(weightInKg: Int, heightInCm: Int) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val uid = auth.currentUser?.uid ?: run {
                _authState.value = AuthState.Error("User not authenticated.")
                return@launch
            }
            try {
                userRepository.completeOnboarding(uid, weightInKg, heightInCm).getOrThrow()
                _authState.value = AuthState.Success(navigationTarget = AuthNavigationTarget.GoToDashboard)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Onboarding failed.")
            }
        }
    }

    fun resetState() {
        _authState.update {
            if (it is AuthState.Success) AuthState.Idle else it
        }
    }

    private fun parseAuthException(e: Exception): String {
        return if (e is FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Please enter a valid email address."
                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered. Please log in."
                "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please use at least 6 characters."
                else -> "An authentication error occurred."
            }
        } else {
            e.localizedMessage ?: "An unknown error occurred."
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }
}