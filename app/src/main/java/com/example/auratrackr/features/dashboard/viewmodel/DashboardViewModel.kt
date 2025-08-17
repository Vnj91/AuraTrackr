package com.example.auratrackr.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.domain.repository.AppUsageRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val username: String = "User",
    val profilePictureUrl: String? = null,
    val auraPoints: Int = 0,
    val weeklyUsage: List<Long> = List(7) { 0L },
    val todaysSchedule: Schedule? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val currentDate = MutableStateFlow(LocalDate.now())

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Combine multiple data sources into a single UI state.
            // This ensures the UI is always consistent and reacts to any data change.
            combine(
                userRepository.getUserProfile(uid),
                getWeeklyUsageData(),
                // This inner combine ensures that when EITHER the date OR the vibe changes,
                // flatMapLatest will cancel the old query and start a new one.
                combine(currentDate, vibeRepository.getSelectedVibe()) { date, vibe ->
                    date to vibe
                }.flatMapLatest { (date, selectedVibe) ->
                    // Convert LocalDate to legacy Date for the repository query
                    val legacyDate = Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                    workoutRepository.getSchedulesForDateAndVibe(uid, legacyDate, selectedVibe.id)
                }
            ) { user, weeklyUsage, todaysSchedules ->
                // Map the combined results to our UI state object.
                DashboardUiState(
                    isLoading = false,
                    username = user?.username ?: "User",
                    profilePictureUrl = user?.profilePictureUrl,
                    auraPoints = user?.auraPoints ?: 0,
                    weeklyUsage = weeklyUsage,
                    todaysSchedule = todaysSchedules.firstOrNull(),
                    error = null
                )
            }
                .onStart { emit(_uiState.value.copy(isLoading = true)) }
                .catch { e ->
                    emit(_uiState.value.copy(isLoading = false, error = e.message))
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /**
     * Updates a workout's status to ACTIVE.
     */
    fun startWorkout(scheduleId: String, workoutId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            workoutRepository.updateWorkoutStatus(uid, scheduleId, workoutId, WorkoutStatus.ACTIVE)
        }
    }

    /**
     * Refreshes the current date to today. This can be called from the UI
     * when the screen becomes visible (e.g., in onResume) to ensure data is current.
     */
    fun refreshDate() {
        currentDate.value = LocalDate.now()
    }

    /**
     * Fetches the total app usage for each day of the current week (Mon-Sun).
     * @return A [Flow] that emits a list of 7 Longs, representing usage in minutes for each day.
     */
    private fun getWeeklyUsageData(): Flow<List<Long>> {
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)

        val dateFlows = (0..6).map { dayOffset ->
            val date = monday.plusDays(dayOffset.toLong())
            appUsageRepository.getTotalUsageForDate(date)
        }

        return combine(dateFlows) { dailyTotals ->
            dailyTotals.toList()
        }
    }
}