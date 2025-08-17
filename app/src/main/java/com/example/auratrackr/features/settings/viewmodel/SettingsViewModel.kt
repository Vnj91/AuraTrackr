package com.example.auratrackr.features.settings.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoadingProfile: Boolean = true,
    val isUploadingPicture: Boolean = false,
    val username: String = "User",
    val height: String = "-",
    val weight: String = "-",
    val profilePictureUrl: String? = null
)

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isLoadingProfile = true) }

            userRepository.getUserProfile(uid)
                .distinctUntilChanged() // Prevents recomposition if the user data hasn't changed
                .collect { user ->
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                isLoadingProfile = false,
                                username = user.username ?: "User",
                                height = user.heightInCm?.let { h -> "$h cm" } ?: "-",
                                weight = user.weightInKg?.let { w -> "$w kg" } ?: "-",
                                profilePictureUrl = user.profilePictureUrl
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoadingProfile = false) }
                        _eventFlow.emit(UiEvent.ShowSnackbar("User profile not found."))
                    }
                }
        }
    }

    /**
     * Handles the selection of a new profile picture. It updates the UI to show a loading
     * state, uploads the image via the repository, and emits events for success or failure.
     *
     * @param imageUri The local URI of the selected image.
     */
    fun onProfilePictureSelected(imageUri: Uri) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isUploadingPicture = true) }

            val result = userRepository.uploadProfilePicture(uid, imageUri)

            if (result.isSuccess) {
                // On success, the getUserProfile flow will automatically emit the new user data
                // with the updated profilePictureUrl. We just need to turn off the uploading indicator.
                _eventFlow.emit(UiEvent.ShowSnackbar("Profile picture updated!"))
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Upload failed. Please try again."
                _eventFlow.emit(UiEvent.ShowSnackbar(errorMessage))
            }
            // This will be updated regardless of success or failure.
            _uiState.update { it.copy(isUploadingPicture = false) }
        }
    }
}