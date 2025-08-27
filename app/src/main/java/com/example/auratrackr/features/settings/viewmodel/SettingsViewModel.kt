package com.example.auratrackr.features.settings.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.repository.ThemeRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.features.settings.ui.ThemeSetting
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
    val profilePictureUrl: String? = null,
    // ✅ ADDED: State for the current theme setting.
    val themeSetting: ThemeSetting = ThemeSetting.SYSTEM
)

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    // ✅ ADDED: Inject the new ThemeRepository.
    private val themeRepository: ThemeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        observeUserProfileAndTheme()
    }

    private fun observeUserProfileAndTheme() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isLoadingProfile = true) }

            // ✅ FIX: Combine the user profile and theme setting flows into a single state.
            combine(
                userRepository.getUserProfile(uid).distinctUntilChanged(),
                themeRepository.getThemeSetting()
            ) { user, theme ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoadingProfile = false,
                            username = user.username ?: "User",
                            height = user.heightInCm?.let { h -> "$h cm" } ?: "-",
                            weight = user.weightInKg?.let { w -> "$w kg" } ?: "-",
                            profilePictureUrl = user.profilePictureUrl,
                            themeSetting = theme
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingProfile = false) }
                    _eventFlow.emit(UiEvent.ShowSnackbar("User profile not found."))
                }
            }.collect() // Start collecting the combined flow.
        }
    }

    fun onProfilePictureSelected(imageUri: Uri) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isUploadingPicture = true) }

            val result = userRepository.uploadProfilePicture(uid, imageUri)

            if (result.isSuccess) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Profile picture updated!"))
            } else {
                val errorMessage = result.exceptionOrNull()?.message ?: "Upload failed. Please try again."
                _eventFlow.emit(UiEvent.ShowSnackbar(errorMessage))
            }
            _uiState.update { it.copy(isUploadingPicture = false) }
        }
    }

    /**
     * ✅ ADDED: Saves the user's selected theme preference.
     */
    fun onThemeSelected(themeSetting: ThemeSetting) {
        viewModelScope.launch {
            themeRepository.setThemeSetting(themeSetting)
        }
    }
}
