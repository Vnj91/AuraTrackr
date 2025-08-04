package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Vibe
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for managing the app's "Vibe" state.
 */
interface VibeRepository {

    /**
     * Provides the list of all available vibes.
     */
    fun getAvailableVibes(): List<Vibe>

    /**
     * Provides the currently selected vibe as a real-time Flow.
     * Components can collect this flow to react to vibe changes.
     */
    fun getSelectedVibe(): Flow<Vibe>

    /**
     * Sets the new selected vibe.
     *
     * @param vibeId The ID of the vibe to set as active.
     */
    suspend fun selectVibe(vibeId: String)
}
