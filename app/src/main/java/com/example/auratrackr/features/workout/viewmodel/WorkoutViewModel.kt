package com.example.auratrackr.features.workout.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WorkoutNavigationEvent {
    object NavigateToSuccess : WorkoutNavigationEvent
    object FinishSession : WorkoutNavigationEvent
}

data class WorkoutSessionUiState(
    val isLoading: Boolean = true,
    val currentWorkout: Workout? = null,
    val elapsedTime: Long = 0L,
    val isTimerRunning: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutSessionUiState())
    val uiState: StateFlow<WorkoutSessionUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<WorkoutNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var timerJob: Job? = null
    private var currentSchedule: Schedule? = null
    private var currentWorkoutIndex = 0

    private val scheduleId: String = savedStateHandle.get<String>("scheduleId") ?: ""
    private val initialWorkoutId: String = savedStateHandle.get<String>("workoutId") ?: ""

    companion object {
        private const val POINTS_PER_WORKOUT = 50
        private const val DEFAULT_WORKOUT_DURATION_SECONDS = 60L
    }

    init {
        loadScheduleAndStartWorkout()
    }

    private fun loadScheduleAndStartWorkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated.") }
                return@launch
            }

            try {
                currentSchedule = workoutRepository.getScheduleFlowById(uid, scheduleId).firstOrNull()
                if (currentSchedule == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Schedule not found.") }
                    return@launch
                }

                currentWorkoutIndex = currentSchedule?.workouts?.indexOfFirst { it.id == initialWorkoutId }?.takeIf { it >= 0 } ?: 0

                _uiState.update { it.copy(isLoading = false) }
                startCurrentWorkout()

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load workout.") }
            }
        }
    }

    private fun startCurrentWorkout() {
        val schedule = currentSchedule ?: return
        if (currentWorkoutIndex < schedule.workouts.size) {
            val workout = schedule.workouts[currentWorkoutIndex]
            _uiState.value = WorkoutSessionUiState(isLoading = false, currentWorkout = workout)

            viewModelScope.launch {
                val uid = auth.currentUser?.uid ?: return@launch
                workoutRepository.updateWorkoutStatus(uid, scheduleId, workout.id, WorkoutStatus.ACTIVE)
            }
        } else {
            viewModelScope.launch { _navigationEvent.emit(WorkoutNavigationEvent.FinishSession) }
        }
    }

    fun onPlayPauseClicked() {
        if (_uiState.value.isTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        _uiState.update { it.copy(isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            val duration = _uiState.value.currentWorkout?.durationInSeconds?.takeIf { it > 0 } ?: DEFAULT_WORKOUT_DURATION_SECONDS
            while (true) {
                delay(1000L)
                val newElapsed = _uiState.value.elapsedTime + 1
                val progress = (newElapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                _uiState.update { it.copy(elapsedTime = newElapsed, progress = progress) }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(isTimerRunning = false) }
    }

    fun onMarkAsDoneClicked() {
        viewModelScope.launch {
            pauseTimer()
            val workoutId = _uiState.value.currentWorkout?.id ?: return@launch
            val uid = auth.currentUser?.uid ?: return@launch
            val schedule = currentSchedule ?: return@launch

            // Update workout status and award points, now including the vibeId.
            workoutRepository.updateWorkoutStatus(uid, scheduleId, workoutId, WorkoutStatus.COMPLETED)
            userRepository.addAuraPoints(uid, POINTS_PER_WORKOUT, schedule.vibeId)

            _navigationEvent.emit(WorkoutNavigationEvent.NavigateToSuccess)
        }
    }

    fun onSkipClicked() {
        pauseTimer()
        moveToNextWorkout()
    }

    fun onResetClicked() {
        pauseTimer()
        _uiState.update { it.copy(elapsedTime = 0L, isTimerRunning = false, progress = 0f) }
    }

    fun onContinueToNextWorkout() {
        moveToNextWorkout()
    }

    private fun moveToNextWorkout() {
        currentWorkoutIndex++
        startCurrentWorkout()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
