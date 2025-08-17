package com.example.auratrackr.features.permissions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.core.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Represents the UI state for the Permissions screen.
 *
 * @property isUsageAccessGranted True if the user has granted Usage Stats access.
 * @property isAccessibilityServiceEnabled True if the user has enabled the app's Accessibility Service.
 * @property isLoading True when the permission statuses are being checked.
 */
data class PermissionsUiState(
    val isUsageAccessGranted: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * The ViewModel for the Permissions screen.
 *
 * This ViewModel is responsible for checking the status of required special permissions
 * using the [PermissionManager] and exposing the state to the UI.
 */
@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        // Check permissions as soon as the ViewModel is created.
        checkPermissions()
    }

    /**
     * Checks the current status of all required permissions and updates the UI state.
     * This should be called whenever the app needs to refresh the permission status,
     * such as when the screen becomes visible again after the user has visited the system settings.
     */
    fun checkPermissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val usageGranted = permissionManager.isUsageAccessGranted()
                val accessibilityEnabled = permissionManager.isAccessibilityServiceEnabled()
                _uiState.update {
                    it.copy(
                        isUsageAccessGranted = usageGranted,
                        isAccessibilityServiceEnabled = accessibilityEnabled,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // In case of an unexpected error, ensure loading is false.
                // Optionally, an error field could be added to the UI state.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}