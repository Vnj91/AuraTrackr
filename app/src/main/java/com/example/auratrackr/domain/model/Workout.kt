package com.example.auratrackr.domain.model

/**
 * Represents the status of a workout in the user's schedule.
 */
enum class WorkoutStatus {
    PENDING,
    ACTIVE,
    COMPLETED
}

/**
 * Represents a single workout session in the user's daily schedule.
 * This data class holds all the necessary information to display a workout
 * item in the dashboard timeline.
 *
 * @property id A unique identifier for this workout instance.
 * @property title The name of the workout (e.g., "WarmUp", "Muscle Up").
 * @property description A summary of the workout details (e.g., "Run 02 km", "10 reps, 3 sets...").
 * @property status The current status of the workout, which will determine its visual state in the UI.
 */
data class Workout(
    val id: String,
    val title: String,
    val description: String,
    val status: WorkoutStatus
)
