package com.example.auratrackr.features.tasks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.features.focus.tracking.TemporaryUnblockManager
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
class TaskViewModel @Inject constructor(
    private val unblockManager: TemporaryUnblockManager, // <-- INJECT THE MANAGER
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val question: String
    private val correctAnswer: Int

    private val _taskResultEvent = MutableSharedFlow<TaskResult>()
    val taskResultEvent = _taskResultEvent.asSharedFlow()

    // Get the blocked app's package name that will be passed via navigation
    private val blockedPackageName: String = savedStateHandle.get<String>("packageName") ?: ""

    init {
        val num1 = Random.nextInt(10, 20)
        val num2 = Random.nextInt(5, 10)
        correctAnswer = num1 * num2
        question = "$num1 x $num2 = ?"
    }

    /**
     * Checks the user's answer and grants a grace period on success.
     */
    fun onAnswerSubmitted(userAnswer: String) {
        viewModelScope.launch {
            val answerInt = userAnswer.toIntOrNull()
            if (answerInt == correctAnswer) {
                // On success, grant the grace period for the specific app
                if (blockedPackageName.isNotEmpty()) {
                    unblockManager.grantTemporaryUnblock(blockedPackageName)
                }
                _taskResultEvent.emit(TaskResult.Success)
            } else {
                _taskResultEvent.emit(TaskResult.Failure)
            }
        }
    }
}
