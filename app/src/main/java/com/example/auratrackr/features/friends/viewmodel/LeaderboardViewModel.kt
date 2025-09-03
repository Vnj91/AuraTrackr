package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.core.ui.UiError
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val rankedUsers: List<User> = emptyList(),
    val pageState: LoadState = LoadState.Loading
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard(isInitialLoad = true)
    }

    fun loadLeaderboard(isInitialLoad: Boolean = false) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(pageState = LoadState.Error(UiError("User not authenticated."))) }
                return@launch
            }

            val loadingState = if (isInitialLoad) LoadState.Loading else LoadState.Refreshing
            _uiState.update { it.copy(pageState = loadingState) }

            combine(
                userRepository.getUserProfile(uid),
                userRepository.getFriends(uid)
            ) { currentUser, friends ->
                val allUsers = (friends + currentUser).filterNotNull().distinctBy { it.uid }
                val rankedList = allUsers.sortedWith(
                    compareByDescending<User> { it.auraPoints }
                        .thenBy { it.username }
                )
                rankedList
            }
                .catch { e ->
                    _uiState.update { it.copy(pageState = LoadState.Error(UiError(e.message ?: "An unknown error occurred."))) }
                }
                .collect { rankedList ->
                    _uiState.update {
                        it.copy(
                            pageState = LoadState.Success,
                            rankedUsers = rankedList
                        )
                    }
                }
        }
    }
}

