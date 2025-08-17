package com.example.auratrackr.features.focus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import com.example.auratrackr.domain.model.InstalledApp
import com.example.auratrackr.domain.repository.AppUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A data class that combines an [InstalledApp] with its monitoring status and budget for the UI.
 * ✅ FIX: Added a nullable 'budget' property to hold the BlockedAppEntity.
 */
data class MonitoredApp(
    val app: InstalledApp,
    val isMonitored: Boolean,
    val budget: BlockedAppEntity? = null
)

data class FocusSettingsUiState(
    val isLoading: Boolean = true,
    val monitoredApps: List<MonitoredApp> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FocusSettingsViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusSettingsUiState())
    val uiState: StateFlow<FocusSettingsUiState> = _uiState.asStateFlow()

    init {
        observeMonitoredApps()
    }

    private fun observeMonitoredApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                appUsageRepository.getInstalledApps(),
                appUsageRepository.getBlockedApps()
            ) { installedApps, blockedApps ->
                // Create a map for efficient lookup of blocked apps.
                val blockedAppsMap = blockedApps.associateBy { it.packageName }

                // ✅ FIX: Populate the 'budget' property when creating the MonitoredApp list.
                installedApps.map { app ->
                    val budgetInfo = blockedAppsMap[app.packageName]
                    MonitoredApp(
                        app = app,
                        isMonitored = budgetInfo != null,
                        budget = budgetInfo
                    )
                }
            }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { monitoredApps ->
                    _uiState.update {
                        it.copy(isLoading = false, monitoredApps = monitoredApps)
                    }
                }
        }
    }

    fun addAppToMonitor(app: InstalledApp, timeBudget: Long) {
        viewModelScope.launch {
            val entity = BlockedAppEntity(
                packageName = app.packageName,
                appName = app.name,
                timeBudgetInMinutes = timeBudget
            )
            appUsageRepository.addBlockedApp(entity)
        }
    }

    fun removeAppFromMonitoring(packageName: String) {
        viewModelScope.launch {
            appUsageRepository.removeBlockedApp(packageName)
        }
    }
}
