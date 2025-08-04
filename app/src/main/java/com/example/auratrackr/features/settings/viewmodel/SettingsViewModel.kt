package com.example.auratrackr.features.settings.viewmodel

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
            _uiState.value = SettingsUiState(isLoading = true)

            userRepository.getUserProfile(uid).collect { user ->
                if (user != null) {
                    _uiState.value = SettingsUiState(
                        isLoading = false,
                        username = user.username ?: "User",
                        height = user.heightInCm?.let { "$it cm" } ?: "-",
                        weight = user.weightInKg?.let { "$it kg" } ?: "-"
                    )
                } else {
                    _uiState.value = SettingsUiState(isLoading = false, error = "User profile not found.")
                }
            }
        }
    }
}
