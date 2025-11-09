package com.example.auratrackr.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.auratrackr.domain.repository.ThemeRepository
import com.example.auratrackr.features.settings.ui.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    // Hilt will provide the singleton instance of DataStore<Preferences>
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {

    // Define a key to safely access the theme setting in DataStore.
    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
    }

    /**
     * ✅ This function now correctly implements the interface required by your ViewModel.
     * It reads the theme preference string from DataStore and maps it to the
     * correct ThemeSetting enum, providing a default value if none is found.
     */
    override fun getThemeSetting(): Flow<ThemeSetting> {
        return dataStore.data.map { preferences ->
            val themeName = preferences[PreferencesKeys.THEME_SETTING] ?: ThemeSetting.SYSTEM.name
            try {
                // Safely convert the stored string back into an enum.
                ThemeSetting.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                // If the stored value is somehow invalid, default to the system theme.
                ThemeSetting.SYSTEM
            }
        }
    }

    /**
     * This function saves the selected theme's enum name as a string in DataStore.
     */
    override suspend fun setThemeSetting(themeSetting: ThemeSetting) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_SETTING] = themeSetting.name
        }
    }
}
