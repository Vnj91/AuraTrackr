package com.example.auratrackr.domain.repository

import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import com.example.auratrackr.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * An interface that defines the contract for all data operations related to
 * application management, blocking, and usage tracking.
 *
 * This repository abstracts the data sources (local database, package manager)
 * from the rest of the application.
 */
interface AppUsageRepository {

    // --- Installed App Information ---

    /**
     * Retrieves a list of all user-installed applications on the device.
     * @return A [Flow] that emits the list of [InstalledApp]s whenever it changes.
     */
    fun getInstalledApps(): Flow<List<InstalledApp>>

    // --- Blocked App Settings ---

    /**
     * Retrieves a list of all applications the user has marked for blocking.
     * @return A [Flow] that emits the list of [BlockedAppEntity]s, updating with any changes.
     */
    fun getBlockedApps(): Flow<List<BlockedAppEntity>>

    /**
     * Fetches a single blocked app entity by its package name.
     * @param packageName The unique package name of the app to retrieve.
     * @return The [BlockedAppEntity] if found, otherwise `null`.
     */
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?

    /**
     * Adds a new app to the list of blocked applications.
     * @param app The [BlockedAppEntity] to be inserted into the database.
     */
    suspend fun addBlockedApp(app: BlockedAppEntity)

    /**
     * Removes an app from the list of blocked applications using its package name.
     * @param packageName The unique package name of the app to unblock.
     */
    suspend fun removeBlockedApp(packageName: String)


    // --- Daily Usage Tracking ---

    /**
     * Retrieves the usage statistics for a specific app on a given date.
     * @param packageName The package name of the app.
     * @param date The specific [LocalDate] to query usage for.
     * @return The [AppUsageEntity] for that day if it exists, otherwise `null`.
     */
    suspend fun getUsageForAppOnDate(packageName: String, date: LocalDate): AppUsageEntity?

    /**
     * Retrieves the total usage time in milliseconds across all tracked apps for a specific date.
     * @param date The [LocalDate] for which to calculate total usage.
     * @return A [Flow] that emits the total usage in milliseconds, or `0L` if no usage is recorded.
     */
    fun getTotalUsageForDate(date: LocalDate): Flow<Long>

    /**
     * Inserts or updates the usage statistics for an app. If an entry for the given
     * package and date already exists, it will be updated; otherwise, a new one is inserted.
     * @param usage The [AppUsageEntity] to be saved.
     */
    suspend fun upsertUsage(usage: AppUsageEntity)
}