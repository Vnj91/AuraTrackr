package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ FIX: Replaced boolean flags and error strings with structured state objects.
data class FindFriendsUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val pageState: LoadState = LoadState.Idle,
    val isSendingRequestTo: Set<String> = emptySet(),
    val requestSentTo: Set<String> = emptySet(),
    val error: UiError? = null
)

sealed interface FindFriendsEvent {
    data class ShowSnackbar(val message: String) : FindFriendsEvent
    // ✅ FIX: Added a specific event for request failures to allow for retries.
    data class RequestFailed(val message: String, val receiverId: String) : FindFriendsEvent
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
    private val searchQueryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            currentUser = userRepository.getUserProfile(uid).firstOrNull()
        }
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        flowOf(Result.success(emptyList()))
                    } else {
                        flow {
                            val uid = auth.currentUser?.uid ?: return@flow
                            emit(userRepository.searchUsersByUsername(query, uid))
                        }
                    }
                }
                .onStart { _uiState.update { it.copy(pageState = LoadState.Loading, error = null) } }
                .catch { e ->
                    _uiState.update { it.copy(pageState = LoadState.Idle, error = UiError(parseAuthException(e))) }
                }
                .collect { result ->
                    _uiState.update {
                        it.copy(
                            pageState = LoadState.Idle,
                            searchResults = result.getOrNull() ?: emptyList(),
                            error = result.exceptionOrNull()?.let { e -> UiError(parseAuthException(e)) }
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    fun sendFriendRequest(receiverId: String) {
        viewModelScope.launch {
            val sender = currentUser ?: run {
                _eventFlow.emit(FindFriendsEvent.ShowSnackbar("Could not identify current user."))
                return@launch
            }

            _uiState.update { it.copy(isSendingRequestTo = it.isSendingRequestTo + receiverId) }

            val result = userRepository.sendFriendRequest(sender, receiverId)

            if (result.isSuccess) {
                _eventFlow.emit(FindFriendsEvent.ShowSnackbar("Friend request sent!"))
                _uiState.update {
                    it.copy(requestSentTo = it.requestSentTo + receiverId)
                }
            } else {
                val errorMessage = parseAuthException(result.exceptionOrNull())
                _eventFlow.emit(FindFriendsEvent.RequestFailed(errorMessage, receiverId))
            }

            _uiState.update { it.copy(isSendingRequestTo = it.isSendingRequestTo - receiverId) }
        }
    }

    // ✅ NEW: A user-friendly error parser.
    private fun parseAuthException(e: Throwable?): String {
        return when (e) {
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No user found with that username."
                else -> "An unexpected error occurred."
            }
            else -> e?.message ?: "An unknown error occurred."
        }
    }
}
