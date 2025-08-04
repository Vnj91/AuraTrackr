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
 * This module tells Hilt how to build the database and its DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides a singleton instance of the AuraTrackrDatabase.
     * @Singleton ensures that only one instance of the database is created for the entire app.
     * .fallbackToDestructiveMigration() is used for simplicity during development. In a production
     * app, you would define a proper migration strategy.
     */
    @Provides
    @Singleton
    fun provideAuraTrackrDatabase(@ApplicationContext context: Context): AuraTrackrDatabase {
        return Room.databaseBuilder(
            context,
            AuraTrackrDatabase::class.java,
            "auratrackr_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides a singleton instance of the BlockedAppDao.
     * Hilt knows how to create this because it depends on the AuraTrackrDatabase,
     * which is provided by the function above.
     */
    @Provides
    @Singleton
    fun provideBlockedAppDao(database: AuraTrackrDatabase): BlockedAppDao {
        return database.blockedAppDao()
    }

    /**
     * Provides a singleton instance of the AppUsageDao.
     * This depends on the AuraTrackrDatabase, which Hilt knows how to provide.
     */
    @Provides
    @Singleton
    fun provideAppUsageDao(database: AuraTrackrDatabase): AppUsageDao {
        return database.appUsageDao()
    }
}
