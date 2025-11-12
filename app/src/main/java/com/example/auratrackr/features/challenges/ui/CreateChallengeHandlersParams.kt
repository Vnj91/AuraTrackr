package com.example.auratrackr.features.challenges.ui

import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest

// Small parameter container to keep handler composable under the configured
// LongParameterList threshold for detekt. This groups related UI callbacks/state.
data class CreateChallengeHandlersParams(
    val showDatePicker: Boolean,
    val setShowDatePicker: (Boolean) -> Unit,
    val setSelectedEndDate: (java.time.LocalDate) -> Unit,
    val onBackClicked: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeHandlers(
    viewModel: com.example.auratrackr.features.challenges.viewmodel.ChallengesViewModel,
    snackbarHostState: SnackbarHostState,
    params: CreateChallengeHandlersParams
) {
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is com.example.auratrackr.features.challenges.viewmodel.ChallengeEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is com.example.auratrackr.features.challenges.viewmodel.ChallengeEvent.CreateSuccess -> {
                    params.onBackClicked()
                }
            }
        }
    }

    if (params.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = java.time.Instant.now().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= java.time.Instant.now().toEpochMilli()
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { params.setShowDatePicker(false) },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        params.setSelectedEndDate(
                            java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        )
                    }
                    params.setShowDatePicker(false)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { params.setShowDatePicker(false) }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
