package com.example.auratrackr.features.schedule.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ScheduleEditorUiState(
    val isLoading: Boolean = true,
    val isNewSchedule: Boolean = false,
    val scheduleId: String = "",
    val nickname: String = "",
    val workouts: List<Workout> = emptyList(),
    val saveResult: Result<Unit>? = null,
    val showAddActivityDialog: Boolean = false // <-- ADDED THIS LINE
)

@HiltViewModel
class ScheduleEditorViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleEditorUiState())
    val uiState: StateFlow<ScheduleEditorUiState> = _uiState.asStateFlow()

    private val scheduleId: String? = savedStateHandle.get<String>("scheduleId")

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            if (scheduleId != null) {
                // Editing an existing schedule
                val schedule = workoutRepository.getScheduleById(uid, scheduleId)
                if (schedule != null) {
                    _uiState.value = ScheduleEditorUiState(
                        isLoading = false,
                        isNewSchedule = false,
                        scheduleId = schedule.id,
                        nickname = schedule.nickname,
                        workouts = schedule.workouts
                    )
                }
            } else {
                // Creating a new schedule
                _uiState.value = ScheduleEditorUiState(isLoading = false, isNewSchedule = true, nickname = "New Schedule")
            }
        }
    }

    fun onNicknameChange(newNickname: String) {
        _uiState.value = _uiState.value.copy(nickname = newNickname)
    }

    fun onAddActivityClicked() {
        // If it's a new schedule, save it first to get an ID
        if (_uiState.value.isNewSchedule) {
            onSaveChanges()
        }
        _uiState.value = _uiState.value.copy(showAddActivityDialog = true)
    }

    fun onDismissAddActivityDialog() {
        _uiState.value = _uiState.value.copy(showAddActivityDialog = false)
    }

    /**
     * Saves a new custom activity to the current schedule.
     */
    fun saveNewActivity(title: String, description: String) {
        viewModelScope.launch {
            if (_uiState.value.scheduleId.isNotEmpty()) {
                workoutRepository.addWorkoutToSchedule(
                    uid = auth.currentUser!!.uid,
                    scheduleId = _uiState.value.scheduleId,
                    title = title,
                    description = description
                )
            }
            onDismissAddActivityDialog()
        }
    }

    fun onDeleteActivityClicked(workoutId: String) {
        viewModelScope.launch {
            workoutRepository.deleteWorkoutFromSchedule(
                uid = auth.currentUser!!.uid,
                scheduleId = _uiState.value.scheduleId,
                workoutId = workoutId
            )
        }
    }

    fun onSaveChanges() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            if (_uiState.value.isNewSchedule) {
                val selectedVibe = vibeRepository.getSelectedVibe().first()
                val result = workoutRepository.createNewSchedule(
                    uid = uid,
                    nickname = _uiState.value.nickname,
                    date = Date(), // Or pass this in as a nav arg
                    vibeId = selectedVibe.id
                )
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(isNewSchedule = false, scheduleId = result.getOrThrow())
                }
            } else {
                workoutRepository.updateScheduleNickname(
                    uid = uid,
                    scheduleId = _uiState.value.scheduleId,
                    newNickname = _uiState.value.nickname
                )
            }
        }
    }
}
