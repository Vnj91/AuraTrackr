package com.example.auratrackr.features.schedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Schedule
import com.example.auratrackr.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ScheduleUiState(
    val selectedDate: Date = Date(),
    val calendarDates: List<Date> = emptyList(),
    val selectedSchedule: Schedule? = null, // <-- ADDED THIS LINE
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository // <-- INJECT THE REPOSITORY
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    // A flow that holds the currently selected date
    private val selectedDateFlow = MutableStateFlow(Date())

    init {
        generateCalendarDates()

        // This is a reactive way to load data. Whenever the selectedDateFlow changes,
        // this block will automatically re-execute and fetch the schedule for the new date.
        viewModelScope.launch {
            selectedDateFlow.flatMapLatest { date ->
                _uiState.update { it.copy(isLoading = true) }
                workoutRepository.getScheduleForDate(date)
            }.collect { schedule ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedSchedule = schedule
                    )
                }
            }
        }
    }

    /**
     * Sets the user-selected date as the current date.
     */
    fun onDateSelected(date: Date) {
        _uiState.update { it.copy(selectedDate = date) }
        // Emit the new date to our flow, which triggers the data loading
        selectedDateFlow.value = date
    }

    /**
     * Generates a list of dates for the horizontal calendar view.
     */
    private fun generateCalendarDates() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7) // Start from 7 days ago

        val dates = List(14) {
            val date = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            date
        }
        _uiState.update { it.copy(calendarDates = dates) }
    }

    /**
     * Formats a Date object into a full string like "11, December 2022".
     */
    fun formatFullDate(date: Date): String {
        val format = SimpleDateFormat("d, MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}
