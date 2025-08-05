package com.example.auratrackr.features.settings.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val username: String = "User",
    val height: String = "-",
    val weight: String = "-",
    val profilePictureUrl: String? = null, // <-- ADDED THIS LINE
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)

            userRepository.getUserProfile(uid).collect { user ->
                if (user != null) {
                    _uiState.value = SettingsUiState(
                        isLoading = false,
                        username = user.username ?: "User",
                        height = user.heightInCm?.let { "$it cm" } ?: "-",
                        weight = user.weightInKg?.let { "$it kg" } ?: "-",
                        profilePictureUrl = user.profilePictureUrl // <-- POPULATE THE URL
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "User profile not found.")
                }
            }
        }
    }

    /**
     * Handles the process of uploading a new profile picture.
     * @param imageUri The local Uri of the image selected by the user.
     */
    fun onProfilePictureSelected(imageUri: Uri) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = userRepository.uploadProfilePicture(uid, imageUri)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
            // The user profile flow will automatically emit the new user data with the updated URL,
            // so we don't need to manually set the isLoading to false here.
        }
    }
}
