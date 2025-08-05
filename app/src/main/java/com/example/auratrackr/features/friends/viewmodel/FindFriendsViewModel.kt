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

data class FindFriendsUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val requestSentTo: Set<String> = emptySet() // Set of user IDs a request has been sent to
)

@HiltViewModel
class FindFriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindFriendsUiState())
    val uiState: StateFlow<FindFriendsUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        // Fetch the current user's profile so we can send requests from them
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            userRepository.getUserProfile(uid).firstOrNull()?.let {
                currentUser = it
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Searches for users by their username.
     */
    fun searchUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = userRepository.searchUsersByUsername(_uiState.value.searchQuery)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    // Exclude the current user from the search results
                    searchResults = result.getOrNull()?.filterNot { it.uid == currentUser?.uid } ?: emptyList()
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    /**
     * Sends a friend request to a specific user.
     */
    fun sendFriendRequest(receiverId: String) {
        viewModelScope.launch {
            val sender = currentUser ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = userRepository.sendFriendRequest(sender, receiverId)
            if (result.isSuccess) {
                // Add the user to a temporary set to show "Request Sent" in the UI
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    requestSentTo = _uiState.value.requestSentTo + receiverId
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }
}
