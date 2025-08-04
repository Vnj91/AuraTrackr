package com.example.auratrackr.features.vibe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.repository.VibeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VibeUiState(
    val vibes: List<Vibe> = emptyList(),
    val selectedVibe: Vibe? = null
)

@HiltViewModel
class VibeViewModel @Inject constructor(
    private val vibeRepository: VibeRepository // <-- INJECT THE REPOSITORY
) : ViewModel() {

    private val _uiState = MutableStateFlow(VibeUiState())
    val uiState: StateFlow<VibeUiState> = _uiState.asStateFlow()

    init {
        // Get the list of available vibes from the repository
        val availableVibes = vibeRepository.getAvailableVibes()
        _uiState.value = VibeUiState(vibes = availableVibes)

        // Listen for changes to the selected vibe from the repository
        viewModelScope.launch {
            vibeRepository.getSelectedVibe().collect { selectedVibe ->
                _uiState.value = _uiState.value.copy(selectedVibe = selectedVibe)
            }
        }
    }

    /**
     * Updates the currently selected vibe via the repository.
     */
    fun onVibeSelected(vibeId: String) {
        viewModelScope.launch {
            vibeRepository.selectVibe(vibeId)
        }
    }
}
