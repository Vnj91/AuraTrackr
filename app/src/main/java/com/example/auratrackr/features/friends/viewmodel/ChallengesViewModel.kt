package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.Challenge
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.ChallengeRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ChallengesUiState(
    val challenges: List<Challenge> = emptyList(),
    val friends: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val challengeRepository: ChallengeRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    init {
        loadChallengesAndFriends()
    }

    private fun loadChallengesAndFriends() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = ChallengesUiState(isLoading = true)

            // Combine the flows for challenges and friends into a single state update
            combine(
                challengeRepository.getUserChallenges(uid),
                userRepository.getFriends(uid)
            ) { challenges, friends ->
                ChallengesUiState(
                    isLoading = false,
                    challenges = challenges,
                    friends = friends
                )
            }.collect { combinedState ->
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
        metric: String,
        endDate: Date,
        participantIds: List<String>
    ) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            // The creator is always a participant
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
            challengeRepository.createChallenge(newChallenge)
            // The UI will update automatically due to the real-time flow.
        }
    }
}
