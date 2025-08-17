package com.example.auratrackr.features.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    val selectedSchedule: Schedule? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    // The single source of truth for the currently selected date.
    private val selectedDateFlow = MutableStateFlow(LocalDate.now())

    init {
        observeScheduleChanges()
    }

    private fun observeScheduleChanges() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // Combine the selected date and vibe flows.
            // This will re-trigger the downstream flatMapLatest whenever either value changes.
            combine(
                selectedDateFlow,
                vibeRepository.getSelectedVibe()
            ) { date, vibe ->
                date to vibe
            }
                .onEach { (date, _) ->
                    // Whenever the date changes, regenerate the calendar view to be centered around it.
                    _uiState.update {
                        it.copy(
                            selectedDate = date,
                            calendarDates = generateCalendarDates(date)
                        )
                    }
                }
                .flatMapLatest { (date, vibe) ->
                    _uiState.update { it.copy(isLoading = true) }
                    // Convert LocalDate to legacy Date for the repository query.
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
                            selectedSchedule = schedules.firstOrNull()
                        )
                    }
                }
        }
    }

    /**
     * Updates the selected date, which will trigger the reactive flow to fetch new data.
     */
    fun onDateSelected(date: LocalDate) {
        selectedDateFlow.value = date
    }

    /**
     * Navigates to the previous day.
     */
    fun onPreviousDateClicked() {
        selectedDateFlow.value = selectedDateFlow.value.minusDays(1)
    }

    /**
     * Navigates to the next day.
     */
    fun onNextDateClicked() {
        selectedDateFlow.value = selectedDateFlow.value.plusDays(1)
    }

    /**
     * Deletes a specific workout from the currently selected schedule.
     * The UI will update automatically due to the real-time flow from Firestore.
     */
    fun onDeleteWorkoutClicked(workoutId: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val scheduleId = _uiState.value.selectedSchedule?.id ?: return@launch
            workoutRepository.deleteWorkoutFromSchedule(uid, scheduleId, workoutId)
        }
    }

    /**
     * Generates a list of dates for the calendar view, centered around a specific date.
     * @param centerDate The date to be the center of the 15-day window.
     * @return A list of [LocalDate] objects.
     */
    private fun generateCalendarDates(centerDate: LocalDate): List<LocalDate> {
        val startDate = centerDate.minusDays(7)
        return List(15) { i ->
            startDate.plusDays(i.toLong())
        }
    }

    /**
     * Formats a [LocalDate] into a user-friendly string (e.g., "8, August 2025").
     */
    fun formatFullDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("d, MMMM yyyy", Locale.getDefault())
        return date.format(formatter)
    }
}