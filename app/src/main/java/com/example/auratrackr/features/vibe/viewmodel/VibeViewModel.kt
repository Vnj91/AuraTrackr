package com.example.auratrackr.features.vibe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.repository.VibeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
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

    private fun observeVibes() {
        viewModelScope.launch {
            combine(
                vibeRepository.getAvailableVibes(),
                vibeRepository.getSelectedVibe()
            ) { availableVibes, selectedVibe ->
                VibeUiState(
                    isLoading = false,
                    vibes = availableVibes,
                    selectedVibe = selectedVibe
                )
            }
                .onStart { emit(VibeUiState(isLoading = true)) }
                .catch { e ->
                    emit(VibeUiState(isLoading = false, error = e.message))
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun onVibeSelected(vibeId: String) {
        if (vibeId == _uiState.value.selectedVibe?.id) return

        viewModelScope.launch {
            vibeRepository.selectVibe(vibeId)
        }
    }
}
