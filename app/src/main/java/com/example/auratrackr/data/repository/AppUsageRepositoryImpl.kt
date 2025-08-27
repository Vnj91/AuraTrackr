package com.example.auratrackr.data.repository

import android.content.Context
import android.content.Intent
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
import java.time.LocalDate
import javax.inject.Inject

class AppUsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blockedAppDao: BlockedAppDao,
    private val appUsageDao: AppUsageDao
) : AppUsageRepository {

    private val packageManager: PackageManager = context.packageManager

    /**
     * Fetches the list of user-launchable, non-system applications.
     * The app's own package is also filtered out from the list.
     * The operation is performed on the IO dispatcher as it can be intensive.
     */
    override fun getInstalledApps(): Flow<List<InstalledApp>> = flow {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val apps = packageManager.queryIntentActivities(mainIntent, 0)
            .filter { it.activityInfo.packageName != context.packageName } // Filter out our own app
            .map { resolveInfo ->
                InstalledApp(
                    name = resolveInfo.loadLabel(packageManager).toString(),
                    packageName = resolveInfo.activityInfo.packageName
                )
            }
            // ✅ FIX: Ensure the list is unique by package name to prevent crashes in LazyColumn.
            .distinctBy { it.packageName }
            .sortedBy { it.name.lowercase() }

        emit(apps)
    }.flowOn(Dispatchers.IO)

    override fun getBlockedApps(): Flow<List<BlockedAppEntity>> {
        return blockedAppDao.getBlockedApps()
    }

    override suspend fun getBlockedApp(packageName: String): BlockedAppEntity? {
        return blockedAppDao.getBlockedApp(packageName)
    }

    override suspend fun addBlockedApp(app: BlockedAppEntity) {
        blockedAppDao.upsertBlockedApp(app)
    }

    override suspend fun removeBlockedApp(packageName: String) {
        blockedAppDao.deleteBlockedApp(packageName)
    }

    override suspend fun getUsageForAppOnDate(packageName: String, date: LocalDate): AppUsageEntity? {
        return appUsageDao.getUsageForAppOnDate(packageName, date)
    }

    override fun getTotalUsageForDate(date: LocalDate): Flow<Long> {
        return appUsageDao.getTotalUsageForDate(date)
    }

    override suspend fun upsertUsage(usage: AppUsageEntity) {
        appUsageDao.upsertUsage(usage)
    }
}
