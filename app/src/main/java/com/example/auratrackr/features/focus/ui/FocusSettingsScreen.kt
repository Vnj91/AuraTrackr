package com.example.auratrackr.features.focus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsViewModel
import com.example.auratrackr.features.focus.viewmodel.MonitoredApp
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.core.Party

@Composable
fun FocusSettingsScreen(
    onBackClicked: () -> Unit,
    viewModel: FocusSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialogForApp by remember { mutableStateOf<MonitoredApp?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var konfettiParty by remember { mutableStateOf<Party?>(null) }

    // clear konfetti after a short time
    LaunchedEffect(konfettiParty) {
        if (konfettiParty != null) {
            delay(2000)
            konfettiParty = null
        }
    }

    // Delegate event handling to the extracted handler to keep this function short.
    HandleFocusSettingsEvents(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState
    )

    // Delegate complex UI to the parts file. ShowBudgetAndKonfetti will render dialogs and konfetti.
    ShowBudgetAndKonfetti(
        dialogApp = showDialogForApp,
        setDialogApp = { showDialogForApp = it },
        viewModel = viewModel,
        setKonfettiParty = { konfettiParty = it }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Focus Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            FocusSettingsContent(
                uiState = uiState,
                onAppSelected = { monitoredApp, enabled ->
                    if (enabled) {
                        showDialogForApp = monitoredApp
                    } else {
                        viewModel.handleEvent(FocusSettingsEvent.ToggleAppMonitoring(monitoredApp.app, false))
                    }
                },
                onRowClicked = { showDialogForApp = it },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
