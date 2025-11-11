package com.example.auratrackr.features.challenges.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.ChallengeMetric
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.ui.theme.Dimensions
import java.time.ZoneId
import java.util.Date

// Constants for spacing and sizing
private val CHALLENGE_ITEM_SPACER_LARGE = 16.dp
private val CHALLENGE_ITEM_SPACER_SMALL = Dimensions.Small
private val CHALLENGE_CONTENT_BOTTOM_PADDING = 80.dp

@Composable
fun MetricSelector(
    selectedMetric: com.example.auratrackr.domain.model.ChallengeMetric,
    onMetricSelected: (com.example.auratrackr.domain.model.ChallengeMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)
    ) {
        com.example.auratrackr.domain.model.ChallengeMetric.values().forEach { metric ->
            androidx.compose.material3.FilterChip(
                selected = selectedMetric == metric,
                onClick = { onMetricSelected(metric) },
                label = { Text(metric.unit) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CreateChallengeForm(
    formState: com.example.auratrackr.features.challenges.ui.CreateChallengeFormState,
    onFormStateChange: (com.example.auratrackr.features.challenges.ui.CreateChallengeFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CHALLENGE_ITEM_SPACER_LARGE)) {
        TitleInput(
            title = formState.title,
            onTitleChange = { onFormStateChange(formState.copy(title = it)) }
        )

        DescriptionInput(
            description = formState.description,
            onDescriptionChange = { onFormStateChange(formState.copy(description = it)) }
        )

        GoalMetricRow(
            goal = formState.goal,
            onGoalChange = { onFormStateChange(formState.copy(goal = it.filter { ch -> ch.isDigit() })) },
            selectedMetric = formState.selectedMetric,
            onMetricSelected = { onFormStateChange(formState.copy(selectedMetric = it)) }
        )
    }
}

@Composable
fun CreateChallengeContent(
    uiState: com.example.auratrackr.features.challenges.viewmodel.ChallengesUiState,
    state: com.example.auratrackr.features.challenges.ui.CreateChallengeScreenState,
    callbacks: com.example.auratrackr.features.challenges.ui.CreateChallengeContentCallbacks,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = CHALLENGE_CONTENT_BOTTOM_PADDING)
    ) {
        item {
            val formState = com.example.auratrackr.features.challenges.ui.CreateChallengeFormState(
                title = state.title,
                description = state.description,
                goal = state.goal,
                selectedMetric = state.selectedMetric,
                selectedEndDate = state.selectedEndDate
            )
            CreateChallengeForm(
                formState = formState,
                onFormStateChange = callbacks.onFormStateChange
            )
        }

        item {
            InviteFriendsSection(
                friends = uiState.friends,
                selectedFriendIds = state.selectedFriendIds,
                onToggleSelection = callbacks.onToggleSelection
            )
        }
    }

    Spacer(modifier = Modifier.height(CHALLENGE_ITEM_SPACER_SMALL))
    CreateChallengeButton(
        uiState = uiState,
        state = state,
        onCreateClicked = callbacks.onCreateClicked
    )
}

// Supporting small types moved with the content for clarity
data class CreateChallengeContentCallbacks(
    val onFormStateChange: (com.example.auratrackr.features.challenges.ui.CreateChallengeFormState) -> Unit,
    val onToggleSelection: (String) -> Unit,
    val onShowDatePicker: () -> Unit,
    val onCreateClicked: () -> Unit
)

data class CreateChallengeFormState(
    val title: String,
    val description: String,
    val goal: String,
    val selectedMetric: com.example.auratrackr.domain.model.ChallengeMetric,
    val selectedEndDate: java.time.LocalDate?
)

// A lightweight screen-level state used by CreateChallengeContent so callers can
// pass a snapshot of the current form values and selected friends.
data class CreateChallengeScreenState(
    val title: String,
    val description: String,
    val goal: String,
    val selectedMetric: com.example.auratrackr.domain.model.ChallengeMetric,
    val selectedEndDate: java.time.LocalDate?,
    val selectedFriendIds: Set<String>
)

// Local constants used by these smaller composables to avoid cross-file private visibility issues.
private val LOCAL_CHALLENGE_BUTTON_HEIGHT = 56.dp
private val LOCAL_CHALLENGE_PROGRESS_SIZE = 24.dp
private val LOCAL_CHALLENGE_CARD_CORNER = 16.dp
private val LOCAL_CHALLENGE_ITEM_SPACER_SMALL = Dimensions.Small

@Composable
fun TitleInput(title: String, onTitleChange: (String) -> Unit) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Challenge Title") },
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LOCAL_CHALLENGE_CARD_CORNER),
        singleLine = true
    )
}

@Composable
fun DescriptionInput(description: String, onDescriptionChange: (String) -> Unit) {
    OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(LOCAL_CHALLENGE_CARD_CORNER)
    )
}

@Composable
fun GoalMetricRow(
    goal: String,
    onGoalChange: (String) -> Unit,
    selectedMetric: ChallengeMetric,
    onMetricSelected: (ChallengeMetric) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(LOCAL_CHALLENGE_ITEM_SPACER_SMALL)) {
        OutlinedTextField(
            value = goal,
            onValueChange = onGoalChange,
            label = { Text("Goal") },
            modifier = Modifier.weight(1f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(LOCAL_CHALLENGE_CARD_CORNER),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
        )
        MetricSelector(
            selectedMetric = selectedMetric,
            onMetricSelected = onMetricSelected
        )
    }
}

@Composable
fun CreateChallengeButton(
    uiState: com.example.auratrackr.features.challenges.viewmodel.ChallengesUiState,
    state: com.example.auratrackr.features.challenges.ui.CreateChallengeScreenState,
    onCreateClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubmitting = uiState.pageState is com.example.auratrackr.core.ui.LoadState.Submitting
    val isFormValid = state.title.isNotBlank() && (state.goal.toLongOrNull() ?: 0L) > 0 && state.selectedEndDate != null

    Button(
        onClick = onCreateClicked,
        enabled = isFormValid && !isSubmitting,
        modifier = modifier
            .fillMaxWidth()
            .height(LOCAL_CHALLENGE_BUTTON_HEIGHT)
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(modifier = Modifier.height(LOCAL_CHALLENGE_PROGRESS_SIZE))
        } else {
            Text("Create Challenge")
        }
    }
}

@Composable
fun InviteFriendsSection(
    friends: List<User>,
    selectedFriendIds: Set<String>,
    onToggleSelection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(LOCAL_CHALLENGE_ITEM_SPACER_SMALL))
        Text(
            "Invite Friends",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(LOCAL_CHALLENGE_ITEM_SPACER_SMALL))

        if (friends.isEmpty()) {
            com.example.auratrackr.features.friends.ui.EmptyState(
                icon = Icons.Default.People,
                message = "You don't have any friends to invite yet. Add some first!"
            )
        } else {
            Column {
                friends.forEach { friend ->
                    FriendInviteItem(
                        friend = friend,
                        isSelected = friend.uid in selectedFriendIds,
                        onSelectionChanged = { onToggleSelection(friend.uid) }
                    )
                }
            }
        }
    }
}

// FriendInviteItem moved to CreateChallengeInviteParts.kt to keep this file focused.

/**
 * Build a CreateChallengeContentCallbacks instance that operates on MutableState holders.
 * This moves callback plumbing out of the top-level screen so the screen function stays short.
 */
// Group related MutableState holders into a single container to avoid long parameter lists.
data class CreateChallengeStateHolders(
    val titleState: MutableState<String>,
    val descriptionState: MutableState<String>,
    val goalState: MutableState<String>,
    val selectedMetricState: MutableState<ChallengeMetric>,
    val selectedEndDateState: MutableState<java.time.LocalDate?>,
    val selectedFriendIdsState: MutableState<Set<String>>,
    val showDatePickerState: MutableState<Boolean>
)

fun makeCreateChallengeCallbacks(
    holders: CreateChallengeStateHolders,
    viewModel: com.example.auratrackr.features.challenges.viewmodel.ChallengesViewModel
): CreateChallengeContentCallbacks =
    CreateChallengeContentCallbacks(
        onFormStateChange = { newState ->
            holders.titleState.value = newState.title
            holders.descriptionState.value = newState.description
            holders.goalState.value = newState.goal
            holders.selectedMetricState.value = newState.selectedMetric
            holders.selectedEndDateState.value = newState.selectedEndDate
        },
        onToggleSelection = { friendId ->
            holders.selectedFriendIdsState.value = if (friendId in holders.selectedFriendIdsState.value) {
                holders.selectedFriendIdsState.value - friendId
            } else {
                holders.selectedFriendIdsState.value + friendId
            }
        },
        onShowDatePicker = { holders.showDatePickerState.value = true },
        onCreateClicked = {
            holders.selectedEndDateState.value?.let {
                val endDate = Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
                val params = com.example.auratrackr.features.challenges.viewmodel.CreateChallengeParams(
                    title = holders.titleState.value,
                    description = holders.descriptionState.value,
                    goal = holders.goalState.value.toLongOrNull() ?: 0L,
                    metric = holders.selectedMetricState.value,
                    endDate = endDate,
                    participantIds = holders.selectedFriendIdsState.value.toList()
                )
                viewModel.createChallenge(params)
            }
        }
    )

// Small screen-level top bar extracted here so the screen function stays small.
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeTopBar(onBackClicked: () -> Unit) {
    androidx.compose.material3.SmallTopAppBar(
        title = { Text("Create Challenge") },
        navigationIcon = {
            androidx.compose.material3.IconButton(onClick = onBackClicked) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}

// Scaffold wrapper extracted out of the screen to reduce the top-level function size.
@Composable
fun CreateChallengeScreenScaffold(
    uiState: com.example.auratrackr.features.challenges.viewmodel.ChallengesUiState,
    holders: CreateChallengeStateHolders,
    viewModel: com.example.auratrackr.features.challenges.viewmodel.ChallengesViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    // centralize side-effects and snackbar handling inside the scaffold composable
    CreateChallengeHandlers(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        params = CreateChallengeHandlersParams(
            showDatePicker = holders.showDatePickerState.value,
            setShowDatePicker = { holders.showDatePickerState.value = it },
            setSelectedEndDate = { holders.selectedEndDateState.value = it },
            onBackClicked = onBackClicked
        )
    )

    // local horizontal padding (kept here to avoid cross-file private visibility issues)
    val horizontalPadding = 16.dp

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = { CreateChallengeTopBar(onBackClicked = onBackClicked) }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = horizontalPadding)
        ) {
            CreateChallengeContent(
                uiState = uiState,
                state = CreateChallengeScreenState(
                    title = holders.titleState.value,
                    description = holders.descriptionState.value,
                    goal = holders.goalState.value,
                    selectedMetric = holders.selectedMetricState.value,
                    selectedEndDate = holders.selectedEndDateState.value,
                    selectedFriendIds = holders.selectedFriendIdsState.value
                ),
                callbacks = makeCreateChallengeCallbacks(holders, viewModel),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(CHALLENGE_ITEM_SPACER_SMALL))
            CreateChallengeButton(
                uiState = uiState,
                state = CreateChallengeScreenState(
                    title = holders.titleState.value,
                    description = holders.descriptionState.value,
                    goal = holders.goalState.value,
                    selectedMetric = holders.selectedMetricState.value,
                    selectedEndDate = holders.selectedEndDateState.value,
                    selectedFriendIds = holders.selectedFriendIdsState.value
                ),
                onCreateClicked = makeCreateChallengeCallbacks(holders, viewModel).onCreateClicked
            )
        }
    }
}

// Small binding helper to keep the screen composable short. This wraps the more verbose
// CreateChallengeHandlers call into a single call site so detekt's LongMethod check
// for the screen is easier to satisfy.
@Composable
fun BindCreateChallengeHandlers(
    viewModel: com.example.auratrackr.features.challenges.viewmodel.ChallengesViewModel,
    snackbarHostState: androidx.compose.material3.SnackbarHostState,
    holders: CreateChallengeStateHolders,
    onBackClicked: () -> Unit
) {
    CreateChallengeHandlers(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        params = CreateChallengeHandlersParams(
            showDatePicker = holders.showDatePickerState.value,
            setShowDatePicker = { holders.showDatePickerState.value = it },
            setSelectedEndDate = { holders.selectedEndDateState.value = it },
            onBackClicked = onBackClicked
        )
    )
}
