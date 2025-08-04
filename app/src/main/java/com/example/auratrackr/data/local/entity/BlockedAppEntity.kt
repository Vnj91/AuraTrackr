package com.example.auratrackr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single application that the user has chosen to track or block.
 * This class defines the schema for the 'blocked_apps' table in the Room database.
 *
 * @property packageName The unique package name of the application (e.g., "com.instagram.android").
 * This serves as the primary key for the table.
 * @property timeBudgetInMinutes The maximum daily usage time allowed for this app, in minutes.
 * @property launchBudget The maximum number of times the user can launch this app daily.
 * @property isEnabled A flag to quickly enable or disable tracking for this app without losing the settings.
 */
@Entity(tableName = "blocked_apps") // <-- THIS ANNOTATION WAS MISSING
data class BlockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val timeBudgetInMinutes: Long,
    val launchBudget: Int,
    val isEnabled: Boolean = true
)
