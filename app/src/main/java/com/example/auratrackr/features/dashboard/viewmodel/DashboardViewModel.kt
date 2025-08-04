package com.example.auratrackr.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.repository.AppUsageRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val username: String = "",
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
    private val vibeRepository: VibeRepository, // <-- INJECT THE VIBE REPOSITORY
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = DashboardUiState(isLoading = true)

            // This is now a powerful reactive stream. It listens for changes to the user's profile,
            // the weekly usage data, AND the currently selected vibe.
            combine(
                userRepository.getUserProfile(uid),
                getWeeklyUsageData(),
                vibeRepository.getSelectedVibe().flatMapLatest { selectedVibe ->
                    // When the vibe changes, this inner flow is re-triggered
                    // to fetch the schedule for today that matches the new vibe.
                    workoutRepository.getSchedulesForDateAndVibe(uid, Date(), selectedVibe.id)
                }
            ) { user, weeklyUsage, todaysSchedules ->
                DashboardUiState(
                    isLoading = false,
                    username = user?.username ?: "User",
                    auraPoints = user?.auraPoints ?: 0,
                    weeklyUsage = weeklyUsage,
                    // We'll take the first schedule that matches the date and vibe
                    todaysSchedule = todaysSchedules.firstOrNull()
                )
            }.catch { e ->
                _uiState.value = DashboardUiState(isLoading = false, error = e.message)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun startWorkout(scheduleId: String, workoutId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            workoutRepository.updateWorkoutStatus(uid, scheduleId, workoutId, "ACTIVE")
        }
    }

    private fun getWeeklyUsageData(): Flow<List<Long>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateFlows = (0..6).map { dayOffset ->
            val date = calendar.time
            val dateString = dateFormat.format(date)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            appUsageRepository.getTotalUsageForDate(dateString).map { it ?: 0L }
        }

        return combine(dateFlows) { dailyTotals ->
            dailyTotals.toList()
        }
    }
}
