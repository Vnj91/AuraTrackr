package com.example.auratrackr.features.vibe.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.auratrackr.domain.model.Vibe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class VibeUiState(
    val vibes: List<Vibe> = emptyList(),
    val selectedVibe: Vibe? = null
)

@HiltViewModel
class VibeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(VibeUiState())
    val uiState: StateFlow<VibeUiState> = _uiState.asStateFlow()

    // A sample list of vibes. In the future, this could come from a repository.
    private val availableVibes = listOf(
        Vibe("1", "Gym", Color(0xFFD4B42A)),
        Vibe("2", "Study", Color(0xFFD9F1F2)),
        Vibe("3", "Home", Color(0xFFF7F6CF)),
        Vibe("4", "Work", Color(0xFFFFD6F5))
    )

    init {
        // Initialize the state with the list of vibes and a default selection.
        _uiState.value = VibeUiState(
            vibes = availableVibes,
            selectedVibe = availableVibes.first() // Default to the first vibe
        )
    }

    /**
     * Updates the currently selected vibe.
     */
    fun onVibeSelected(vibeId: String) {
        val newSelectedVibe = availableVibes.find { it.id == vibeId }
        if (newSelectedVibe != null) {
            _uiState.value = _uiState.value.copy(selectedVibe = newSelectedVibe)
        }
    }
}
