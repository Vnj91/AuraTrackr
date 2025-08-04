package com.example.auratrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the blocked_apps table.
 * Defines all the database operations for managing blocked app settings.
 */
@Dao
interface BlockedAppDao {

    /**
     * Inserts or updates a blocked app setting in the database.
     * If an app with the same package name already exists, it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlockedApp(app: BlockedAppEntity)

    /**
     * Deletes a blocked app setting from the database using its package name.
     */
    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedApp(packageName: String)

    /**
     * Retrieves all blocked app settings from the database as a real-time Flow.
     * The UI will automatically update whenever the data changes.
     */
    @Query("SELECT * FROM blocked_apps")
    fun getBlockedApps(): Flow<List<BlockedAppEntity>>

    /**
     * Retrieves a single blocked app setting by its package name.
     * Used to check if an app is already being monitored.
     */
    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?
}
