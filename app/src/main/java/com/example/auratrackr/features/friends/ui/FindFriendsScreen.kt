package com.example.auratrackr.features.friends.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.FindFriendsEvent
import com.example.auratrackr.features.friends.viewmodel.FindFriendsViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.Dimensions
import kotlinx.coroutines.flow.collectLatest

// Layout constants for FindFriends screen
private val FRIENDS_HORIZONTAL_PADDING = 16.dp
private val FRIENDS_ITEM_SPACER_LARGE = 16.dp
private val FRIENDS_ITEM_SPACER_SMALL = Dimensions.Small
private val FRIENDS_ITEM_CONTENT_PADDING = 12.dp
private val FRIENDS_AVATAR_SIZE = 40.dp
private val FRIENDS_BUTTON_HORIZONTAL_PADDING = 16.dp
private val FRIENDS_ICON_SPACER = 4.dp
private val FRIENDS_EMPTY_ICON_SIZE = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindFriendsScreen(
    onBackClicked: () -> Unit,
    viewModel: FindFriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is FindFriendsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FindFriendsEvent.RequestFailed -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.error.message,
                        actionLabel = "Retry"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.sendFriendRequest(event.receiverId)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Find Friends") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Delegate the main content to a smaller composable to keep the public
        // `FindFriendsScreen` function under detekt's LongMethod threshold.
        FindFriendsContent(
            paddingValues = paddingValues,
            uiState = uiState,
            keyboardController = keyboardController,
            focusManager = focusManager,
            viewModel = viewModel
        )
    }
}

@Composable
private fun FindFriendsContent(
    paddingValues: PaddingValues,
    uiState: com.example.auratrackr.core.ui.LoadState<*>? = null, // kept generic for preview compatibility
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController? = null,
    focusManager: androidx.compose.ui.platform.FocusManager,
    viewModel: FindFriendsViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(FRIENDS_HORIZONTAL_PADDING)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        SearchBar(
            query = viewModel.uiState.value.searchQuery,
            onQueryChange = { viewModel.onSearchQueryChanged(it) },
            onSearch = { keyboardController?.hide() },
            onClear = { viewModel.onSearchQueryChanged("") }
        )

        Spacer(modifier = Modifier.height(FRIENDS_ITEM_SPACER_LARGE))

        ResultsArea(viewModel = viewModel)
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search by username") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun ResultsArea(viewModel: FindFriendsViewModel) {
    AnimatedContent(
        targetState = viewModel.uiState.value.pageState,
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ContentStateAnimation"
    ) { pageState ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (pageState) {
                is LoadState.Idle -> {
                    EmptyState(
                        icon = Icons.Default.Search,
                        message = "Search for friends by their username to get started."
                    )
                }
                is LoadState.Loading -> {
                    CircularProgressIndicator()
                }
                is LoadState.Error -> {
                    ErrorState(
                        message = pageState.error.message,
                        onRetry = { viewModel.onSearchQueryChanged(viewModel.uiState.value.searchQuery) }
                    )
                }
                is LoadState.Success -> {
                    if (viewModel.uiState.value.searchQuery.isBlank()) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            message = "Start typing to search for friends."
                        )
                    } else if (viewModel.uiState.value.searchResults.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            message = "No users found for \"${viewModel.uiState.value.searchQuery}\""
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(FRIENDS_ITEM_SPACER_SMALL)) {
                            items(viewModel.uiState.value.searchResults, key = { it.uid }) { user ->
                                UserSearchResultItem(
                                    user = user,
                                    isRequestSent = user.uid in viewModel.uiState.value.requestSentTo,
                                    isSendingRequest = user.uid in viewModel.uiState.value.isSendingRequestTo,
                                    onSendRequest = { viewModel.sendFriendRequest(user.uid) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun UserSearchResultItem(
    user: User,
    isRequestSent: Boolean,
    isSendingRequest: Boolean,
    onSendRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRequestSent) {},
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(FRIENDS_ITEM_CONTENT_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FRIENDS_ITEM_CONTENT_PADDING)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePictureUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "${user.username}'s Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(FRIENDS_AVATAR_SIZE)
                    .clip(CircleShape)
            )
            Text(
                text = user.username ?: "Unknown User",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            FriendRequestButton(
                isRequestSent = isRequestSent,
                isSendingRequest = isSendingRequest,
                onSendRequest = onSendRequest
            )
        }
    }
}

@Composable
private fun FriendRequestButton(
    isRequestSent: Boolean,
    isSendingRequest: Boolean,
    onSendRequest: () -> Unit
) {
    FilledTonalButton(
        onClick = onSendRequest,
        enabled = !isRequestSent && !isSendingRequest,
        contentPadding = PaddingValues(horizontal = FRIENDS_BUTTON_HORIZONTAL_PADDING)
    ) {
        when {
            isSendingRequest -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    strokeWidth = 2.dp
                )
            }
            isRequestSent -> {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Request Sent",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(FRIENDS_ICON_SPACER))
                Text("Sent")
            }
            else -> {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(FRIENDS_ICON_SPACER))
                Text("Add")
            }
        }
    }
}

// NOTE: In a real project, these shared state composables would be moved to a common package.
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FRIENDS_HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(FRIENDS_EMPTY_ICON_SIZE),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(FRIENDS_ITEM_SPACER_LARGE))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(FRIENDS_ITEM_SPACER_LARGE))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FRIENDS_HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(FRIENDS_EMPTY_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FRIENDS_ITEM_SPACER_LARGE))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FindFriendsScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        Surface {
            FindFriendsScreen(onBackClicked = {})
        }
    }
}
