package com.example.auratrackr.features.friends.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.FriendsEvent
import com.example.auratrackr.features.friends.viewmodel.FriendsViewModel
import com.example.auratrackr.features.friends.viewmodel.LoadState
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBackClicked: () -> Unit,
    onFindFriendsClicked: () -> Unit,
    onLeaderboardClicked: () -> Unit,
    onChallengesClicked: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Friends", "Requests")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is FriendsEvent.ShowSnackbar -> {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel
                        )
                        if (result == SnackbarResult.ActionPerformed && event.actionLabel == "Undo") {
                            // This requires a way to hold onto the request that was declined.
                            // For simplicity, we'll assume the ViewModel can handle this.
                        }
                    }
                }
                is FriendsEvent.UndoDecline -> {
                    viewModel.undoDecline(event.request)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onFindFriendsClicked) {
                        Icon(Icons.Default.PersonSearch, contentDescription = "Find Friends")
                    }
                    IconButton(onClick = onChallengesClicked) {
                        Icon(Icons.Default.Groups, contentDescription = "Group Challenges")
                    }
                    IconButton(onClick = onLeaderboardClicked) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(title)
                                if (index == 1 && uiState.friendRequests.isNotEmpty()) {
                                    Badge { Text("${uiState.friendRequests.size}") }
                                }
                            }
                        }
                    )
                }
            }

            when (uiState.pageState) {
                is LoadState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is LoadState.Idle -> {
                    if (uiState.error != null) {
                        ErrorState(
                            message = uiState.error!!.message,
                            onRetry = { viewModel.loadFriendsAndRequests() }
                        )
                    } else {
                        when (selectedTabIndex) {
                            0 -> FriendsList(
                                friends = uiState.friends,
                                onFindFriendsClicked = onFindFriendsClicked
                            )
                            1 -> FriendRequestsList(
                                requests = uiState.friendRequests,
                                processingIds = uiState.processingRequestIds,
                                onAccept = { viewModel.acceptFriendRequest(it) },
                                onDecline = { viewModel.declineFriendRequest(it) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun FriendsList(friends: List<User>, onFindFriendsClicked: () -> Unit) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Default.People,
            title = "No Friends Yet",
            message = "Find some friends to start competing and sharing your progress!",
            actionText = "Find Friends",
            onActionClicked = onFindFriendsClicked
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
        ) {
            items(friends, key = { it.uid }) { friend ->
                FriendItem(user = friend)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun FriendRequestsList(
    requests: List<FriendRequest>,
    processingIds: Set<String>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit
) {
    if (requests.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Notifications,
            title = "No New Requests",
            message = "You have no pending friend requests at the moment."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
        ) {
            items(requests, key = { it.id }) { request ->
                FriendRequestItem(
                    request = request,
                    isProcessing = request.id in processingIds,
                    onAccept = { onAccept(request) },
                    onDecline = { onDecline(request) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun FriendItem(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // ✅ FIX: The clickable modifier now uses the modern, default ripple effect.
            .clickable(onClick = { /* TODO: Navigate to user profile */ })
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.profilePictureUrl)
                .crossfade(true)
                .error(R.drawable.ic_person_placeholder)
                .placeholder(R.drawable.ic_person_placeholder)
                .build(),
            contentDescription = "${user.username}'s profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Text(user.username ?: "Unknown User", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun FriendRequestItem(
    request: FriendRequest,
    isProcessing: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(request.senderProfileImageUrl)
                .crossfade(true)
                .error(R.drawable.ic_person_placeholder)
                .placeholder(R.drawable.ic_person_placeholder)
                .build(),
            contentDescription = "${request.senderUsername}'s profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Text(
            text = "${request.senderUsername} sent you a request.",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDecline) {
                    Text("Decline")
                }
                Button(onClick = onAccept) {
                    Text("Accept")
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onActionClicked: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onActionClicked != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onActionClicked,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(actionText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        FriendsScreen({}, {}, {}, {})
    }
}
