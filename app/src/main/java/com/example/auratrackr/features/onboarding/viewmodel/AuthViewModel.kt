package com.example.auratrackr.features.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CancellationException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

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
            } catch (e: FirebaseFirestoreException) {
                // Narrowed to Firestore-specific failures when reading profile info.
                Timber.tag(TAG).e(e, "checkUserProfile failed (firestore)")
                _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to check user profile.")
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // final fallback logs unexpected errors from Firebase APIs
    fun register(email: String, username: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val firebaseUser = authResult.user ?: throw IllegalStateException(
                    "Firebase user was null after registration."
                )
                val newUser = User(uid = firebaseUser.uid, email = email, username = username)
                userRepository.createUserProfile(newUser).getOrThrow()
                // Auth state listener will handle navigation automatically
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseAuthException) {
                Timber.tag(TAG).e(e, "register failed (auth)")
                _authState.value = AuthState.Error(parseAuthException(e))
            } catch (e: Exception) {
                // Fallback for unexpected errors (keep logging). Firebase coroutines APIs can throw
                // a variety of unchecked exceptions; log them here.
                Timber.tag(TAG).e(e, "register failed (unexpected)")
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registration failed.")
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // final fallback logs unexpected errors from Firebase APIs
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, pass).await()
                // Auth state listener will handle navigation
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseAuthException) {
                Timber.tag(TAG).e(e, "login failed (auth)")
                _authState.value = AuthState.Error(parseAuthException(e))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "login failed (unexpected)")
                _authState.value = AuthState.Error(e.localizedMessage ?: "Login failed.")
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // final fallback logs unexpected errors from Firebase APIs
    fun signInAnonymously() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInAnonymously().await()
                val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase anonymous user was null.")
                val newUser = User(uid = firebaseUser.uid, username = "Guest", isGuest = true)
                userRepository.createUserProfile(newUser).getOrThrow()
                // Auth state listener will handle navigation
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseAuthException) {
                Timber.tag(TAG).e(e, "signInAnonymously failed (auth)")
                _authState.value = AuthState.Error(parseAuthException(e))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "signInAnonymously failed (unexpected)")
                _authState.value = AuthState.Error(e.localizedMessage ?: "Anonymous sign-in failed.")
            }
        }
    }

    @Suppress("TooGenericExceptionCaught") // final fallback logs unexpected errors from Firebase APIs
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success(successMessage = "Password reset email sent!")
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseAuthException) {
                Timber.tag(TAG).e(e, "sendPasswordResetEmail failed (auth)")
                _authState.value = AuthState.Error(parseAuthException(e))
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "sendPasswordResetEmail failed (unexpected)")
                _authState.value = AuthState.Error(e.localizedMessage ?: "Password reset failed.")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Success(navigationTarget = AuthNavigationTarget.GoToLogin)
    }

    @Suppress("TooGenericExceptionCaught") // final fallback logs unexpected errors from Firebase APIs
    fun completeOnboarding(weightInKg: Int, heightInCm: Int) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val uid = auth.currentUser?.uid ?: run {
                _authState.value = AuthState.Error("User not authenticated.")
                return@launch
            }
            try {
                userRepository.completeOnboarding(uid, weightInKg, heightInCm).getOrThrow()
                // The auth state listener will now automatically handle the navigation to the dashboard.
            } catch (e: CancellationException) {
                throw e
            } catch (e: FirebaseFirestoreException) {
                Timber.tag(TAG).e(e, "completeOnboarding failed (firestore)")
                _authState.value = AuthState.Error(e.localizedMessage ?: "Onboarding failed.")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "completeOnboarding failed (unexpected)")
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
