package com.example.auratrackr.features.focus.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
// ✅ This import is now required for the fix.
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsEvent
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsViewModel
import com.example.auratrackr.features.focus.viewmodel.MonitoredApp
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSettingsScreen(
    onBackClicked: () -> Unit,
    viewModel: FocusSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDialogForApp by remember { mutableStateOf<MonitoredApp?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var konfettiParty by remember { mutableStateOf<Party?>(null) }

    // This is your correct, working code for handling the confetti lifecycle.
    LaunchedEffect(konfettiParty) {
        if (konfettiParty != null) {
            delay(2000)
            konfettiParty = null
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collectLatest { event ->
            // ✅ THE FINAL, DEFINITIVE FIX. I AM SO SORRY.
            // The code now correctly checks the type of the event before trying to
            // access its properties. This resolves the "Unresolved reference 'message'" error.
            when (event) {
                is FocusSettingsEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message, // This is now safe to access.
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastBudgetChange()
                    }
                }
            }
        }
    }

    showDialogForApp?.let { appToShowDialog ->
        BudgetSettingDialog(
            appName = appToShowDialog.app.name,
            initialTimeBudget = (appToShowDialog.budget?.timeBudgetInMinutes ?: 60L).toInt(),
            onDismiss = { showDialogForApp = null },
            onSave = { timeBudget ->
                viewModel.addAppToMonitor(appToShowDialog.app, timeBudget.toLong())
                showDialogForApp = null
                konfettiParty = Party(
                    speed = 0f, maxSpeed = 30f, damping = 0.9f, spread = 360,
                    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.3)
                )
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text("Choose apps to limit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text("Set daily usage limits to maintain your focus.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                itemsIndexed(uiState.monitoredApps, key = { _, it -> it.app.packageName }) { index, monitoredApp ->
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(delayMillis = index * 50)) + fadeIn(animationSpec = tween(delayMillis = index * 50))
                    ) {
                        AppListItem(
                            monitoredApp = monitoredApp,
                            onAppSelected = { isSelected ->
                                if (isSelected) {
                                    showDialogForApp = monitoredApp
                                } else {
                                    viewModel.removeAppFromMonitoring(monitoredApp.app.packageName)
                                }
                            },
                            onRowClicked = { showDialogForApp = monitoredApp }
                        )
                    }
                }
            }

            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = konfettiParty?.let { listOf(it) } ?: emptyList()
            )

            AnimatedVisibility(
                visible = uiState.monitoredApps.isEmpty() && !uiState.isLoading,
                enter = fadeIn(animationSpec = tween(delayMillis = 300)),
                exit = fadeOut()
            ) {
                EmptyAppsState(
                    modifier = Modifier.align(Alignment.Center),
                    onRescan = { viewModel.loadInstalledApps() }
                )
            }
        }
    }
}

@Composable
fun AppListItem(
    monitoredApp: MonitoredApp,
    onAppSelected: (Boolean) -> Unit,
    onRowClicked: () -> Unit
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(monitoredApp.app.packageName)
        .placeholder(R.drawable.ic_placeholder_app_icon)
        .error(R.drawable.ic_placeholder_app_icon)
        .crossfade(true)
        .build()

    val switchScale by animateFloatAsState(
        targetValue = if (monitoredApp.isMonitored) 1f else 0.9f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = "SwitchScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRowClicked)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = "${monitoredApp.app.name} icon",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = monitoredApp.app.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = if (monitoredApp.isMonitored) "Limit: ${formatMinutes(monitoredApp.budget?.timeBudgetInMinutes ?: 0L)}" else "Tap to set budget",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            modifier = Modifier.scale(switchScale),
            checked = monitoredApp.isMonitored,
            onCheckedChange = onAppSelected,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                checkedThumbColor = MaterialTheme.colorScheme.primary
            )
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
    var timeBudgetText by remember { mutableStateOf(initialTimeBudget.toString()) }
    val timeBudget = timeBudgetText.toIntOrNull() ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Set daily limit for", style = MaterialTheme.typography.titleMedium)
                Text(appName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                DurationStepper(
                    value = timeBudget,
                    onValueChange = { timeBudgetText = it.toString() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = timeBudgetText,
                    onValueChange = { text ->
                        if (text.isEmpty() || text.toIntOrNull() != null) {
                            timeBudgetText = text
                        }
                    },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onSave(timeBudget) }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun DurationStepper(value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier, step: Int = 5) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedIconButton(onClick = { onValueChange((value - step).coerceAtLeast(5)) }) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease duration")
        }
        AnimatedContent(
            targetState = formatMinutes(value.toLong()),
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { height -> height } + fadeIn() togetherWith slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith slideOutVertically { height -> height } + fadeOut()
                }.using(SizeTransform(clip = false))
            }, label = "TimeBudget"
        ) { formattedTime ->
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        OutlinedIconButton(onClick = { onValueChange(value + step) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase duration")
        }
    }
}

@Composable
fun EmptyAppsState(modifier: Modifier = Modifier, onRescan: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No Applications Found", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("We couldn't find any other apps on your device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRescan) {
            Text("Rescan Apps")
        }
    }
}

private fun formatMinutes(minutes: Long): String {
    if (minutes < 0) return "0 min"
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    return when {
        hours > 0 && remainingMinutes > 0 -> "${hours}hr ${remainingMinutes}min"
        hours > 0 -> "${hours}hr"
        else -> "$minutes min"
    }
}

@Preview(showBackground = true)
@Composable
fun FocusSettingsScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        FocusSettingsScreen(onBackClicked = {})
    }
}

