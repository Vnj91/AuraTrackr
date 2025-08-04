package com.example.auratrackr.features.focus.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.InstalledApp
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsViewModel
import com.example.auratrackr.features.focus.viewmodel.MonitoredApp
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSettingsScreen(
    onBackClicked: () -> Unit,
    viewModel: FocusSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialogForApp by remember { mutableStateOf<InstalledApp?>(null) }

    if (showDialogForApp != null) {
        BudgetSettingDialog(
            appName = showDialogForApp!!.name,
            onDismiss = { showDialogForApp = null },
            onSave = { timeBudget, launchBudget ->
                viewModel.addAppToMonitor(showDialogForApp!!.packageName, timeBudget, launchBudget)
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
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            Text("Choose apps to limit", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Select the apps you want to monitor and set usage limits for.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    items(uiState.monitoredApps, key = { it.app.packageName }) { monitoredApp ->
                        AppListItem(
                            monitoredApp = monitoredApp,
                            onAppSelected = { isSelected ->
                                if (isSelected) {
                                    showDialogForApp = monitoredApp.app
                                } else {
                                    viewModel.removeAppToMonitor(monitoredApp.app.packageName)
                                }
                            }
                        )
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
        Image(
            painter = rememberDrawablePainter(drawable = monitoredApp.app.icon),
            contentDescription = "${monitoredApp.app.name} icon",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = monitoredApp.app.name,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
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
    onDismiss: () -> Unit,
    onSave: (timeBudget: Long, launchBudget: Int) -> Unit
) {
    var timeBudget by remember { mutableStateOf("60") } // Default 60 minutes
    var launchBudget by remember { mutableStateOf("20") } // Default 20 launches

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Set Budget for $appName", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = timeBudget,
                    onValueChange = { timeBudget = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Time Budget (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = launchBudget,
                    onValueChange = { launchBudget = it.filter { char -> char.isDigit() } },
                    label = { Text("Daily Launch Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                    Button(onClick = {
                        val time = timeBudget.toLongOrNull() ?: 60L
                        val launches = launchBudget.toIntOrNull() ?: 20
                        onSave(time, launches)
                    }) {
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
    FocusSettingsScreen(onBackClicked = {})
}
