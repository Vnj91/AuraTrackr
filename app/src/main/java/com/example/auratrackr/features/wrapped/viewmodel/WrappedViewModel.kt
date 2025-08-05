package com.example.auratrackr.features.wrapped.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auratrackr.domain.model.UserSummary
import com.example.auratrackr.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    init {
        loadUserSummary()
    }

    private fun loadUserSummary() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            // For now, we'll fetch the summary for the current year.
            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

            _uiState.value = WrappedUiState(isLoading = true)

            userRepository.getUserSummary(uid, currentYear).collect { summary ->
                if (summary != null) {
                    _uiState.value = WrappedUiState(isLoading = false, summary = summary)
                } else {
                    // This can happen if the Cloud Function hasn't run yet or there's no data.
                    _uiState.value = WrappedUiState(isLoading = false, error = "Your Aura Wrapped summary is not ready yet. Check back later!")
                }
            }
        }
    }
}
