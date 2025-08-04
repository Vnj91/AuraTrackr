package com.example.auratrackr.domain.repository

import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import com.example.auratrackr.domain.model.InstalledApp
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for all app-related data operations.
 */
interface AppUsageRepository {

    // --- Installed App Info ---
    fun getInstalledApps(): Flow<List<InstalledApp>>

    // --- Blocked App Settings (from blocked_apps table) ---
    fun getBlockedApps(): Flow<List<BlockedAppEntity>>
    suspend fun addBlockedApp(app: BlockedAppEntity)
    suspend fun removeBlockedApp(packageName: String)
    suspend fun getBlockedApp(packageName: String): BlockedAppEntity?

    // --- Daily Usage Tracking (from app_usage_stats table) ---
    suspend fun getUsageForAppOnDate(packageName: String, date: String): AppUsageEntity?
    fun getTotalUsageForDate(date: String): Flow<Long?>
    suspend fun upsertUsage(usage: AppUsageEntity)
}
