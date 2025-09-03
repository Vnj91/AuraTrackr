package com.example.auratrackr.features.challenges.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.domain.model.ChallengeMetric
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.challenges.viewmodel.ChallengeEvent
import com.example.auratrackr.features.challenges.viewmodel.ChallengesViewModel
import com.example.auratrackr.features.friends.ui.EmptyState
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    onBackClicked: () -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var selectedMetric by remember { mutableStateOf(ChallengeMetric.STEPS) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedFriendIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isFormValid by remember(title, goal, selectedEndDate) {
        derivedStateOf {
            title.isNotBlank() && (goal.toLongOrNull() ?: 0L) > 0 && selectedEndDate != null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is ChallengeEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ChallengeEvent.CreateSuccess -> onBackClicked()
            }
        }
    }

    if (showDatePicker) {
        ChallengeDatePickerDialog(
            onDateSelected = { selectedEndDate = it },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create New Challenge") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Challenge Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = goal,
                                onValueChange = { goal = it.filter { char -> char.isDigit() } },
                                label = { Text("Goal") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
                            )
                            MetricSelector(
                                selectedMetric = selectedMetric,
                                onMetricSelected = { selectedMetric = it }
                            )
                        }
                        OutlinedTextField(
                            value = selectedEndDate?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) ?: "",
                            onValueChange = {},
                            label = { Text("End Date") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true },
                            shape = RoundedCornerShape(16.dp),
                            readOnly = true,
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Invite Friends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (uiState.friends.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.People,
                            message = "You don't have any friends to invite yet. Add some first!"
                        )
                    }
                } else {
                    items(uiState.friends, key = { it.uid }) { friend ->
                        FriendInviteItem(
                            friend = friend,
                            isSelected = friend.uid in selectedFriendIds,
                            onSelectionChanged = {
                                selectedFriendIds = if (it) {
                                    selectedFriendIds + friend.uid
                                } else {
                                    selectedFriendIds - friend.uid
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    selectedEndDate?.let {
                        val endDate = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        viewModel.createChallenge(
                            title = title,
                            description = description,
                            goal = goal.toLong(),
                            metric = selectedMetric,
                            endDate = endDate,
                            participantIds = selectedFriendIds.toList()
                        )
                    }
                },
                enabled = isFormValid && uiState.pageState != LoadState.Submitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.pageState is LoadState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Challenge")
                }
            }
        }
    }
}

@Composable
fun FriendInviteItem(
    friend: User,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelectionChanged(!isSelected) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(friend.profilePictureUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "${friend.username}'s Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Text(friend.username ?: "Unknown User", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Checkbox(checked = isSelected, onCheckedChange = { onSelectionChanged(it) })
        }
    }
}

@Composable
fun MetricSelector(
    selectedMetric: ChallengeMetric,
    onMetricSelected: (ChallengeMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChallengeMetric.values().forEach { metric ->
            FilterChip(
                selected = selectedMetric == metric,
                onClick = { onMetricSelected(metric) },
                label = { Text(metric.unit) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeDatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= Instant.now().toEpochMilli()
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true)
@Composable
fun CreateChallengeScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        CreateChallengeScreen(onBackClicked = {})
    }
}

