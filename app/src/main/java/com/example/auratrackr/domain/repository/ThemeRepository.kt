package com.example.auratrackr.domain.repository

import com.example.auratrackr.features.settings.ui.ThemeSetting
import kotlinx.coroutines.flow.Flow

/**
 * ✅ THE FINAL FIX: This interface is now correctly defined.
 * It provides the exact function signatures that your stable SettingsViewModel requires.
 */
interface ThemeRepository {

    /**
     * Retrieves the user's preferred theme setting as a Flow.
     */
    fun getThemeSetting(): Flow<ThemeSetting>

    /**
     * Persists the user's chosen theme setting.
     */
    suspend fun setThemeSetting(themeSetting: ThemeSetting)
}

