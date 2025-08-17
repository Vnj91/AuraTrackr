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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- State and Event Definitions ---

sealed interface BlockerUiState {
    object Loading : BlockerUiState
    data class Ready(
        val currentUserPoints: Int = 0,
        val canAffordExtraTime: Boolean = false,
        val waitTimerSecondsRemaining: Int? = null
    ) : BlockerUiState
    data class Error(val message: String) : BlockerUiState
}

sealed interface BlockerEvent {
    data class SpendResult(val result: Result<Unit>) : BlockerEvent
    object UnblockGracePeriod : BlockerEvent
}

@HiltViewModel
class BlockingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val unblockManager: TemporaryUnblockManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockerUiState>(BlockerUiState.Loading)
    val uiState: StateFlow<BlockerUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<BlockerEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null
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
            val uid = auth.currentUser?.uid ?: run {
                _uiState.value = BlockerUiState.Error("User not authenticated.")
                return@launch
            }
            userRepository.getUserProfile(uid)
                .distinctUntilChanged() // Avoids unnecessary updates if user data hasn't changed
                .collect { user ->
                    val points = user?.auraPoints ?: 0
                    _uiState.value = BlockerUiState.Ready(
                        currentUserPoints = points,
                        canAffordExtraTime = points >= EXTRA_TIME_COST
                    )
                }
        }
    }

    fun spendPointsForExtraTime() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is BlockerUiState.Ready || !currentState.canAffordExtraTime) return@launch

            val uid = auth.currentUser?.uid ?: return@launch
            val result = userRepository.spendAuraPoints(uid, EXTRA_TIME_COST)

            if (result.isSuccess) {
                unblockManager.grantTemporaryUnblock(blockedPackageName)
                _eventFlow.emit(BlockerEvent.UnblockGracePeriod)
            }
            _eventFlow.emit(BlockerEvent.SpendResult(result))
        }
    }

    fun startWaitTimer() {
        if (timerJob?.isActive == true) return

        val currentState = _uiState.value
        if (currentState !is BlockerUiState.Ready) return

        timerJob = viewModelScope.launch {
            for (i in WAIT_TIMER_DURATION_SECONDS downTo 1) {
                _uiState.update {
                    if (it is BlockerUiState.Ready) it.copy(waitTimerSecondsRemaining = i) else it
                }
                delay(1000L)
            }
            unblockManager.grantTemporaryUnblock(blockedPackageName)
            _uiState.update {
                if (it is BlockerUiState.Ready) it.copy(waitTimerSecondsRemaining = 0) else it
            }
            _eventFlow.emit(BlockerEvent.UnblockGracePeriod)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}