package com.example.auratrackr.features.live.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ The UI state is now simplified and no longer contains any Health Connect status.
data class LiveActivityUiState(
    val isLoading: Boolean = false,
    val liveActivityState: LiveActivityState = LiveActivityState.Idle
)

enum class LiveActivityState {
    Idle, Tracking
}

sealed interface LiveActivityEvent {
    data class ShowSnackbar(val message: String) : LiveActivityEvent
}

@HiltViewModel
class LiveActivityViewModel @Inject constructor(
    // ✅ The HealthConnectRepository dependency has been completely removed.
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveActivityUiState())
    val uiState: StateFlow<LiveActivityUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<LiveActivityEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var trackingJob: Job? = null

    // ✅ All functions related to checking and requesting Health Connect permissions are gone.

    fun onStartStopClicked() {
        if (_uiState.value.liveActivityState == LiveActivityState.Tracking) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        trackingJob?.cancel()
        _uiState.update { it.copy(liveActivityState = LiveActivityState.Tracking) }

        // ✅ The logic for reading steps has been removed.
        // You can add other tracking logic here in the future if needed.
    }

    private fun stopTracking() {
        viewModelScope.launch {
            // ✅ The logic for awarding points based on steps is gone.
            trackingJob?.cancel()
            _uiState.update { it.copy(liveActivityState = LiveActivityState.Idle) }
            _eventFlow.emit(LiveActivityEvent.ShowSnackbar("Live activity stopped!"))
        }
    }
}
