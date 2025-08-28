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

// ✅ FIX: Replaced boolean flags and error strings with structured state objects.
data class LeaderboardUiState(
    val rankedUsers: List<User> = emptyList(),
    val pageState: LoadState = LoadState.Loading,
    val error: UiError? = null
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
     * This function is public to allow for manual refreshes from the UI.
     */
    fun loadLeaderboard() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError("User not authenticated.")) }
                return@launch
            }

            // Combine the flows for the user's profile and their friends list.
            // Note: For apps with potentially very large friend lists, this approach should be
            // replaced with a paginated query to avoid performance issues.
            combine(
                userRepository.getUserProfile(uid),
                userRepository.getFriends(uid)
            ) { currentUser, friends ->
                val allUsers = (friends + currentUser).filterNotNull().distinctBy { it.uid }

                // Sort by points (descending) and then by username (ascending) for a stable ranking.
                val rankedList = allUsers.sortedWith(
                    compareByDescending<User> { it.auraPoints }
                        .thenBy { it.username }
                )

                LeaderboardUiState(
                    pageState = LoadState.Idle,
                    rankedUsers = rankedList
                )
            }
                .onStart { _uiState.update { it.copy(pageState = LoadState.Loading, error = null) } }
                .catch { e ->
                    _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError(e.message ?: "An unknown error occurred.")) }
                }
                .collect { combinedState ->
                    _uiState.value = combinedState
                }
        }
    }
}
