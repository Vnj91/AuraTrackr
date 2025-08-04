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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ScheduleUiState(
    val selectedDate: Date = Date(),
    val calendarDates: List<Date> = emptyList(),
    val selectedSchedule: Schedule? = null,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val vibeRepository: VibeRepository, // <-- INJECT THE VIBE REPOSITORY
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val selectedDateFlow = MutableStateFlow(Date())

    init {
        generateCalendarDates()
        observeScheduleChanges()
    }

    private fun observeScheduleChanges() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            // This is now a combined reactive stream. It listens for changes to BOTH
            // the selected date AND the selected vibe.
            combine(
                selectedDateFlow,
                vibeRepository.getSelectedVibe()
            ) { date, vibe ->
                // Create a pair to pass to the next step
                date to vibe
            }.flatMapLatest { (date, vibe) ->
                _uiState.update { it.copy(isLoading = true) }
                // Fetch schedules that match both the date and the vibe ID
                workoutRepository.getSchedulesForDateAndVibe(uid, date, vibe.id)
            }.collect { schedules ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        // We'll take the first schedule that matches
                        selectedSchedule = schedules.firstOrNull()
                    )
                }
            }
        }
    }

    fun onDateSelected(date: Date) {
        _uiState.update { it.copy(selectedDate = date) }
        selectedDateFlow.value = date
    }

    private fun generateCalendarDates() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)

        val dates = List(14) {
            val date = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            date
        }
        _uiState.update { it.copy(calendarDates = dates) }
    }

    fun formatFullDate(date: Date): String {
        val format = SimpleDateFormat("d, MMMM yyyy", Locale.getDefault())
        return format.format(date)
    }
}
