package com.example.auratrackr.features.focus.tracking

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.domain.repository.AppUsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A class responsible for the core logic of tracking app usage and checking it against user-set budgets.
 *
 * This tracker is designed to be run periodically by a background service. It queries the system's
 * usage stats, compares them against the user's defined limits, updates the in-memory blocker state,
 * and persists the latest usage data to the local database.
 *
 * @param context The application context.
 * @param appUsageRepository The repository for accessing app data.
 * @param blockerState The singleton state holder for apps that are over budget.
 */
@Singleton
class UsageTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appUsageRepository: AppUsageRepository,
    private val blockerState: BlockerState
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    companion object {
        private const val TAG = "UsageTracker"
    }

    /**
     * Performs a usage tracking cycle. It calculates the usage for all monitored apps since
     * the beginning of the day and updates the local database and the blocker state accordingly.
     */
    suspend fun trackUsage() {
        // 1. Get the list of apps to monitor from our local database.
        val monitoredApps = appUsageRepository.getBlockedApps().firstOrNull()
        if (monitoredApps.isNullOrEmpty()) {
            Log.d(TAG, "No monitored apps found. Skipping usage track.")
            blockerState.clearAll() // Ensure state is clear if no apps are monitored
            return
        }

        val monitoredAppsMap = monitoredApps.associateBy { it.packageName }
        val today = LocalDate.now()

        // 2. Set the time range for the query: from the start of today until now.
        val startTime = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = System.currentTimeMillis()

        // 3. Query the system for usage stats.
        val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
        if (usageStatsList.isNullOrEmpty()) {
            Log.d(TAG, "UsageStatsManager returned no data for the time range.")
            return
        }

        Log.d(TAG, "Processing usage stats for ${usageStatsList.size} apps.")

        // 4. Process the stats and update our database and blocker state.
        for (stat in usageStatsList) {
            val budgetInfo = monitoredAppsMap[stat.packageName] ?: continue // Skip if not a monitored app

            val totalTimeInMinutes = stat.totalTimeInForeground / (1000 * 60)

            // --- Budget Check Logic ---
            if (totalTimeInMinutes > budgetInfo.timeBudgetInMinutes) {
                blockerState.addApp(stat.packageName)
                Log.i(TAG, "App '${stat.packageName}' is OVER budget ($totalTimeInMinutes / ${budgetInfo.timeBudgetInMinutes} mins). Adding to block list.")
            } else {
                blockerState.removeApp(stat.packageName)
            }

            // --- Database Update Logic ---
            // TODO: Implement a robust way to track launch counts. `stat.getLaunchCount()` is deprecated and unreliable.
            val newUsage = AppUsageEntity(
                packageName = stat.packageName,
                date = today,
                usageInMinutes = totalTimeInMinutes,
                launchCount = 0 // Placeholder until launch count is tracked
            )

            appUsageRepository.upsertUsage(newUsage)
        }
    }
}