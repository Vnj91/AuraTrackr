package com.example.auratrackr.features.challenges.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.ChallengeMetric
import com.example.auratrackr.features.challenges.viewmodel.ChallengesViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    onBackClicked: () -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Keep MutableState holders grouped in a small holder so the top-level screen stays short.
    val holders = CreateChallengeStateHolders(
        titleState = remember { mutableStateOf("") },
        descriptionState = remember { mutableStateOf("") },
        goalState = remember { mutableStateOf("") },
        selectedMetricState = remember { mutableStateOf(ChallengeMetric.STEPS) },
        selectedEndDateState = remember { mutableStateOf<LocalDate?>(null) },
        selectedFriendIdsState = remember { mutableStateOf<Set<String>>(emptySet()) },
        showDatePickerState = remember { mutableStateOf(false) }
    )

    // form validation is evaluated at the action site; no long-lived derived state needed here

    // Delegate the full scaffold and content rendering into the parts file so the top-level
    // screen function stays minimal and detekt LongMethod warnings are reduced.
    CreateChallengeScreenScaffold(
        uiState = uiState,
        holders = holders,
        viewModel = viewModel,
        onBackClicked = onBackClicked
    )
}
