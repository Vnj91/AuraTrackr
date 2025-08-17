package com.example.auratrackr.features.friends.ui

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
import androidx.compose.ui.res.stringResource
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
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.flow.collectLatest

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

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is FriendsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
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
                    IconButton(onClick = onChallengesClicked) {
                        Icon(Icons.Default.Groups, contentDescription = "Group Challenges")
                    }
                    IconButton(onClick = onLeaderboardClicked) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard")
                    }
                    TextButton(onClick = onFindFriendsClicked) {
                        Text("Find")
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
                        text = { Text(title) },
                        icon = {
                            if (index == 1 && uiState.friendRequests.isNotEmpty()) {
                                Badge { Text("${uiState.friendRequests.size}") }
                            }
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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
    }
}

@Composable
fun FriendsList(friends: List<User>, onFindFriendsClicked: () -> Unit) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Default.People,
            message = "You haven't added any friends yet. Find some friends to start competing!",
            actionText = "Find Friends",
            onActionClicked = onFindFriendsClicked
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends, key = { it.uid }) { friend ->
                FriendItem(user = friend)
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
            message = "You have no pending friend requests.",
            actionText = null,
            onActionClicked = {}
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests, key = { it.id }) { request ->
                FriendRequestItem(
                    request = request,
                    isProcessing = request.id in processingIds,
                    onAccept = { onAccept(request) },
                    onDecline = { onDecline(request) }
                )
            }
        }
    }
}

@Composable
fun FriendItem(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePictureUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Text(user.username ?: "Unknown User", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FriendRequestItem(
    request: FriendRequest,
    isProcessing: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(request.senderProfileImageUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "Sender Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Text(
                text = "${request.senderUsername} sent you a request.",
                modifier = Modifier.weight(1f)
            )
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Row {
                    IconButton(onClick = onDecline) {
                        Icon(Icons.Default.Clear, contentDescription = "Decline Request", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onAccept) {
                        Icon(Icons.Default.Check, contentDescription = "Accept Request", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    actionText: String?,
    onActionClicked: () -> Unit
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
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onActionClicked) {
                Text(actionText)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsScreenPreview() {
    AuraTrackrTheme {
        FriendsScreen({}, {}, {}, {})
    }
}
