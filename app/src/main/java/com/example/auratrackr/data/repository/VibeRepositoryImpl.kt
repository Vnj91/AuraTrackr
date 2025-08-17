package com.example.auratrackr.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.repository.VibeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vibe_settings")

/**
 * The concrete implementation of the [VibeRepository].
 *
 * This class holds the state of available and selected vibes. It uses [DataStore]
 * to persist the user's selected vibe across app sessions, ensuring a consistent
 * experience.
 *
 * @param context The application context, injected by Hilt to access DataStore.
 */
@Singleton
class VibeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : VibeRepository {

    private object VibePreferencesKeys {
        val SELECTED_VIBE_ID = stringPreferencesKey("selected_vibe_id")
    }

    private val availableVibes = listOf(
        Vibe(id = "gym", name = "Gym", colorHex = 0xFF3D5AFE),
        Vibe(id = "study", name = "Study", colorHex = 0xFFFFAB00),
        Vibe(id = "home", name = "Home", colorHex = 0xFF00C853),
        Vibe(id = "work", name = "Work", colorHex = 0xFFD500F9)
    )

    override fun getAvailableVibes(): Flow<List<Vibe>> {
        return kotlinx.coroutines.flow.flowOf(availableVibes)
    }

    override fun getSelectedVibe(): Flow<Vibe> {
        return context.dataStore.data.map { preferences ->
            val vibeId = preferences[VibePreferencesKeys.SELECTED_VIBE_ID]
            availableVibes.find { it.id == vibeId } ?: availableVibes.first()
        }
    }

    override suspend fun getSelectedVibeOnce(): Vibe {
        return getSelectedVibe().first()
    }

    override suspend fun selectVibe(vibeId: String) {
        if (availableVibes.any { it.id == vibeId }) {
            context.dataStore.edit { settings ->
                settings[VibePreferencesKeys.SELECTED_VIBE_ID] = vibeId
            }
        } else {
            // ✅ ADDED: Log a warning if an invalid ID is provided.
            Timber.w("selectVibe called with invalid vibeId: $vibeId")
        }
    }
}
