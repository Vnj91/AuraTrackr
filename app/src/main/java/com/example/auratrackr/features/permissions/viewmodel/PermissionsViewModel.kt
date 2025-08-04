package com.example.auratrackr.features.permissions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.core.permissions.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionsUiState(
    val isUsageAccessGranted: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false
)

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState

    init {
        // Check permissions as soon as the ViewModel is created.
        checkPermissions()
    }

    /**
     * Checks the current status of all required permissions and updates the UI state.
     * This should be called whenever the app resumes to get the latest status.
     */
    fun checkPermissions() {
        viewModelScope.launch {
            val usageGranted = permissionManager.isUsageAccessGranted()
            val accessibilityEnabled = permissionManager.isAccessibilityServiceEnabled()
            _uiState.value = PermissionsUiState(
                isUsageAccessGranted = usageGranted,
                isAccessibilityServiceEnabled = accessibilityEnabled
            )
        }
    }
}
