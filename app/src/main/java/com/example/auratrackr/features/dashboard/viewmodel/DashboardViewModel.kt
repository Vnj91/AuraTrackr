package com.example.auratrackr.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.repository.AppUsageRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val auraPoints: Int = 0, // <-- ADDED THIS LINE
    val weeklyUsage: List<Long> = List(7) { 0L },
    val todaysSchedule: List<Workout> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appUsageRepository: AppUsageRepository,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
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

            combine(
                userRepository.getUserProfile(uid),
                getWeeklyUsageData(),
                workoutRepository.getTodaysSchedule()
            ) { user, weeklyUsage, todaysSchedule ->
                DashboardUiState(
                    isLoading = false,
                    username = user?.username ?: "User",
                    auraPoints = user?.auraPoints ?: 0, // <-- POPULATE THE POINTS
                    weeklyUsage = weeklyUsage,
                    todaysSchedule = todaysSchedule
                )
            }.catch { e ->
                _uiState.value = DashboardUiState(isLoading = false, error = e.message)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun startWorkout(id: String) {
        viewModelScope.launch {
            workoutRepository.startWorkout(id)
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
