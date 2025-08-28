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
    val pageState: LoadState = LoadState.Loading, // ✅ FIX: Use structured state
    val processingRequestIds: Set<String> = emptySet(),
    val error: UiError? = null // ✅ FIX: Use structured error
)

sealed interface FriendsEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : FriendsEvent
    // ✅ NEW: A specific event to handle the "Undo" action for declined requests.
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

    fun loadFriendsAndRequests() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: run {
                _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError("User not authenticated.")) }
                return@launch
            }

            combine(
                userRepository.getFriends(uid),
                userRepository.getFriendRequests(uid)
            ) { friends, requests ->
                FriendsUiState(
                    pageState = LoadState.Idle,
                    friends = friends,
                    friendRequests = requests
                )
            }
                .onStart { _uiState.update { it.copy(pageState = LoadState.Loading) } }
                .catch { e ->
                    _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError(e.message ?: "Failed to load data.")) }
                }
                .collect { combinedState ->
                    _uiState.value = combinedState
                }
        }
    }

    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds + request.id) }
            val result = userRepository.acceptFriendRequest(request)
            if (result.isSuccess) {
                // ✅ FIX: Emit a more engaging success message.
                _eventFlow.emit(FriendsEvent.ShowSnackbar("You and ${request.senderUsername} are now friends!"))
            } else {
                _eventFlow.emit(FriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to accept request."))
            }
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds - request.id) }
        }
    }

    fun declineFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds + request.id) }
            val result = userRepository.declineFriendRequest(request)
            if (result.isSuccess) {
                // ✅ FIX: Provide an "Undo" action in the snackbar.
                _eventFlow.emit(FriendsEvent.ShowSnackbar("Request declined.", "Undo"))
            } else {
                _eventFlow.emit(FriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to decline request."))
            }
            _uiState.update { it.copy(processingRequestIds = it.processingRequestIds - request.id) }
        }
    }

    // ✅ NEW: A function to handle the "Undo" action.
    fun undoDecline(request: FriendRequest) {
        viewModelScope.launch {
            // This would typically involve another repository call to revert the decline.
            // For now, we'll just re-send the request as a simple implementation.
            val sender = User(uid = request.senderId, username = request.senderUsername)
            userRepository.sendFriendRequest(sender, request.receiverId)
        }
    }
}
