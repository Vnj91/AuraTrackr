package com.example.auratrackr.core.di

import android.content.Context
import androidx.room.Room
import com.example.auratrackr.data.local.AuraTrackrDatabase
import com.example.auratrackr.data.local.dao.AppUsageDao
import com.example.auratrackr.data.local.dao.BlockedAppDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module responsible for providing database-related instances.
 * This module defines how to create and provide the application's Room database
 * and its associated Data Access Objects (DAOs).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "auratrackr_database"

    /**
     * Provides a singleton instance of the [AuraTrackrDatabase].
     *
     * @param context The application context provided by Hilt.
     * @return A singleton instance of the app's Room database.
     */
    @Provides
    @Singleton
    fun provideAuraTrackrDatabase(@ApplicationContext context: Context): AuraTrackrDatabase {
        return Room.databaseBuilder(
            context,
            AuraTrackrDatabase::class.java,
            DATABASE_NAME
        )
            // NOTE: Using fallbackToDestructiveMigration during development. Replace with a proper
            // migration strategy before production releases.
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides a singleton instance of the [BlockedAppDao].
     *
     * @param database The [AuraTrackrDatabase] instance provided by Hilt.
     * @return A singleton DAO for accessing blocked app data.
     */
    @Provides
    @Singleton
    fun provideBlockedAppDao(database: AuraTrackrDatabase): BlockedAppDao = database.blockedAppDao()

    /**
     * Provides a singleton instance of the [AppUsageDao].
     *
     * @param database The [AuraTrackrDatabase] instance provided by Hilt.
     * @return A singleton DAO for accessing app usage data.
     */
    @Provides
    @Singleton
    fun provideAppUsageDao(database: AuraTrackrDatabase): AppUsageDao = database.appUsageDao()
}
