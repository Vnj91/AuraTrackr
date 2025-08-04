package com.example.auratrackr.features.focus.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton class that holds the in-memory state of which apps are currently over their usage budget.
 * This acts as a communication bridge between the background UsageTracker and the foreground AccessibilityService.
 */
@Singleton
class BlockerState @Inject constructor() {

    private val _appsOverBudget = MutableStateFlow<Set<String>>(emptySet())
    val appsOverBudget = _appsOverBudget.asStateFlow()

    /**
     * Adds an app's package name to the set of apps that are over budget.
     */
    fun addApp(packageName: String) {
        _appsOverBudget.value = _appsOverBudget.value + packageName
    }

    /**
     * Removes an app's package name from the set of apps that are over budget.
     */
    fun removeApp(packageName: String) {
        _appsOverBudget.value = _appsOverBudget.value - packageName
    }
}
