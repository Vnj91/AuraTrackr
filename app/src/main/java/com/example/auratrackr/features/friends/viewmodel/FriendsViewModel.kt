package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.FriendRequest
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

data class FriendsUiState(
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadFriendsAndRequests()
    }

    private fun loadFriendsAndRequests() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            _uiState.value = FriendsUiState(isLoading = true)

            // Combine the flows for friends and requests into a single state update
            combine(
                userRepository.getFriends(uid),
                userRepository.getFriendRequests(uid)
            ) { friends, requests ->
                FriendsUiState(
                    isLoading = false,
                    friends = friends,
                    friendRequests = requests
                )
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
    }

    /**
     * Accepts a friend request.
     */
    fun acceptFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            userRepository.acceptFriendRequest(request)
            // The UI will update automatically due to the real-time flow.
        }
    }

    /**
     * Declines a friend request.
     */
    fun declineFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            userRepository.declineFriendRequest(request)
            // The UI will update automatically.
        }
    }
}
