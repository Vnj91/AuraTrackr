package com.example.auratrackr.features.challenges.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.core.ui.UiError
import com.example.auratrackr.domain.model.Challenge
import com.example.auratrackr.domain.model.ChallengeMetric
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.ChallengeRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val friends: List<User> = emptyList(),
    val pageState: LoadState = LoadState.Loading
)

sealed interface ChallengeEvent {
    data class ShowSnackbar(val message: String) : ChallengeEvent
    object CreateSuccess : ChallengeEvent
}

/**
 * Parameter object that groups all inputs required to create a challenge.
 * Bundling these parameters reduces signature length and makes future changes easier.
 */
data class CreateChallengeParams(
    val title: String,
    val description: String,
    val goal: Long,
    val metric: ChallengeMetric,
    val endDate: Date,
    val participantIds: List<String>
)

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
                _uiState.update { it.copy(pageState = LoadState.Error(UiError("User not authenticated."))) }
                return@launch
            }

            combine(
                challengeRepository.getUserChallenges(uid),
                userRepository.getFriends(uid)
            ) { challenges, friends ->
                ChallengesUiState(
                    pageState = LoadState.Success,
                    challenges = challenges,
                    friends = friends
                )
            }
                .onStart { _uiState.update { it.copy(pageState = LoadState.Loading) } }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            pageState = LoadState.Error(UiError(e.message ?: "An unknown error occurred."))
                        )
                    }
                }
                .collect { combinedState ->
                    _uiState.value = combinedState
                }
        }
    }

    fun createChallenge(params: CreateChallengeParams) {
        viewModelScope.launch {
            if (params.title.isBlank()) {
                _eventFlow.emit(ChallengeEvent.ShowSnackbar("Challenge title cannot be empty."))
                return@launch
            }
            if (params.goal <= 0) {
                _eventFlow.emit(ChallengeEvent.ShowSnackbar("Goal must be greater than zero."))
                return@launch
            }

            _uiState.update { it.copy(pageState = LoadState.Submitting) }
            val uid = auth.currentUser?.uid ?: return@launch

            val allParticipants = (params.participantIds + uid).distinct()

            val newChallenge = Challenge(
                title = params.title,
                description = params.description,
                creatorId = uid,
                participants = allParticipants,
                goal = params.goal,
                metric = params.metric,
                endDate = params.endDate
            )

            val result = challengeRepository.createChallenge(newChallenge)

            if (result.isSuccess) {
                _eventFlow.emit(ChallengeEvent.CreateSuccess)
            } else {
                _eventFlow.emit(
                    ChallengeEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to create challenge.")
                )
            }
            _uiState.update { it.copy(pageState = LoadState.Success) }
        }
    }
}
