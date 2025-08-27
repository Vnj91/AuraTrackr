package com.example.auratrackr.domain.repository

import com.example.auratrackr.features.settings.ui.ThemeSetting
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for managing the app's theme preference.
 */
interface ThemeRepository {

    /**
     * Retrieves the user's saved theme preference as a real-time Flow.
     * @return A [Flow] that emits the current [ThemeSetting].
     */
    fun getThemeSetting(): Flow<ThemeSetting>

    /**
     * Saves the user's selected theme preference.
     * @param themeSetting The [ThemeSetting] to save.
     */
    suspend fun setThemeSetting(themeSetting: ThemeSetting)
}
