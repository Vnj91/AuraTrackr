package com.example.auratrackr.features.tasks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.features.focus.tracking.TemporaryUnblockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// A sealed class to represent the result of the task attempt
sealed interface TaskResult {
    object Success : TaskResult
    object Failure : TaskResult
}

data class TaskUiState(
    val question: String = "",
    val showError: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val unblockManager: TemporaryUnblockManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val _taskResultEvent = MutableSharedFlow<TaskResult>()
    val taskResultEvent = _taskResultEvent.asSharedFlow()

    private val blockedPackageName: String = savedStateHandle.get<String>("packageName") ?: ""
    private var correctAnswer: Int = 0

    init {
        generateNewTask()
    }

    /**
     * Generates a new simple multiplication task and updates the UI state.
     */
    private fun generateNewTask() {
        val num1 = Random.nextInt(10, 20)
        val num2 = Random.nextInt(5, 10)
        correctAnswer = num1 * num2
        _uiState.value = TaskUiState(question = "$num1 x $num2 = ?")
    }

    /**
     * Processes the user's submitted answer, validates it, and emits the result.
     * @param userAnswer The answer provided by the user from the text input.
     */
    fun onAnswerSubmitted(userAnswer: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showError = false) }

            val answerInt = userAnswer.toIntOrNull()

            if (answerInt == correctAnswer) {
                // On success, grant the temporary unblock and emit a success event.
                if (blockedPackageName.isNotEmpty()) {
                    unblockManager.grantTemporaryUnblock(blockedPackageName)
                }
                _taskResultEvent.emit(TaskResult.Success)
            } else {
                // On failure (either wrong answer or invalid input), show an error.
                _uiState.update { it.copy(showError = true) }
                _taskResultEvent.emit(TaskResult.Failure)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Resets the error state. This should be called from the UI whenever the
     * user modifies their answer in the text field.
     */
    fun onAnswerInputChanged() {
        _uiState.update { it.copy(showError = false) }
    }
}