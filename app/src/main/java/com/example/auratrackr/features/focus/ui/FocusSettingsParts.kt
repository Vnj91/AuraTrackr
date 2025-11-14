@file:OptIn(ExperimentalAnimationApi::class)

package com.example.auratrackr.features.focus.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import com.example.auratrackr.ui.components.GlassCard
import kotlinx.coroutines.delay
import com.example.auratrackr.ui.components.GlassCard
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsUiState
import com.example.auratrackr.features.focus.viewmodel.FocusSettingsViewModel
import com.example.auratrackr.features.focus.viewmodel.MonitoredApp
import com.example.auratrackr.ui.theme.Dimensions
import kotlinx.coroutines.flow.collectLatest
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party

// Local copies of focus layout constants used by the parts to avoid moving file-private vals.
private val PART_FOCUS_HORIZONTAL_PADDING = 16.dp
private val PART_FOCUS_ICON_SIZE = 48.dp
private val PART_FOCUS_LIST_ITEM_VERTICAL_PADDING = 12.dp
private val PART_FOCUS_SPACER_WIDTH = 16.dp
private val PART_FOCUS_EMPTY_PADDING = 32.dp

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
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

    val switchScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (monitoredApp.isMonitored) 1f else 0.9f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.4f),
        label = "SwitchScale"
    )
    
    // Bouncing icon animation
    var iconLoaded by remember { mutableStateOf(false) }
    val iconScale by animateFloatAsState(
        targetValue = if (iconLoaded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_bounce"
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PART_FOCUS_HORIZONTAL_PADDING, vertical = 8.dp)
            .clickable(onClick = onRowClicked)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PART_FOCUS_LIST_ITEM_VERTICAL_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "${monitoredApp.app.name} icon",
                modifier = Modifier
                    .size(PART_FOCUS_ICON_SIZE)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
                onSuccess = { iconLoaded = true }
            )
            Spacer(modifier = Modifier.width(PART_FOCUS_SPACER_WIDTH))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = monitoredApp.app.name, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                Text(
                    text = if (monitoredApp.isMonitored) {
                        "Limit: ${formatMinutes(monitoredApp.budget?.timeBudgetInMinutes ?: 0L)}"
                    } else {
                        "Tap to set budget"
                    },
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(PART_FOCUS_SPACER_WIDTH))
            Switch(
                modifier = Modifier.scale(switchScale),
                checked = monitoredApp.isMonitored,
                onCheckedChange = onAppSelected,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun FocusSettingsOverlays(
    konfettiParty: Party?,
    uiState: com.example.auratrackr.features.focus.viewmodel.FocusSettingsUiState,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = konfettiParty?.let { listOf(it) } ?: emptyList()
        )

        AnimatedVisibility(
            visible = uiState.monitoredApps.isEmpty() && !uiState.isLoading,
            enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(delayMillis = 300)),
            exit = fadeOut()
        ) {
            EmptyAppsState(
                modifier = Modifier.align(Alignment.Center),
                onRescan = onRescan
            )
        }
    }
}

@Composable
fun EmptyAppsState(modifier: Modifier = Modifier, onRescan: () -> Unit) {
    Column(
        modifier = modifier.padding(PART_FOCUS_EMPTY_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(PART_FOCUS_SPACER_WIDTH))
        Text(
            "No Applications Found",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            "We couldn't find any other apps on your device.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRescan) {
            Text("Rescan Apps")
        }
    }
}

/**
 * Small extracted parts from FocusSettingsScreen to reduce the top-level function length.
 */

@Composable
fun ShowBudgetAndKonfetti(
    dialogApp: MonitoredApp?,
    setDialogApp: (MonitoredApp?) -> Unit,
    viewModel: FocusSettingsViewModel,
    setKonfettiParty: (nl.dionsegijn.konfetti.core.Party?) -> Unit
) {
    dialogApp?.let { appToShowDialog ->
        BudgetSettingDialog(
            appName = appToShowDialog.app.name,
            initialTimeBudget = (appToShowDialog.budget?.timeBudgetInMinutes ?: 60L).toInt(),
            onDismiss = { setDialogApp(null) },
            onSave = { timeBudget ->
                viewModel.addAppToMonitor(appToShowDialog.app, timeBudget.toLong())
                setDialogApp(null)
                setKonfettiParty(
                    nl.dionsegijn.konfetti.core.Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                        emitter = nl.dionsegijn.konfetti.core.emitter.Emitter(
                            duration = 100,
                            java.util.concurrent.TimeUnit.MILLISECONDS
                        ).max(100),
                        position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3)
                    )
                )
            }
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
        GlassCard(
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Set daily limit for", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(
                    appName,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                DurationStepper(
                    value = timeBudget,
                    onValueChange = { timeBudgetText = it.toString() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = timeBudgetText,
                    onValueChange = { text -> if (text.isEmpty() || text.toIntOrNull() != null) timeBudgetText = text },
                    label = { Text("Minutes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(Dimensions.Small))
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
        AnimatedContent(targetState = value, transitionSpec = { fadeIn() with fadeOut() }) { current ->
            Text(
                text = formatMinutes(current.toLong()),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        OutlinedIconButton(onClick = { onValueChange(value + step) }) {
            Icon(Icons.Default.Add, contentDescription = "Increase duration")
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

/**
 * Extracted event handler to keep the main FocusSettingsScreen small.
 * Runs a LaunchedEffect that listens to viewModel.eventFlow and shows
 * a snackbar via the provided SnackbarHostState.
 */
@Composable
fun HandleFocusSettingsEvents(
    viewModel: FocusSettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is com.example.auratrackr.features.focus.viewmodel.FocusSettingsEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
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
}

@Composable
fun FocusSettingsContent(
    uiState: FocusSettingsUiState,
    onAppSelected: (MonitoredApp, Boolean) -> Unit,
    onRowClicked: (MonitoredApp) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier
    ) {
        item {
            Text(
                text = "Monitored Apps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        itemsIndexed(uiState.monitoredApps, key = { _, it -> it.app.packageName }) { index, monitoredApp ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(delayMillis = index * 50)
                ) + fadeIn(),
                exit = fadeOut()
            ) {
                AppListItem(
                    monitoredApp = monitoredApp,
                    onAppSelected = { enabled -> onAppSelected(monitoredApp, enabled) },
                    onRowClicked = { onRowClicked(monitoredApp) }
                )
            }
        }
    }
}
