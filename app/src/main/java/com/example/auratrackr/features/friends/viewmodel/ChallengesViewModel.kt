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

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val friends: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val error: String? = null
)

sealed interface ChallengeEvent {
    data class ShowSnackbar(val message: String) : ChallengeEvent
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

    private fun loadChallengesAndFriends() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated.") }
                return@launch
            }

            // Combine the flows for challenges and friends into a single state update.
            combine(
                challengeRepository.getUserChallenges(uid),
                userRepository.getFriends(uid)
            ) { challenges, friends ->
                ChallengesUiState(
                    isLoading = false,
                    challenges = challenges,
                    friends = friends
                )
            }
                .onStart { _uiState.update { it.copy(isLoading = true) } }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { combinedState ->
                    _uiState.value = combinedState
                }
        }
    }

    /**
     * Creates a new group challenge.
     */
    fun createChallenge(
        title: String,
        description: String,
        goal: Long,
        metric: ChallengeMetric,
        endDate: Date,
        participantIds: List<String>
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val uid = auth.currentUser?.uid ?: return@launch

            // The creator is always a participant.
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
                _eventFlow.emit(ChallengeEvent.ShowSnackbar("Challenge created successfully!"))
            } else {
                _eventFlow.emit(ChallengeEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to create challenge."))
            }
            _uiState.update { it.copy(isCreating = false) }
        }
    }
}