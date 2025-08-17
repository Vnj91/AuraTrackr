package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Vibe
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for managing the app's "Vibe" state.
 *
 * This repository abstracts the data source for vibes, which could be a local
 * data store (like SharedPreferences or DataStore) or a remote configuration.
 */
interface VibeRepository {

    /**
     * Retrieves the list of all available vibes, providing real-time updates.
     * Using a Flow allows the UI to reactively update if the list of vibes
     * is ever changed dynamically (e.g., fetched from a remote config).
     *
     * @return A [Flow] that emits the list of all available [Vibe]s.
     */
    fun getAvailableVibes(): Flow<List<Vibe>>

    /**
     * Retrieves the currently selected vibe as a real-time Flow.
     * Components can collect this flow to react to vibe changes instantly.
     *
     * @return A [Flow] that emits the currently selected [Vibe].
     */
    fun getSelectedVibe(): Flow<Vibe>

    /**
     * Fetches the currently selected vibe a single time.
     * This is useful for one-off operations where a subscription to changes is not needed.
     *
     * @return The currently selected [Vibe], or a default if none is set.
     */
    suspend fun getSelectedVibeOnce(): Vibe

    /**
     * Sets the new selected vibe. This will persist the selection and notify
     * any collectors of the `getSelectedVibe` Flow.
     *
     * @param vibeId The unique ID of the vibe to set as active.
     */
    suspend fun selectVibe(vibeId: String)
}