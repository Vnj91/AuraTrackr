package com.example.auratrackr.features.workout.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

// A sealed class to represent navigation events from the ViewModel to the UI
sealed class WorkoutNavigationEvent {
    object NavigateToSuccess : WorkoutNavigationEvent()
    object NavigateToNextWorkout : WorkoutNavigationEvent()
    object FinishSession : WorkoutNavigationEvent()
}

data class WorkoutSessionUiState(
    val currentWorkout: Workout? = null,
    val elapsedTime: Long = 0L,
    val isTimerRunning: Boolean = false,
    val progress: Float = 0f // 0.0f to 1.0f
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

    companion object {
        private const val POINTS_PER_WORKOUT = 50
    }

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            // For now, we fetch today's schedule. This could be passed via nav args later.
            currentSchedule = workoutRepository.getScheduleForDate(Date()).firstOrNull()
            startCurrentWorkout()
        }
    }

    private fun startCurrentWorkout() {
        currentSchedule?.let { schedule ->
            if (currentWorkoutIndex < schedule.workouts.size) {
                val workout = schedule.workouts[currentWorkoutIndex]
                _uiState.value = WorkoutSessionUiState(currentWorkout = workout)
                // We could start the timer automatically or wait for user input
                // For now, we'll wait for the user to press play.
            } else {
                // All workouts in the schedule are done
                viewModelScope.launch { _navigationEvent.emit(WorkoutNavigationEvent.FinishSession) }
            }
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
        _uiState.value = _uiState.value.copy(isTimerRunning = true)
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(elapsedTime = _uiState.value.elapsedTime + 1)
                // TODO: Update progress based on a target time if one exists
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = false)
    }

    fun onMarkAsDoneClicked() {
        viewModelScope.launch {
            pauseTimer()
            val workoutId = _uiState.value.currentWorkout?.id ?: return@launch
            workoutRepository.finishWorkout(workoutId)
            val uid = auth.currentUser?.uid
            if (uid != null) {
                userRepository.addAuraPoints(uid, POINTS_PER_WORKOUT)
            }
            _navigationEvent.emit(WorkoutNavigationEvent.NavigateToSuccess)
        }
    }

    fun onSkipClicked() {
        pauseTimer()
        currentWorkoutIndex++
        startCurrentWorkout()
    }

    fun onResetClicked() {
        pauseTimer()
        _uiState.value = _uiState.value.copy(elapsedTime = 0L, isTimerRunning = false)
    }

    /**
     * Called from the UI after the SuccessScreen is shown.
     */
    fun onContinueToNextWorkout() {
        currentWorkoutIndex++
        startCurrentWorkout()
    }
}
