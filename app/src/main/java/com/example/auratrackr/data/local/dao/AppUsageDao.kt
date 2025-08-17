package com.example.auratrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.auratrackr.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Data Access Object (DAO) for the `app_usage_stats` table.
 * Provides methods for inserting, querying, and deleting app usage data.
 */
@Dao
interface AppUsageDao {

    /**
     * Inserts or replaces an app usage record. If a record for the same app and date
     * already exists, it will be replaced with the new one.
     *
     * @param usage The [AppUsageEntity] to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUsage(usage: AppUsageEntity)

    /**
     * Retrieves the usage record for a specific app on a specific date.
     *
     * @param packageName The package name of the app.
     * @param date The [LocalDate] to query usage for.
     * @return The [AppUsageEntity] for that day, or `null` if no record exists.
     */
    @Query("SELECT * FROM app_usage_stats WHERE packageName = :packageName AND date = :date LIMIT 1")
    suspend fun getUsageForAppOnDate(packageName: String, date: LocalDate): AppUsageEntity?

    /**
     * A private helper function that retrieves the total usage, which might be null if no records exist.
     */
    @Query("SELECT SUM(usageInMinutes) FROM app_usage_stats WHERE date = :date")
    fun getTotalUsageForDateNullable(date: LocalDate): Flow<Long?>

    /**
     * Retrieves the total screen time usage in minutes for a specific date,
     * summed across all monitored apps. This is used to power the dashboard bar chart.
     *
     * This function wraps the nullable query and maps a `null` result to `0L` to provide
     * a non-nullable [Flow] to the repository layer, simplifying UI logic.
     *
     * @param date The [LocalDate] for which to calculate total usage.
     * @return A [Flow] that emits the total usage in minutes, defaulting to `0L`.
     */
    fun getTotalUsageForDate(date: LocalDate): Flow<Long> =
        getTotalUsageForDateNullable(date).map { it ?: 0L }

    /**
     * Deletes all app usage records that are older than the specified cutoff date.
     * This is useful for data pruning to manage database size.
     *
     * @param cutoffDate The [LocalDate] before which all records should be deleted.
     */
    @Query("DELETE FROM app_usage_stats WHERE date < :cutoffDate")
    suspend fun deleteOldUsageData(cutoffDate: LocalDate)
}