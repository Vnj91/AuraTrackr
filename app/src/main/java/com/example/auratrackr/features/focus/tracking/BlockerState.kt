package com.example.auratrackr.features.focus.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton class that holds the in-memory state of which apps are currently over their usage budget.
 *
 * This acts as a thread-safe, observable communication bridge between the background [UsageTracker]
 * (which adds apps to this state) and the foreground [com.example.auratrackr.features.focus.service.FocusAccessibilityService]
 * (which reads this state to determine if an app should be blocked).
 */
@Singleton
class BlockerState @Inject constructor() {

    private val _appsOverBudget = MutableStateFlow<Set<String>>(emptySet())
    val appsOverBudget = _appsOverBudget.asStateFlow()

    /**
     * Atomically adds an app's package name to the set of apps that are over budget.
     *
     * @param packageName The unique package name of the app to add.
     */
    fun addApp(packageName: String) {
        _appsOverBudget.update { currentSet ->
            currentSet + packageName
        }
    }

    /**
     * Atomically removes an app's package name from the set of apps that are over budget.
     *
     * @param packageName The unique package name of the app to remove.
     */
    fun removeApp(packageName: String) {
        _appsOverBudget.update { currentSet ->
            currentSet - packageName
        }
    }

    /**
     * Clears all apps from the over-budget state.
     */
    fun clearAll() {
        _appsOverBudget.value = emptySet()
    }
}
