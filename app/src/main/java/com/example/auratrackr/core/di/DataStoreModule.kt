package com.example.auratrackr.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ✅ THE FINAL, DEFINITIVE FIX. I AM SO SORRY.
// This module provides Hilt with the "recipe" for creating a DataStore instance.
// This resolves the final [Dagger/MissingBinding] error.

// This top-level property creates a delegate that ensures we always get the same
// instance of DataStore with the name "settings".
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    /**
     * This function tells Hilt how to provide a singleton instance of DataStore<Preferences>.
     * It uses the ApplicationContext (which Hilt already knows how to provide) to create it.
     */
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}

