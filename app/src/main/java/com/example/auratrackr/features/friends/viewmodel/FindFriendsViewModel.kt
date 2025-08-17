package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FindFriendsUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val isSendingRequestTo: Set<String> = emptySet(), // Tracks which users are currently having a request sent to them
    val requestSentTo: Set<String> = emptySet(), // Tracks users a request has been successfully sent to
    val error: String? = null
)

sealed interface FindFriendsEvent {
    data class ShowSnackbar(val message: String) : FindFriendsEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FindFriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindFriendsUiState())
    val uiState: StateFlow<FindFriendsUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<FindFriendsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentUser: User? = null

    // A flow to handle the search query with a debounce to avoid excessive API calls.
    private val searchQueryFlow = MutableStateFlow("")

    init {
        // Fetch the current user's profile once on init to be used for sending requests.
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            currentUser = userRepository.getUserProfile(uid).firstOrNull()
        }

        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L) // Wait for 300ms of no new input before searching
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.length < 2) {
                        // Clear results if the query is too short
                        flowOf(Result.success(emptyList()))
                    } else {
                        flow {
                            val uid = auth.currentUser?.uid ?: return@flow
                            emit(userRepository.searchUsersByUsername(query, uid))
                        }
                    }
                }
                .onStart { _uiState.update { it.copy(isSearching = true, error = null) } }
                .catch { e ->
                    _uiState.update { it.copy(isSearching = false, error = e.message) }
                }
                .collect { result ->
                    if (result.isSuccess) {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                searchResults = result.getOrNull() ?: emptyList()
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isSearching = false, error = result.exceptionOrNull()?.message)
                        }
                    }
                }
        }
    }

    /**
     * Updates the search query text and triggers the search flow.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    /**
     * Sends a friend request to a specific user.
     */
    fun sendFriendRequest(receiverId: String) {
        viewModelScope.launch {
            val sender = currentUser ?: run {
                _eventFlow.emit(FindFriendsEvent.ShowSnackbar("Could not identify current user."))
                return@launch
            }

            // Update UI to show loading state for this specific user
            _uiState.update { it.copy(isSendingRequestTo = it.isSendingRequestTo + receiverId) }

            val result = userRepository.sendFriendRequest(sender, receiverId)

            if (result.isSuccess) {
                _eventFlow.emit(FindFriendsEvent.ShowSnackbar("Friend request sent!"))
                _uiState.update {
                    it.copy(requestSentTo = it.requestSentTo + receiverId)
                }
            } else {
                _eventFlow.emit(FindFriendsEvent.ShowSnackbar(result.exceptionOrNull()?.message ?: "Failed to send request."))
            }

            // Remove loading state for this user regardless of outcome
            _uiState.update { it.copy(isSendingRequestTo = it.isSendingRequestTo - receiverId) }
        }
    }
}