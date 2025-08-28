package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Challenge
import com.example.auratrackr.domain.model.ChallengeMetric
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.ChallengeRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// ✅ FIX: Replaced boolean flags with a descriptive sealed interface for loading states.
sealed interface LoadState {
    object Loading : LoadState
    object Idle : LoadState
    object Submitting : LoadState
}

// ✅ FIX: Replaced the error string with a structured error object.
data class UiError(
    val message: String,
    val isCritical: Boolean = false
)

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val friends: List<User> = emptyList(),
    val pageState: LoadState = LoadState.Loading,
    val error: UiError? = null
)

sealed interface ChallengeEvent {
    data class ShowSnackbar(val message: String) : ChallengeEvent
    object CreateSuccess : ChallengeEvent
}

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ChallengeEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadChallengesAndFriends()
    }

    fun loadChallengesAndFriends() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError("User not authenticated.", true)) }
                return@launch
            }

            combine(
                challengeRepository.getUserChallenges(uid),
                userRepository.getFriends(uid)
            ) { challenges, friends ->
                ChallengesUiState(
                    pageState = LoadState.Idle,
                    challenges = challenges,
                    friends = friends
                )
            }
                .onStart { _uiState.update { it.copy(pageState = LoadState.Loading) } }
                .catch { e ->
                    _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError(e.message ?: "An unknown error occurred.")) }
                }
                .collect { combinedState ->
                    _uiState.value = combinedState
                }
        }
    }

    fun createChallenge(
        title: String,
        description: String,
        goal: Long,
        metric: ChallengeMetric,
        endDate: Date,
        participantIds: List<String>
    ) {
        viewModelScope.launch {
            // ✅ FIX: Added inline validation for the form fields.
            if (title.isBlank()) {
                _eventFlow.emit(ChallengeEvent.ShowSnackbar("Challenge title cannot be empty."))
                return@launch
            }
            if (goal <= 0) {
                _eventFlow.emit(ChallengeEvent.ShowSnackbar("Goal must be greater than zero."))
                return@launch
            }

            _uiState.update { it.copy(pageState = LoadState.Submitting) }
            val uid = auth.currentUser?.uid ?: return@launch

            val allParticipants = (participantIds + uid).distinct()

            val newChallenge = Challenge(
                title = title,
                description = description,
                creatorId = uid,
                participants = allParticipants,
                goal = goal,
                metric = metric,
                endDate = endDate
            )

            val result = challengeRepository.createChallenge(newChallenge)

            if (result.isSuccess) {
                _eventFlow.emit(ChallengeEvent.CreateSuccess)
            } else {
                _eventFlow.emit(ChallengeEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to create challenge."))
            }
            _uiState.update { it.copy(pageState = LoadState.Idle) }
        }
    }
}
