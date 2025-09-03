package com.example.auratrackr.features.dashboard.viewmodel

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.example.auratrackr.features.focus.service.UsageTrackingService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Date
import javax.inject.Inject

// ✅ UPDATE: The UI state now holds the new points-per-vibe data.
data class DashboardUiState(
    val isLoading: Boolean = true,
    val username: String = "User",
    val profilePictureUrl: String? = null,
    val auraPoints: Int = 0,
    val pointsByVibe: Map<Vibe, Int> = emptyMap(),
    val todaysSchedule: Schedule? = null,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val currentDate = MutableStateFlow(LocalDate.now())

    init {
        ensureTrackingServiceIsRunning()
        loadDashboardData()
    }

    private fun ensureTrackingServiceIsRunning() {
        if (!isServiceRunning(UsageTrackingService::class.java)) {
            val serviceIntent = Intent(context, UsageTrackingService::class.java).apply {
                action = UsageTrackingService.ACTION_START_SERVICE
            }
            context.startService(serviceIntent)
        }
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // ✅ UPDATE: The combine operator now includes the points history and available vibes.
            combine(
                userRepository.getUserProfile(uid),
                userRepository.getPointsHistory(uid),
                vibeRepository.getAvailableVibes(),
                currentDate.flatMapLatest { date ->
                    val legacyDate = Date.from(date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                    // The selected vibe is now fetched inside here to correctly trigger schedule reloads.
                    vibeRepository.getSelectedVibe().flatMapLatest { selectedVibe ->
                        workoutRepository.getSchedulesForDateAndVibe(uid, legacyDate, selectedVibe.id)
                    }
                }
            ) { user, pointsHistory, availableVibes, todaysSchedules ->

                // ✅ NEW: Logic to process the points history and group it by Vibe.
                val vibesMap = availableVibes.associateBy { it.id }
                val pointsByVibe = pointsHistory
                    .groupBy { it.vibeId }
                    .mapValues { (_, entries) -> entries.sumOf { it.points } }
                    .mapNotNull { (vibeId, points) ->
                        vibesMap[vibeId]?.let { vibe -> vibe to points }
                    }
                    .toMap()

                DashboardUiState(
                    isLoading = false,
                    username = user?.username ?: "User",
                    profilePictureUrl = user?.profilePictureUrl,
                    auraPoints = user?.auraPoints ?: 0,
                    pointsByVibe = pointsByVibe, // Pass the new data to the UI state.
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

    fun startWorkout(scheduleId: String, workoutId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            workoutRepository.updateWorkoutStatus(uid, scheduleId, workoutId, WorkoutStatus.ACTIVE)
        }
    }

    fun refreshDate() {
        currentDate.value = LocalDate.now()
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

