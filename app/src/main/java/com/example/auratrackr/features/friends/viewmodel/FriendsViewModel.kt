package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = true,
    val processingRequestIds: Set<String> = emptySet(), // Tracks IDs of requests being accepted/declined
    val error: String? = null
)

sealed interface FriendsEvent {
    data class ShowSnackbar(val message: String) : FriendsEvent
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<FriendsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadFriendsAndRequests()
    }

    private fun loadFriendsAndRequests() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated.") }
                return@launch
            }

            // Combine the flows for friends and requests into a single state update.
            combine(
                userRepository.getFriends(uid),
                userRepository.getFriendRequests(uid)
            ) { friends, requests ->
                FriendsUiState(
                    isLoading = false,
                    friends = friends,
                    friendRequests = requests
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
     * Accepts a friend request. Updates the UI to show a loading state for the specific
     * request and emits a snackbar event upon completion.
     */
    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds + request.id) }
            val result = userRepository.acceptFriendRequest(request)
            if (result.isFailure) {
                _eventFlow.emit(FriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to accept request."))
            }
            // The UI will update automatically from the real-time flow.
            // We just need to remove the loading indicator for this item.
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds - request.id) }
        }
    }

    /**
     * Declines a friend request. Updates the UI to show a loading state for the specific
     * request and emits a snackbar event upon completion.
     */
    fun declineFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds + request.id) }
            val result = userRepository.declineFriendRequest(request)
            if (result.isFailure) {
                _eventFlow.emit(FriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to decline request."))
            }
            // UI updates automatically from the flow.
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds - request.id) }
        }
    }
}