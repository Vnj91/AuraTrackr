package com.example.auratrackr.features.friends.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.LeaderboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBackClicked: () -> Unit,
    currentUserId: String?, // Pass the current user's ID to highlight them
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(uiState.rankedUsers) { index, user ->
                        LeaderboardItem(
                            rank = index + 1,
                            user = user,
                            isCurrentUser = user.uid == currentUserId
                        )
                    }
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
    val cardColor = if (isCurrentUser) Color(0xFFD4B42A).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Text(
                text = "$rank",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Profile Pic
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Username
            Text(
                text = user.username ?: "Unknown User",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Aura Points
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Aura Points",
                    tint = Color(0xFFD4B42A)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${user.auraPoints}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Crown for 1st place
            if (rank == 1) {
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "First Place",
                    tint = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LeaderboardScreenPreview() {
    val sampleUsers = listOf(
        User(uid = "1", username = "Alice", auraPoints = 1250),
        User(uid = "2", username = "Bob", auraPoints = 980),
        User(uid = "3", username = "You", auraPoints = 850),
        User(uid = "4", username = "Charlie", auraPoints = 720)
    )
    // LeaderboardScreen(onBackClicked = {}, currentUserId = "3")
}
