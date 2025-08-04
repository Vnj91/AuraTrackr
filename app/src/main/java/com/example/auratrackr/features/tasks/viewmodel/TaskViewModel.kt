package com.example.auratrackr.features.tasks.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

// A sealed class to represent the result of the task attempt
sealed class TaskResult {
    object Success : TaskResult()
    object Failure : TaskResult()
}

@HiltViewModel
class TaskViewModel @Inject constructor() : ViewModel() {

    // A simple state to hold the current question
    val question: String
    private val correctAnswer: Int

    private val _taskResultEvent = MutableSharedFlow<TaskResult>()
    val taskResultEvent = _taskResultEvent.asSharedFlow()

    init {
        // Generate a simple multiplication problem when the ViewModel is created
        val num1 = Random.nextInt(10, 20) // Random number between 10 and 19
        val num2 = Random.nextInt(5, 10)  // Random number between 5 and 9
        correctAnswer = num1 * num2
        question = "$num1 x $num2 = ?"
    }

    /**
     * Checks if the user's submitted answer is correct.
     * Emits a TaskResult event to the UI.
     */
    fun onAnswerSubmitted(userAnswer: String) {
        viewModelScope.launch {
            val answerInt = userAnswer.toIntOrNull()
            if (answerInt == correctAnswer) {
                _taskResultEvent.emit(TaskResult.Success)
            } else {
                _taskResultEvent.emit(TaskResult.Failure)
            }
        }
    }
}
