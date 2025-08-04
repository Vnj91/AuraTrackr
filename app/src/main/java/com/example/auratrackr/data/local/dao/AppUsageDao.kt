package com.example.auratrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.auratrackr.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the app_usage_stats table.
 */
@Dao
interface AppUsageDao {

    /**
     * Inserts or updates a usage record. If a record for the same app and date
     * already exists, it will be replaced.
     * Note: A more robust implementation might use @Update and check for existence first.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsage(usage: AppUsageEntity)

    /**
     * Retrieves the usage record for a specific app on a specific date.
     */
    @Query("SELECT * FROM app_usage_stats WHERE packageName = :packageName AND date = :date LIMIT 1")
    suspend fun getUsageForAppOnDate(packageName: String, date: String): AppUsageEntity?

    /**
     * Retrieves the total screen time usage in minutes for a specific date,
     * summed across all monitored apps.
     * This will be used to power the dashboard bar chart.
     */
    @Query("SELECT SUM(usageInMinutes) FROM app_usage_stats WHERE date = :date")
    fun getTotalUsageForDate(date: String): Flow<Long?>
}
