package com.example.auratrackr.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import java.time.LocalDate

/**
 * Represents the daily usage statistics for a single monitored application.
 *
 * This entity uses a composite primary key (`packageName`, `date`) to ensure that
 * there is only one usage record per app per day, preventing duplicate entries. This is
 * more robust than using an auto-generated ID for this use case. An index is also
 * added to the `date` column to optimize queries that filter by date.
 *
 * @property packageName The unique package name of the application being tracked. Part of the composite primary key.
 * @property date The date of the usage record. Storing this as [LocalDate] ensures type safety and proper sorting. Part of the composite primary key.
 * @property usageInMinutes The total time spent in the app for that day, in minutes.
 * @property launchCount The total number of times the app was launched on that day.
 */
@Entity(
    tableName = "app_usage_stats",
    primaryKeys = ["packageName", "date"],
    indices = [Index(value = ["date"])]
)
data class AppUsageEntity(
    val packageName: String,
    val date: LocalDate,
    val usageInMinutes: Long,
    val launchCount: Int
)
