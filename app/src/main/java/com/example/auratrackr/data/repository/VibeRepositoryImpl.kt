package com.example.auratrackr.data.repository

import androidx.compose.ui.graphics.Color
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.repository.VibeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The concrete implementation of the VibeRepository.
 * This class holds the state of available and selected vibes in memory.
 */
@Singleton
class VibeRepositoryImpl @Inject constructor() : VibeRepository {

    // A sample list of vibes. In a real app, this might come from a remote config or local DB.
    private val availableVibes = listOf(
        Vibe("1", "Gym", Color(0xFFD4B42A)),
        Vibe("2", "Study", Color(0xFFD9F1F2)),
        Vibe("3", "Home", Color(0xFFF7F6CF)),
        Vibe("4", "Work", Color(0xFFFFD6F5))
    )

    // A MutableStateFlow to hold the currently selected vibe.
    // It's initialized with the first vibe as the default.
    private val _selectedVibe = MutableStateFlow(availableVibes.first())

    override fun getAvailableVibes(): List<Vibe> {
        return availableVibes
    }

    override fun getSelectedVibe(): Flow<Vibe> {
        return _selectedVibe.asStateFlow()
    }

    override suspend fun selectVibe(vibeId: String) {
        availableVibes.find { it.id == vibeId }?.let { newVibe ->
            _selectedVibe.value = newVibe
        }
    }
}
