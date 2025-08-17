package com.example.auratrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `blocked_apps` table.
 * Defines all the database operations for managing blocked app settings.
 */
@Dao
interface BlockedAppDao {

    /**
     * Inserts or replaces a blocked app setting in the database.
     * If an app with the same package name already exists, it will be replaced.
     *
     * @param app The [BlockedAppEntity] to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBlockedApp(app: BlockedAppEntity)

    /**
     * Deletes a blocked app setting from the database using its package name.
     *
     * @param packageName The unique package name of the app to remove from blocking.
     */
    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteBlockedApp(packageName: String)

    /**
     * Retrieves all blocked app settings from the database as a real-time [Flow].
     * The UI will automatically update whenever the data changes.
     *
     * @return A [Flow] emitting a list of all [BlockedAppEntity]s.
     */
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getBlockedApps(): Flow<List<BlockedAppEntity>>

    /**
     * Retrieves a single blocked app setting by its package name.
     * Useful for checking if an app is already being monitored.
     *
     * @param packageName The unique package name of the app to retrieve.
     * @return The [BlockedAppEntity] if found, otherwise `null`.
     */
    @Query("SELECT * FROM blocked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?

    /**
     * Checks if a specific app is currently in the blocked list.
     * This is more efficient than fetching the entire entity if only an existence check is needed.
     *
     * @param packageName The package name to check.
     * @return `true` if the app is blocked, `false` otherwise.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM blocked_apps WHERE packageName = :packageName LIMIT 1)")
    suspend fun isAppBlocked(packageName: String): Boolean

    /**
     * Deletes all entries from the `blocked_apps` table.
     * Useful for a "reset" or "clear all" functionality.
     */
    @Query("DELETE FROM blocked_apps")
    suspend fun clearAll()
}