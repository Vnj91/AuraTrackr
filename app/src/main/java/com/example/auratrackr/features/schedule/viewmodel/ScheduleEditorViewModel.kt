package com.example.auratrackr.features.schedule.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ScheduleEditorUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isNewSchedule: Boolean = false,
    val scheduleId: String? = null,
    val nickname: String = "",
    val workouts: List<Workout> = emptyList(),
    val error: String? = null,
    val showAddActivityDialog: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleEditorViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository,
    private val auth: FirebaseAuth,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleEditorUiState())
    val uiState: StateFlow<ScheduleEditorUiState> = _uiState.asStateFlow()

    private val scheduleIdFlow = savedStateHandle.getStateFlow<String?>("scheduleId", null)

    init {
        observeSchedule()
    }

    private fun observeSchedule() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch

            // Use flatMapLatest to switch to the correct data source (a new schedule or an existing one)
            // whenever the scheduleId changes. This is the correct, efficient way to handle this.
            scheduleIdFlow
                .flatMapLatest { id ->
                    if (id != null) {
                        workoutRepository.getScheduleFlowById(uid, id)
                    } else {
                        // If there's no ID, emit a null to represent a new schedule state.
                        flowOf(null)
                    }
                }
                .collect { schedule ->
                    if (schedule != null) {
                        // We are editing an existing schedule.
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isNewSchedule = false,
                                scheduleId = schedule.id,
                                nickname = schedule.nickname,
                                workouts = schedule.workouts
                            )
                        }
                    } else {
                        // We are creating a new schedule.
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isNewSchedule = true,
                                nickname = "New Schedule" // Default name
                            )
                        }
                    }
                }
        }
    }

    fun onNicknameChange(newNickname: String) {
        _uiState.update { it.copy(nickname = newNickname) }
    }

    fun onAddActivityClicked() {
        viewModelScope.launch {
            if (_uiState.value.isNewSchedule) {
                // If it's a new schedule, we must save it first to get an ID.
                val newId = createNewSchedule()
                if (newId != null) {
                    // Update the SavedStateHandle to trigger the flow observation.
                    savedStateHandle["scheduleId"] = newId
                    // Now that we have an ID, we can show the dialog.
                    _uiState.update { it.copy(showAddActivityDialog = true, isNewSchedule = false) }
                }
            } else {
                // If the schedule already exists, just show the dialog.
                _uiState.update { it.copy(showAddActivityDialog = true) }
            }
        }
    }

    fun onDismissAddActivityDialog() {
        _uiState.update { it.copy(showAddActivityDialog = false) }
    }

    fun saveNewActivity(title: String, description: String) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch

            val uid = auth.currentUser?.uid ?: return@launch
            val scheduleId = _uiState.value.scheduleId ?: return@launch
            val newWorkout = Workout(title = title, description = description)

            workoutRepository.addWorkoutToSchedule(uid, scheduleId, newWorkout)
            onDismissAddActivityDialog()
        }
    }

    fun onDeleteActivityClicked(workoutId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val scheduleId = _uiState.value.scheduleId ?: return@launch
            workoutRepository.deleteWorkoutFromSchedule(uid, scheduleId, workoutId)
        }
    }

    private suspend fun createNewSchedule(): String? {
        _uiState.update { it.copy(isSaving = true) }
        val uid = auth.currentUser?.uid ?: return null
        val selectedVibe = vibeRepository.getSelectedVibeOnce()
        val result = workoutRepository.createNewSchedule(
            uid = uid,
            nickname = _uiState.value.nickname.ifBlank { "My Schedule" },
            date = Date(), // Assuming today's date for new schedules
            vibeId = selectedVibe.id
        )
        _uiState.update { it.copy(isSaving = false) }
        return result.getOrNull()
    }

    fun onSaveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val uid = auth.currentUser?.uid ?: return@launch
            val scheduleId = _uiState.value.scheduleId ?: return@launch

            val result = workoutRepository.updateScheduleNickname(
                uid = uid,
                scheduleId = scheduleId,
                newNickname = _uiState.value.nickname
            )
            // Optionally, update the UI with the result (e.g., show a toast)
            _uiState.update { it.copy(isSaving = false, error = result.exceptionOrNull()?.message) }
        }
    }
}