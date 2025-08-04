package com.example.auratrackr.domain.model

import java.util.Date

/**
 * Represents a single, named workout schedule created by the user.
 * A schedule contains a list of individual workout activities.
 *
 * @property id A unique identifier for the schedule.
 * @property nickname The user-given name for the schedule (e.g., "Morning Routine", "Weightlifting Day").
 * @property workouts The list of individual workout activities that make up this schedule.
 * @property assignedDate The specific date this schedule is planned for. Can be null if it's just a template.
 */
data class Schedule(
    val id: String,
    val nickname: String,
    val workouts: List<Workout>,
    val assignedDate: Date? = null
)
