package com.example.auratrackr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.auratrackr.domain.repository.ThemeRepository
import com.example.auratrackr.features.settings.ui.ThemeSetting
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ThemeRepository {

    private object PreferencesKeys {
        val THEME_SETTING = stringPreferencesKey("theme_setting")
    }

    override fun getThemeSetting(): Flow<ThemeSetting> {
        return context.themeDataStore.data.map { preferences ->
            // Read the saved string, or default to SYSTEM if nothing is saved yet.
            ThemeSetting.valueOf(preferences[PreferencesKeys.THEME_SETTING] ?: ThemeSetting.SYSTEM.name)
        }
    }

    override suspend fun setThemeSetting(themeSetting: ThemeSetting) {
        context.themeDataStore.edit { settings ->
            settings[PreferencesKeys.THEME_SETTING] = themeSetting.name
        }
    }
}
