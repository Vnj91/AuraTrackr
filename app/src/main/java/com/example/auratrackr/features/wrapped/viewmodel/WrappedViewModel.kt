package com.example.auratrackr.features.wrapped.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.domain.repository.AuthRepository
import com.example.auratrackr.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

data class WrappedUiState(
    val summary: UserSummary? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WrappedViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WrappedUiState())
    val uiState: StateFlow<WrappedUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "WrappedViewModel"
    }

    init {
        loadUserSummary()
    }

    @Suppress("TooGenericExceptionCaught") // final fallback logs unexpected failures when loading the wrapped summary
    fun loadUserSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val uid = authRepository.currentUserId()
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated.") }
                return@launch
            }

            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

            try {
                val summary = userRepository.getUserSummary(uid, currentYear).firstOrNull()

                if (summary != null) {
                    _uiState.update { it.copy(summary = summary, isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Your Aura Wrapped summary is not ready yet. Check back later!"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to fetch user summary")
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to fetch data: ${e.localizedMessage}")
                }
            }
        }
    }
}
