package com.example.auratrackr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single application that the user has chosen to track or block.
 * This class defines the schema for the `blocked_apps` table in the Room database.
 *
 * @property packageName The unique package name of the application (e.g., "com.instagram.android").
 * This serves as the primary key for the table, ensuring each app can only have one entry.
 * @property appName The user-friendly display name of the application (e.g., "Instagram").
 * Stored for easy access in the UI without needing to query the package manager repeatedly.
 * @property timeBudgetInMinutes The maximum daily usage time allowed for this app, in minutes.
 * @property isEnabled A flag to quickly enable or disable tracking for this app without deleting its settings.
 */
@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val timeBudgetInMinutes: Long,
    val isEnabled: Boolean = true
)