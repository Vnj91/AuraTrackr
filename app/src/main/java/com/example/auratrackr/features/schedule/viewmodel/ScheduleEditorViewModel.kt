package com.example.auratrackr.features.schedule.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
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
    val showAddActivityDialog: Boolean = false,
    val availableVibes: List<Vibe> = emptyList(),
    val selectedVibeId: String = "",
    val repeatDays: List<DayOfWeek> = emptyList(),
    val assignedDate: Date? = null
)

// ✅ ADDED: A sealed interface for one-time events
sealed interface ScheduleEditorEvent {
    data class ShowSnackbar(val message: String) : ScheduleEditorEvent
    object NavigateBack : ScheduleEditorEvent
}

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

    // ✅ ADDED: A SharedFlow to emit one-time events to the UI.
    private val _eventFlow = MutableSharedFlow<ScheduleEditorEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    private val scheduleIdFlow = savedStateHandle.getStateFlow<String?>("scheduleId", null)

    init {
        observeScheduleData()
    }

    private fun observeScheduleData() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch

            scheduleIdFlow.flatMapLatest { id ->
                combine(
                    if (id != null) workoutRepository.getScheduleFlowById(uid, id) else flowOf(null),
                    vibeRepository.getAvailableVibes()
                ) { schedule, vibes ->
                    schedule to vibes
                }
            }.collect { (schedule, vibes) ->
                if (schedule != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNewSchedule = false,
                            scheduleId = schedule.id,
                            nickname = schedule.nickname,
                            workouts = schedule.workouts,
                            availableVibes = vibes,
                            selectedVibeId = schedule.vibeId.ifEmpty { vibes.firstOrNull()?.id ?: "" },
                            repeatDays = schedule.repeatDays.map { dayString -> DayOfWeek.valueOf(dayString) },
                            assignedDate = schedule.assignedDate
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isNewSchedule = true,
                            nickname = "New Schedule",
                            availableVibes = vibes,
                            selectedVibeId = vibes.firstOrNull()?.id ?: "",
                            assignedDate = Date()
                        )
                    }
                }
            }
        }
    }

    fun onNicknameChange(newNickname: String) {
        _uiState.update { it.copy(nickname = newNickname) }
    }

    fun onVibeSelected(vibeId: String) {
        _uiState.update { it.copy(selectedVibeId = vibeId) }
    }

    fun onRepeatDaySelected(day: DayOfWeek, isSelected: Boolean) {
        _uiState.update { currentState ->
            val updatedDays = if (isSelected) {
                currentState.repeatDays + day
            } else {
                currentState.repeatDays - day
            }
            currentState.copy(repeatDays = updatedDays.distinct(), assignedDate = if (updatedDays.isNotEmpty()) null else currentState.assignedDate)
        }
    }

    fun onDateSelected(date: Date) {
        _uiState.update { it.copy(assignedDate = date, repeatDays = emptyList()) }
    }


    fun onAddActivityClicked() {
        viewModelScope.launch {
            if (_uiState.value.isNewSchedule) {
                val newId = createNewSchedule()
                if (newId != null) {
                    savedStateHandle["scheduleId"] = newId
                    _uiState.update { it.copy(showAddActivityDialog = true, isNewSchedule = false) }
                }
            } else {
                _uiState.update { it.copy(showAddActivityDialog = true) }
            }
        }
    }

    fun onDismissAddActivityDialog() {
        _uiState.update { it.copy(showAddActivityDialog = false) }
    }

    fun saveNewActivity(title: String, description: String, durationInMinutes: Long) {
        viewModelScope.launch {
            if (title.isBlank()) return@launch

            val uid = auth.currentUser?.uid ?: return@launch
            val scheduleId = _uiState.value.scheduleId ?: return@launch
            val newWorkout = Workout(
                title = title,
                description = description,
                durationInSeconds = durationInMinutes * 60
            )

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
        val result = workoutRepository.createNewSchedule(
            uid = uid,
            schedule = _uiState.value.let {
                Schedule(
                    nickname = it.nickname.ifBlank { "My Schedule" },
                    vibeId = it.selectedVibeId,
                    repeatDays = it.repeatDays.map { day -> day.name },
                    assignedDate = it.assignedDate
                )
            }
        )
        _uiState.update { it.copy(isSaving = false) }
        return result.getOrNull()
    }

    fun onSaveChanges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val uid = auth.currentUser?.uid ?: return@launch
            val scheduleId = _uiState.value.scheduleId ?: return@launch
            val scheduleToUpdate = _uiState.value.let {
                Schedule(
                    id = scheduleId,
                    nickname = it.nickname,
                    vibeId = it.selectedVibeId,
                    repeatDays = it.repeatDays.map { day -> day.name },
                    assignedDate = it.assignedDate,
                    workouts = it.workouts
                )
            }

            val result = workoutRepository.updateSchedule(uid, scheduleToUpdate)
            _uiState.update { it.copy(isSaving = false) }

            // ✅ FIX: Emit events instead of updating the UI state directly.
            if (result.isSuccess) {
                _eventFlow.emit(ScheduleEditorEvent.ShowSnackbar("Schedule saved!"))
                _eventFlow.emit(ScheduleEditorEvent.NavigateBack)
            } else {
                _eventFlow.emit(ScheduleEditorEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to save."))
            }
        }
    }
}
