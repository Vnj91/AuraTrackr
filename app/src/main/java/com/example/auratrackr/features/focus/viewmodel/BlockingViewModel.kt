package com.example.auratrackr.features.focus.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.features.focus.tracking.TemporaryUnblockManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlockerUiState(
    val currentUserPoints: Int = 0,
    val canAffordExtraTime: Boolean = false,
    val spendResult: Result<Unit>? = null,
    val waitTimerSecondsRemaining: Int? = null,
    val unblockEvent: Boolean = false
)

@HiltViewModel
class BlockingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val unblockManager: TemporaryUnblockManager, // <-- INJECT THE MANAGER
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BlockerUiState())
    val uiState: StateFlow<BlockerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    // Get the blocked app's package name passed from the Activity's intent extras
    private val blockedPackageName: String = savedStateHandle.get<String>("packageName") ?: ""

    companion object {
        const val EXTRA_TIME_COST = 25
        const val WAIT_TIMER_DURATION_SECONDS = 60
    }

    init {
        fetchUserPoints()
    }

    private fun fetchUserPoints() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            userRepository.getUserProfile(uid).collect { user ->
                val points = user?.auraPoints ?: 0
                _uiState.value = _uiState.value.copy(
                    currentUserPoints = points,
                    canAffordExtraTime = points >= EXTRA_TIME_COST
                )
            }
        }
    }

    fun spendPointsForExtraTime() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            if (_uiState.value.canAffordExtraTime) {
                val result = userRepository.spendAuraPoints(uid, EXTRA_TIME_COST)
                if (result.isSuccess) {
                    // Grant the grace period for the specific app
                    unblockManager.grantTemporaryUnblock(blockedPackageName)
                    _uiState.value = _uiState.value.copy(unblockEvent = true)
                }
                _uiState.value = _uiState.value.copy(spendResult = result)
            }
        }
    }

    fun startWaitTimer() {
        if (timerJob?.isActive == true) return

        timerJob = viewModelScope.launch {
            for (i in WAIT_TIMER_DURATION_SECONDS downTo 1) {
                _uiState.value = _uiState.value.copy(waitTimerSecondsRemaining = i)
                delay(1000L)
            }
            // Grant the grace period for the specific app
            unblockManager.grantTemporaryUnblock(blockedPackageName)
            _uiState.value = _uiState.value.copy(
                waitTimerSecondsRemaining = 0,
                unblockEvent = true
            )
        }
    }

    fun clearSpendResult() {
        _uiState.value = _uiState.value.copy(spendResult = null)
    }

    fun consumeUnblockEvent() {
        _uiState.value = _uiState.value.copy(unblockEvent = false)
    }
}
