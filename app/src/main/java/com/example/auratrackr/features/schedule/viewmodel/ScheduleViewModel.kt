package com.example.auratrackr.features.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.repository.AuthRepository
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val calendarDates: List<LocalDate> = emptyList(),
    // ✅ FIX: The state now correctly holds a LIST of schedules for the selected date.
    val schedulesForSelectedDate: List<Schedule> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val selectedDateFlow = MutableStateFlow(LocalDate.now())

    // ✅ FIX: Added a trigger flow to allow for manual refreshes from the UI.
    private val refreshTrigger = MutableStateFlow(Unit)

    init {
        observeScheduleChanges()
    }

    private fun observeScheduleChanges() {
        val uid = authRepository.currentUserId() ?: return

        viewModelScope.launch {
            // ✅ FIX: Added the refreshTrigger. Now, when refresh() is called, this entire flow re-executes.
            combine(
                selectedDateFlow,
                vibeRepository.getSelectedVibe(),
                refreshTrigger
            ) { date, vibe, _ ->
                date to vibe
            }
                .onEach { (date, _) ->
                    _uiState.update {
                        it.copy(
                            selectedDate = date,
                            calendarDates = generateCalendarDates(date)
                        )
                    }
                }
                .flatMapLatest { (date, vibe) ->
                    _uiState.update { it.copy(isLoading = true) }
                    val legacyDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
                    workoutRepository.getSchedulesForDateAndVibe(uid, legacyDate, vibe.id)
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { schedules ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            schedulesForSelectedDate = schedules
                        )
                    }
                }
        }
    }

    /**
     * Triggers a manual refresh of the schedule data. This should be called from the UI
     * when the screen becomes visible to ensure data is fresh.
     */
    fun refresh() {
        refreshTrigger.value = Unit
    }

    fun onDateSelected(date: LocalDate) {
        selectedDateFlow.value = date
    }

    fun onPreviousDateClicked() {
        selectedDateFlow.value = selectedDateFlow.value.minusDays(1)
    }

    fun onNextDateClicked() {
        selectedDateFlow.value = selectedDateFlow.value.plusDays(1)
    }

    // ✅ FIX: The function now requires the scheduleId to know which schedule to modify.
    fun onDeleteWorkoutClicked(scheduleId: String, workoutId: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUserId() ?: return@launch
            workoutRepository.deleteWorkoutFromSchedule(uid, scheduleId, workoutId)
        }
    }

    private fun generateCalendarDates(centerDate: LocalDate): List<LocalDate> {
        val startDate = centerDate.minusDays(7)
        return List(15) { i ->
            startDate.plusDays(i.toLong())
        }
    }

    fun formatFullDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("d, MMMM yyyy", Locale.getDefault())
        return date.format(formatter)
    }
}
