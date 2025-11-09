package com.example.auratrackr.features.friends.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.core.ui.UiError
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FindFriendsUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val pageState: LoadState = LoadState.Idle,
    val isSendingRequestTo: Set<String> = emptySet(),
    val requestSentTo: Set<String> = emptySet()
)

sealed interface FindFriendsEvent {
    data class ShowSnackbar(val message: String) : FindFriendsEvent
    data class RequestFailed(val error: UiError, val receiverId: String) : FindFriendsEvent
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
                .collectLatest { query ->
                    if (query.length < 2) {
                        _uiState.update { it.copy(pageState = LoadState.Idle, searchResults = emptyList()) }
                    } else {
                        searchUsers(query)
                    }
                }
        }
    }

    private suspend fun searchUsers(query: String) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.update { it.copy(pageState = LoadState.Loading) }
        val result = userRepository.searchUsersByUsername(query, uid)
        if (result.isSuccess) {
            _uiState.update {
                it.copy(
                    pageState = LoadState.Success,
                    searchResults = result.getOrNull() ?: emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(pageState = LoadState.Error(UiError(parseAuthException(result.exceptionOrNull()))))
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
                _uiState.update { it.copy(requestSentTo = it.requestSentTo + receiverId) }
            } else {
                val error = UiError(parseAuthException(result.exceptionOrNull()))
                _eventFlow.emit(FindFriendsEvent.RequestFailed(error, receiverId))
            }
            _uiState.update { it.copy(isSendingRequestTo = it.isSendingRequestTo - receiverId) }
        }
    }

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
