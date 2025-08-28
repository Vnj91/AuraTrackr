package com.example.auratrackr.features.focus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.domain.model.InstalledApp
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsViewModel
import com.example.auratrackr.features.focus.viewmodel.MonitoredApp
import com.example.auratrackr.ui.theme.AuraTrackrTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSettingsScreen(
    onBackClicked: () -> Unit,
    viewModel: FocusSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialogForApp by remember { mutableStateOf<MonitoredApp?>(null) }

    showDialogForApp?.let { appToShowDialog ->
        BudgetSettingDialog(
            appName = appToShowDialog.app.name,
            initialTimeBudget = (appToShowDialog.budget?.timeBudgetInMinutes ?: 60L).toInt(),
            onDismiss = { showDialogForApp = null },
            onSave = { timeBudget ->
                viewModel.addAppToMonitor(appToShowDialog.app, timeBudget.toLong())
                showDialogForApp = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Focus Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                uiState.monitoredApps.isEmpty() -> {
                    EmptyAppsState(modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(
                                    "Choose apps to limit",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Select the apps you want to monitor and set daily usage limits for.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(uiState.monitoredApps, key = { it.app.packageName }) { monitoredApp ->
                            AppListItem(
                                monitoredApp = monitoredApp,
                                onAppSelected = { isSelected ->
                                    if (isSelected) {
                                        showDialogForApp = monitoredApp
                                    } else {
                                        viewModel.removeAppFromMonitoring(monitoredApp.app.packageName)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppListItem(
    monitoredApp: MonitoredApp,
    onAppSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppSelected(!monitoredApp.isMonitored) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(monitoredApp.app.packageName)
                .placeholder(R.drawable.ic_placeholder_app_icon)
                .error(R.drawable.ic_placeholder_app_icon)
                .crossfade(true)
                .build(),
            contentDescription = "${monitoredApp.app.name} icon",
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = monitoredApp.app.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (monitoredApp.isMonitored) "Limit: ${monitoredApp.budget?.timeBudgetInMinutes ?: 0} min/day" else "Tap to set budget",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = monitoredApp.isMonitored,
            onCheckedChange = { onAppSelected(it) }
        )
    }
}

@Composable
fun BudgetSettingDialog(
    appName: String,
    initialTimeBudget: Int,
    onDismiss: () -> Unit,
    onSave: (timeBudget: Int) -> Unit
) {
    var timeBudget by remember { mutableStateOf(initialTimeBudget) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Set a daily limit for $appName",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                DurationStepper(
                    value = timeBudget,
                    onValueChange = { timeBudget = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(timeBudget) }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun DurationStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    step: Int = 5
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedIconButton(onClick = { onValueChange((value - step).coerceAtLeast(5)) }) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease duration")
        }
        Text(
            text = "$value min",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        OutlinedIconButton(onClick = { onValueChange(value + step) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase duration")
        }
    }
}

@Composable
fun EmptyAppsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No applications found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We couldn't find any launchable apps on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}


@Preview(showBackground = true)
@Composable
fun FocusSettingsScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        FocusSettingsScreen(onBackClicked = {})
    }
}
