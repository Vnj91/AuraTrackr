package com.example.auratrackr.features.focus.tracking

import android.app.usage.UsageStatsManager
import android.content.Context
import com.example.auratrackr.data.local.entity.AppUsageEntity
import com.example.auratrackr.domain.repository.AppUsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A class responsible for the core logic of tracking app usage and checking it against user-set budgets.
 *
 * @param context The application context.
 * @param appUsageRepository The repository for accessing app data.
 * @param blockerState The singleton state holder for apps that are over budget.
 */
@Singleton
class UsageTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appUsageRepository: AppUsageRepository,
    private val blockerState: BlockerState // Injects the state holder
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    /**
     * The main function to perform a usage tracking cycle.
     * It calculates the usage for all monitored apps since the beginning of the day
     * and updates the local database and the blocker state.
     */
    suspend fun trackUsage() {
        // 1. Get the list of apps we need to monitor from our database.
        val monitoredApps = appUsageRepository.getBlockedApps().firstOrNull() ?: return
        if (monitoredApps.isEmpty()) return

        // Create a map for quick lookups of budget info.
        val monitoredAppsMap = monitoredApps.associateBy { it.packageName }

        // 2. Set the time range for our query: from the start of today until now.
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // 3. Query the system for usage stats.
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // 4. Process the stats and update our database and blocker state.
        for (stat in usageStats) {
            val budgetInfo = monitoredAppsMap[stat.packageName]
            // Proceed only if the app is in our monitored list.
            if (budgetInfo != null) {
                val todayDateString = getTodayDateString()
                val totalTimeInMinutes = (stat.totalTimeInForeground / 1000 / 60).toLong()

                // --- LOGIC TO CHECK BUDGET ---
                // If usage exceeds the budget, add it to the blocked set.
                if (totalTimeInMinutes > budgetInfo.timeBudgetInMinutes) {
                    blockerState.addApp(stat.packageName)
                } else {
                    // Otherwise, ensure it's not in the blocked set.
                    blockerState.removeApp(stat.packageName)
                }

                // --- LOGIC TO SAVE USAGE (from previous step) ---
                val existingUsage = appUsageRepository.getUsageForAppOnDate(stat.packageName, todayDateString)
                val newUsage = AppUsageEntity(
                    id = existingUsage?.id ?: 0, // Keep existing ID to ensure replacement (upsert)
                    packageName = stat.packageName,
                    date = todayDateString,
                    usageInMinutes = totalTimeInMinutes,
                    launchCount = existingUsage?.launchCount ?: 0 // We will handle launch count later
                )

                // 5. Save the updated usage data back to our database.
                appUsageRepository.upsertUsage(newUsage)
            }
        }
    }

    /**
     * Helper function to get the current date in a consistent YYYY-MM-DD format.
     */
    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
