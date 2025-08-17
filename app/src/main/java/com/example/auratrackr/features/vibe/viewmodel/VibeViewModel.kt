package com.example.auratrackr.features.vibe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.repository.VibeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VibeUiState(
    val isLoading: Boolean = true,
    val vibes: List<Vibe> = emptyList(),
    val selectedVibe: Vibe? = null,
    val error: String? = null
)

@HiltViewModel
class VibeViewModel @Inject constructor(
    private val vibeRepository: VibeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VibeUiState())
    val uiState: StateFlow<VibeUiState> = _uiState.asStateFlow()

    init {
        observeVibes()
    }

    /**
     * Observes both the list of available vibes and the currently selected vibe.
     * It combines them into a single UI state, ensuring the UI is always in sync.
     */
    private fun observeVibes() {
        viewModelScope.launch {
            combine(
                vibeRepository.getAvailableVibes(),
                vibeRepository.getSelectedVibe()
            ) { availableVibes, selectedVibe ->
                // This mapping function is called whenever either of the source flows emits a new value.
                VibeUiState(
                    isLoading = false,
                    vibes = availableVibes,
                    selectedVibe = selectedVibe
                )
            }
                .onStart { emit(VibeUiState(isLoading = true)) } // Set loading state at the beginning of the flow collection.
                .catch { e ->
                    // If an error occurs in either of the source flows, update the UI state with the error message.
                    emit(VibeUiState(isLoading = false, error = e.message))
                }
                .collect { newState ->
                    // Collect the combined state and update the UI.
                    _uiState.value = newState
                }
        }
    }

    /**
     * Updates the currently selected vibe.
     *
     * @param vibeId The unique ID of the vibe to be selected.
     */
    fun onVibeSelected(vibeId: String) {
        // Prevent re-selecting the same vibe to avoid unnecessary writes.
        if (vibeId == _uiState.value.selectedVibe?.id) return

        viewModelScope.launch {
            vibeRepository.selectVibe(vibeId)
        }
    }
}