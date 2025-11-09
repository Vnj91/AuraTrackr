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
import timber.log.Timber
import javax.inject.Inject

data class PermissionsUiState(
    val isUsageAccessGranted: Boolean = false,
    val isAccessibilityServiceEnabled: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    companion object {
        private const val TAG = "PermissionsViewModel"
    }

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Call permission checks individually and handle platform-specific
            // SecurityException without catching generic Exception to satisfy detekt.
            val usageGranted = try {
                permissionManager.isUsageAccessGranted()
            } catch (e: SecurityException) {
                Timber.tag(TAG).w(e, "isUsageAccessGranted threw SecurityException")
                false
            }

            val accessibilityEnabled = try {
                permissionManager.isAccessibilityServiceEnabled()
            } catch (e: SecurityException) {
                Timber.tag(TAG).w(e, "isAccessibilityServiceEnabled threw SecurityException")
                false
            }

            _uiState.update {
                it.copy(
                    isUsageAccessGranted = usageGranted,
                    isAccessibilityServiceEnabled = accessibilityEnabled,
                    isLoading = false
                )
            }
        }
    }
}
