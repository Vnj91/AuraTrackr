package com.example.auratrackr.features.friends.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.LeaderboardViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBackClicked: () -> Unit,
    currentUserId: String?,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val isRefreshing = uiState.pageState is LoadState.Refreshing
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)

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
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.loadLeaderboard() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LeaderboardContent(
                uiState = uiState,
                currentUserId = currentUserId,
                onRetry = { viewModel.loadLeaderboard(isInitialLoad = true) }
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardUiState,
    currentUserId: String?,
    onRetry: () -> Unit
) {
    val listState = rememberLazyListState()
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val pageState = uiState.pageState) {
            is LoadState.Loading -> {
                CircularProgressIndicator()
            }
            is LoadState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: ${pageState.error.message ?: "Unknown error"}")
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
            is LoadState.Success, is LoadState.Refreshing -> {
                if (uiState.rankedUsers.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Text("Leaderboard is Empty", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "Add some friends to see how you rank!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.rankedUsers,
                            key = { _, user -> user.uid }
                        ) { index, user ->
                            LeaderboardItem(
                                rank = index + 1,
                                user = user,
                                isCurrentUser = user.uid == currentUserId
                            )
                        }
                    }
                }
            }
            else -> { /* Do nothing for other states */ }
        }
    }
}

@Composable
fun LeaderboardItem(
    rank: Int,
    user: User,
    isCurrentUser: Boolean
) {
    val cardColor = getLeaderboardCardColor(rank, isCurrentUser)
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
                contentDescription = "${user.username}'s Profile Picture",
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

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Aura Points",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${user.auraPoints}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

// ✅ FIX: Added the OptIn annotation to the preview function to resolve the experimental API warning.
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Leaderboard Light")
@Composable
fun LeaderboardScreenLightPreview() {
    val sampleUsers = listOf(
        User(uid = "1", username = "Alice", auraPoints = 1250),
        User(uid = "2", username = "Bob", auraPoints = 980),
        User(uid = "3", username = "Charlie", auraPoints = 850),
        User(uid = "4", username = "You", auraPoints = 720)
    )
    AuraTrackrTheme(useDarkTheme = false) {
        Scaffold(topBar = { TopAppBar(title = { Text("Weekly Leaderboard") }) }) { padding ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(sampleUsers) { index, user ->
                    LeaderboardItem(rank = index + 1, user = user, isCurrentUser = user.uid == "4")
                }
            }
        }
    }
}

@Composable
private fun getLeaderboardCardColor(rank: Int, isCurrentUser: Boolean): Color {
    return when {
        rank == 1 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
        rank == 2 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        rank == 3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        isCurrentUser -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}
