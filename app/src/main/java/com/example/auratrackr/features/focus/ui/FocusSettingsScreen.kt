package com.example.auratrackr.features.focus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            initialTimeBudget = (appToShowDialog.budget?.timeBudgetInMinutes ?: 60L).toString(),
            onDismiss = { showDialogForApp = null },
            onSave = { timeBudget ->
                viewModel.addAppToMonitor(appToShowDialog.app, timeBudget)
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
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
                // ✅ FIX: Use a proper placeholder for app icons.
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
        Text(
            text = monitoredApp.app.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
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
    initialTimeBudget: String,
    onDismiss: () -> Unit,
    onSave: (timeBudget: Long) -> Unit
) {
    var timeBudget by remember { mutableStateOf(initialTimeBudget) }
    val isSaveEnabled by remember(timeBudget) {
        derivedStateOf { timeBudget.toLongOrNull() != null && timeBudget.toLong() > 0 }
    }

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
                    "Set Budget for $appName",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = timeBudget,
                    onValueChange = { timeBudget = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Time Budget (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = timeBudget.toLongOrNull() == null
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
                        onClick = { onSave(timeBudget.toLong()) },
                        enabled = isSaveEnabled
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FocusSettingsScreenPreview() {
    AuraTrackrTheme {
        FocusSettingsScreen(onBackClicked = {})
    }
}
