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
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _navigationState.value = AuthNavigationState.Unauthenticated
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            userRepository.getUserProfile(firebaseUser.uid).collect { user ->
                _uiState.value = AuthUiState(isLoading = false)
                if (user == null) {
                    // This can happen for anonymous users who don't have a profile yet
                    // or in rare error cases. We treat them as new users.
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
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = email,
                        username = username,
                        hasCompletedOnboarding = false
                    )
                    userRepository.createUserProfile(newUser).getOrThrow()
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

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                checkCurrentUser()
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message)
            }
        }
    }

    /**
     * Signs the user in anonymously and creates a basic user profile.
     */
    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                val authResult = auth.signInAnonymously().await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // Create a basic profile for the anonymous user
                    val newUser = User(
                        uid = firebaseUser.uid,
                        email = null,
                        username = "Guest",
                        hasCompletedOnboarding = false
                    )
                    userRepository.createUserProfile(newUser).getOrThrow()
                    // The checkCurrentUser logic will now send them to onboarding
                    checkCurrentUser()
                } else {
                    throw IllegalStateException("Firebase anonymous user was null.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message)
            }
        }
    }

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
                _navigationState.value = AuthNavigationState.GoToDashboard
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
