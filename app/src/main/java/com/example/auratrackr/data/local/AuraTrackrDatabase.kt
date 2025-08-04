package com.example.auratrackr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.auratrackr.data.local.dao.AppUsageDao
import com.example.auratrackr.data.local.dao.BlockedAppDao
import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.data.local.entity.BlockedAppEntity

/**
 * The main Room database class for the application.
 * This class brings together all the entities (tables) and DAOs (database queries).
 *
 * @property version The version of the database. This must be incremented whenever the
 * schema (the structure of the tables) changes.
 */
@Database(
    entities = [
        BlockedAppEntity::class,
        AppUsageEntity::class // The new entity for tracking daily usage
    ],
    version = 2, // The version is incremented from 1 to 2 because we added a new table
    exportSchema = false // You can set this to true for schema version history in production
)
abstract class AuraTrackrDatabase : RoomDatabase() {

    /**
     * Provides an abstract method to get the DAO for blocked app settings.
     * Room will generate the implementation for this.
     */
    abstract fun blockedAppDao(): BlockedAppDao

    /**
     * Provides an abstract method to get the DAO for daily app usage stats.
     * Room will generate the implementation for this.
     */
    abstract fun appUsageDao(): AppUsageDao
}
