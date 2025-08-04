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

// A new data class to combine app info with its blocked status for the UI
data class MonitoredApp(
    val app: InstalledApp,
    val isMonitored: Boolean
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
    val uiState: StateFlow<FocusSettingsUiState> = _uiState

    init {
        loadScreenData()
    }

    private fun loadScreenData() {
        viewModelScope.launch {
            _uiState.value = FocusSettingsUiState(isLoading = true)

            // Combine two flows: one for all installed apps and one for the apps saved in our DB
            appUsageRepository.getInstalledApps()
                .combine(appUsageRepository.getBlockedApps()) { installedApps, blockedApps ->
                    val blockedPackageNames = blockedApps.map { it.packageName }.toSet()

                    // Create the final list for the UI
                    installedApps.map { app ->
                        MonitoredApp(
                            app = app,
                            isMonitored = app.packageName in blockedPackageNames
                        )
                    }
                }
                .catch { e ->
                    _uiState.value = FocusSettingsUiState(isLoading = false, error = e.message)
                }
                .collect { monitoredApps ->
                    _uiState.value = FocusSettingsUiState(isLoading = false, monitoredApps = monitoredApps)
                }
        }
    }

    /**
     * Adds an app to the local database to be monitored.
     */
    fun addAppToMonitor(packageName: String, timeBudget: Long, launchBudget: Int) {
        viewModelScope.launch {
            val entity = BlockedAppEntity(
                packageName = packageName,
                timeBudgetInMinutes = timeBudget,
                launchBudget = launchBudget
            )
            appUsageRepository.addBlockedApp(entity)
        }
    }

    /**
     * Removes an app from the local database.
     */
    fun removeAppToMonitor(packageName: String) {
        viewModelScope.launch {
            appUsageRepository.removeBlockedApp(packageName)
        }
    }
}
