package com.example.auratrackr.features.wrapped.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(WrappedUiState())
    val uiState: StateFlow<WrappedUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "WrappedViewModel"
    }

    init {
        loadUserSummary()
    }

    /**
     * Fetches the user's summary for the current year.
     * This function is designed for a one-shot data load.
     */
    fun loadUserSummary() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val uid = auth.currentUser?.uid
            if (uid == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not authenticated.") }
                return@launch
            }

            // Use java.util.Calendar to maintain consistency with other legacy date logic in the project.
            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

            try {
                // Since a "Wrapped" summary is typically static for a given year, we only
                // need to fetch it once rather than subscribing to a continuous flow.
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
                Log.e(TAG, "Failed to fetch user summary", e)
                _uiState.update {
                    it.copy(isLoading = false, error = "Failed to fetch data: ${e.localizedMessage}")
                }
            }
        }
    }
}