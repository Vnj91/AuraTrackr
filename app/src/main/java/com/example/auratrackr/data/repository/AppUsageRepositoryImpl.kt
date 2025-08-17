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
import java.time.LocalDate
import javax.inject.Inject

/**
 * The concrete implementation of the [AppUsageRepository].
 *
 * This class uses the Android [PackageManager] to fetch data about installed apps
 * and the DAOs to manage settings and usage stats in the local Room database.
 * It runs potentially long-running operations on the IO dispatcher.
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

    /**
     * Fetches the list of user-installed, non-system applications.
     * The app's own package is also filtered out from the list.
     * The operation is performed on the IO dispatcher as it can be intensive.
     */
    override fun getInstalledApps(): Flow<List<InstalledApp>> = flow {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 && it.packageName != context.packageName }
            .map { appInfo ->
                InstalledApp(
                    name = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName
                )
            }
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