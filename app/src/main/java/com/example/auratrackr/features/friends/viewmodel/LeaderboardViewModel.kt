package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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

    /**
     * Fetches the current user and their friends, combines them, and creates a ranked leaderboard.
     * The data is observed in real-time and the UI state is updated accordingly.
     */
    fun loadLeaderboard() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated.") }
                return@launch
            }

            // Combine the flow for the current user's profile with the flow for their friends' profiles.
            combine(
                userRepository.getUserProfile(uid),
                userRepository.getFriends(uid)
            ) { currentUser, friends ->
                // Create a single list containing the current user and all their friends.
                val allUsers = (friends + currentUser).filterNotNull().distinctBy { it.uid }

                // Sort the list by Aura Points. A secondary sort by username is added to ensure a
                // stable order for users with the same score, preventing UI flicker.
                val rankedList = allUsers.sortedWith(
                    compareByDescending<User> { it.auraPoints }
                        .thenBy { it.username }
                )

                LeaderboardUiState(
                    isLoading = false,
                    rankedUsers = rankedList
                )
            }
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    // If an error occurs in either of the upstream flows, update the UI state.
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { combinedState ->
                    _uiState.value = combinedState
                }
        }
    }
}