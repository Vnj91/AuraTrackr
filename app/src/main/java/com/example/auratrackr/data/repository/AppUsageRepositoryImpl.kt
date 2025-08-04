package com.example.auratrackr.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.auratrackr.data.local.dao.AppUsageDao
import com.example.auratrackr.data.local.dao.BlockedAppDao
import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.data.local.entity.BlockedAppEntity
import com.example.auratrackr.domain.model.InstalledApp
import com.example.auratrackr.domain.repository.AppUsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * The concrete implementation of the AppUsageRepository.
 * This class uses the Android PackageManager to fetch data about installed apps
 * and the DAOs to manage settings and usage stats in the local database.
 *
 * @param context The application context, injected by Hilt.
 * @param blockedAppDao The Data Access Object for the blocked apps table, injected by Hilt.
 * @param appUsageDao The Data Access Object for the app usage stats table, injected by Hilt.
 */
class AppUsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedAppDao: BlockedAppDao,
    private val appUsageDao: AppUsageDao
) : AppUsageRepository {

    private val packageManager: PackageManager = context.packageManager

    // --- Installed App Info ---

    /**
     * Fetches the list of installed apps from the PackageManager.
     * This operation is performed on the IO dispatcher as it can be intensive.
     */
    override fun getInstalledApps(): Flow<List<InstalledApp>> = flow {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter {
                // Filter to get only user-installed apps (not system apps)
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                        // Also filter out our own app
                        it.packageName != context.packageName
            }
            .map { appInfo ->
                // Map the ApplicationInfo to our simpler InstalledApp model
                InstalledApp(
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    icon = appInfo.loadIcon(packageManager)
                )
            }
            .sortedBy { it.name.lowercase() } // Sort alphabetically

        emit(apps)
    }.flowOn(Dispatchers.IO)

    // --- Blocked App Settings (from blocked_apps table) ---

    override fun getBlockedApps(): Flow<List<BlockedAppEntity>> {
        return blockedAppDao.getBlockedApps()
    }

    override suspend fun addBlockedApp(app: BlockedAppEntity) {
        blockedAppDao.upsertBlockedApp(app)
    }

    override suspend fun removeBlockedApp(packageName: String) {
        blockedAppDao.deleteBlockedApp(packageName)
    }

    override suspend fun getBlockedApp(packageName: String): BlockedAppEntity? {
        return blockedAppDao.getBlockedApp(packageName)
    }

    // --- Daily Usage Tracking (from app_usage_stats table) ---

    override suspend fun getUsageForAppOnDate(packageName: String, date: String): AppUsageEntity? {
        return appUsageDao.getUsageForAppOnDate(packageName, date)
    }

    override fun getTotalUsageForDate(date: String): Flow<Long?> {
        return appUsageDao.getTotalUsageForDate(date)
    }

    override suspend fun upsertUsage(usage: AppUsageEntity) {
        appUsageDao.upsertUsage(usage)
    }
}
