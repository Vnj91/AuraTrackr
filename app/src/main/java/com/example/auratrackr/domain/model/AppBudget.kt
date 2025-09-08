package com.example.auratrackr.domain.model

// Represents the daily usage budget for a monitored app.
data class AppBudget(
    val packageName: String,
    val timeBudgetInMinutes: Long
)
