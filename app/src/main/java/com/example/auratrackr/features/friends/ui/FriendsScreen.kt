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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.FriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    onBackClicked: () -> Unit,
    onFindFriendsClicked: () -> Unit,
    onLeaderboardClicked: () -> Unit,
    onChallengesClicked: () -> Unit, // <-- ADDED THIS CALLBACK
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("My Friends", "Requests")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // *** THIS IS THE UPDATE ***
                    // Add the button to navigate to the Challenges screen
                    IconButton(onClick = onChallengesClicked) {
                        Icon(Icons.Default.Groups, contentDescription = "Group Challenges")
                    }
                    IconButton(onClick = onLeaderboardClicked) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard")
                    }
                    TextButton(onClick = onFindFriendsClicked) {
                        Text("Find Friends")
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
                        text = { Text(title) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTabIndex) {
                    0 -> FriendsList(friends = uiState.friends)
                    1 -> FriendRequestsList(
                        requests = uiState.friendRequests,
                        onAccept = { viewModel.acceptFriendRequest(it) },
                        onDecline = { viewModel.declineFriendRequest(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendsList(friends: List<User>) {
    if (friends.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("You haven't added any friends yet.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(friends) { friend ->
                FriendItem(user = friend)
            }
        }
    }
}

@Composable
fun FriendRequestsList(
    requests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit
) {
    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("You have no pending friend requests.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(requests) { request ->
                FriendRequestItem(
                    request = request,
                    onAccept = { onAccept(request) },
                    onDecline = { onDecline(request) }
                )
            }
        }
    }
}

@Composable
fun FriendItem(user: User) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(40.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(user.username ?: "Unknown User", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FriendRequestItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(40.dp).clip(CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${request.senderUsername} sent you a request.",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDecline) {
                Icon(Icons.Default.Clear, contentDescription = "Decline", tint = Color.Red)
            }
            IconButton(onClick = onAccept) {
                Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Green)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FriendsScreenPreview() {
    FriendsScreen(onBackClicked = {}, onFindFriendsClicked = {}, onLeaderboardClicked = {}, onChallengesClicked = {})
}
