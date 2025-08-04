package com.example.auratrackr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the daily usage statistics for a single monitored application.
 * Each record is unique for a given package name and date.
 *
 * @property id A unique auto-generated ID for the record.
 * @property packageName The unique package name of the application being tracked.
 * @property date The date of the usage record in YYYY-MM-DD format.
 * @property usageInMinutes The total time spent in the app for that day, in minutes.
 * @property launchCount The total number of times the app was launched on that day.
 */
@Entity(tableName = "app_usage_stats")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val date: String, // Using YYYY-MM-DD format for simplicity
    val usageInMinutes: Long,
    val launchCount: Int
)
