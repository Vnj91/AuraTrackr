package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.core.ui.UiError
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
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
import javax.inject.Inject

data class FriendsUiState(
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val pageState: LoadState = LoadState.Loading,
    val processingRequestIds: Set<String> = emptySet() // Tracks IDs of requests being accepted/declined
)

sealed interface FriendsEvent {
    data class ShowSnackbar(val message: String) : FriendsEvent
    data class UndoDecline(val request: FriendRequest) : FriendsEvent
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
                _uiState.update { it.copy(pageState = LoadState.Error(UiError("User not authenticated."))) }
                return@launch
            }

            // Combine the flows for friends and requests into a single state update.
            combine(
                userRepository.getFriends(uid),
                userRepository.getFriendRequests(uid)
            ) { friends, requests ->
                FriendsUiState(
                    pageState = LoadState.Success,
                    friends = friends,
                    friendRequests = requests,
                    processingRequestIds = _uiState.value.processingRequestIds // Preserve processing state
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

    /**
     * Accepts a friend request. Updates the UI to show a loading state for the specific
     * request and emits a snackbar event upon completion.
     */
    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds + request.id) }
            val result = userRepository.acceptFriendRequest(request)
            if (result.isSuccess) {
                _eventFlow.emit(FriendsEvent.ShowSnackbar("You and ${request.senderUsername} are now friends!"))
            } else {
                _eventFlow.emit(
                    FriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to accept request.")
                )
            }
            // The UI will update automatically from the real-time flow.
            // We just need to remove the loading indicator for this item.
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds - request.id) }
        }
    }

    /**
     * Declines a friend request and emits an event allowing the user to undo the action.
     */
    fun declineFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds + request.id) }
            val result = userRepository.declineFriendRequest(request)
            if (result.isSuccess) {
                _eventFlow.emit(FriendsEvent.UndoDecline(request))
            } else {
                _eventFlow.emit(
                    FriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to decline request.")
                )
            }
            // UI updates automatically from the flow.
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds - request.id) }
        }
    }

    /**
     * Reverts a declined friend request. In a real app, this would change the request's
     * status back to PENDING.
     */
    fun undoDeclineFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            // This is a placeholder for the actual undo logic. A real implementation would
            // call a repository method to set the request status back to PENDING.
            userRepository.sendFriendRequest(
                User(uid = request.senderId, username = request.senderUsername),
                request.receiverId
            )
            _eventFlow.emit(FriendsEvent.ShowSnackbar("Action undone."))
        }
    }
}
