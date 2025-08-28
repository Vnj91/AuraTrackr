package com.example.auratrackr.features.friends.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.LeaderboardViewModel
import com.example.auratrackr.features.friends.viewmodel.LoadState
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.launch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBackClicked: () -> Unit,
    currentUserId: String?,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val showJumpToMeButton by remember(uiState.rankedUsers, listState.firstVisibleItemIndex) {
        derivedStateOf {
            val currentUserIndex = uiState.rankedUsers.indexOfFirst { it.uid == currentUserId }
            currentUserIndex > -1 && listState.firstVisibleItemIndex > currentUserIndex
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (showJumpToMeButton) {
                FloatingActionButton(onClick = {
                    coroutineScope.launch {
                        val currentUserIndex = uiState.rankedUsers.indexOfFirst { it.uid == currentUserId }
                        if (currentUserIndex != -1) {
                            listState.animateScrollToItem(currentUserIndex)
                        }
                    }
                }) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Jump to my rank")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.pageState is LoadState.Loading,
            onRefresh = { viewModel.loadLeaderboard() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState.pageState) {
                    is LoadState.Loading -> {
                        if (uiState.rankedUsers.isEmpty()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is LoadState.Idle -> {
                        if (uiState.error != null) {
                            LeaderboardErrorState(
                                message = uiState.error!!.message,
                                onRetry = { viewModel.loadLeaderboard() }
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(uiState.rankedUsers, key = { _, user -> user.uid }) { index, user ->
                                    LeaderboardItem(
                                        rank = index + 1,
                                        user = user,
                                        isCurrentUser = user.uid == currentUserId
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
}

@Composable
fun LeaderboardItem(
    rank: Int,
    user: User,
    isCurrentUser: Boolean
) {
    val cardColor = when {
        rank == 1 -> Color(0xFFFFF9C4)
        rank == 2 -> Color(0xFFF0F0F0)
        rank == 3 -> Color(0xFFFFE0B2)
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val border = if (isCurrentUser) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp)
            )

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
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Text(
                text = user.username ?: "Unknown User",
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrentUser) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${user.auraPoints}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (rank <= 3) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Top 3 Rank",
                    tint = when (rank) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        else -> Color(0xFFCD7F32)
                    }
                )
            }
        }
    }
}

@Composable
fun LeaderboardErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LeaderboardScreenPreview() {
    val sampleUsers = listOf(
        User(uid = "1", username = "Alice", auraPoints = 1250),
        User(uid = "2", username = "Bob", auraPoints = 980),
        User(uid = "3", username = "Charlie", auraPoints = 850),
        User(uid = "4", username = "You", auraPoints = 720)
    )
    AuraTrackrTheme(useDarkTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Weekly Leaderboard") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(sampleUsers) { index, user ->
                    LeaderboardItem(
                        rank = index + 1,
                        user = user,
                        isCurrentUser = user.uid == "4"
                    )
                }
            }
        }
    }
}
