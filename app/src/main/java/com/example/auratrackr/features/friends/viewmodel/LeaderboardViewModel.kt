package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val rankedUsers: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = LeaderboardUiState(isLoading = true)

            // Combine the flow for the current user's profile with the flow for their friends' profiles.
            combine(
                userRepository.getUserProfile(uid),
                userRepository.getFriends(uid)
            ) { currentUser, friends ->
                // Create a single list containing the current user and all their friends.
                val allUsers = (friends + currentUser).filterNotNull().distinctBy { it.uid }

                // Sort the list in descending order based on Aura Points to create the ranking.
                val rankedList = allUsers.sortedByDescending { it.auraPoints }

                LeaderboardUiState(
                    isLoading = false,
                    rankedUsers = rankedList
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }
}
