package com.example.auratrackr.features.challenges.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.ChallengesViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChallengeScreen(
    onBackClicked: () -> Unit,
    viewModel: ChallengesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var selectedFriendIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    Scaffold(
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Challenge Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it.filter { char -> char.isDigit() } },
                label = { Text("Goal (e.g., 100000 steps)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Invite Friends", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.friends) { friend ->
                    FriendInviteItem(
                        friend = friend,
                        isSelected = friend.uid in selectedFriendIds,
                        onSelectionChanged = {
                            selectedFriendIds = if (friend.uid in selectedFriendIds) {
                                selectedFriendIds - friend.uid
                            } else {
                                selectedFriendIds + friend.uid
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    // TODO: Add date picker for end date
                    viewModel.createChallenge(
                        title = title,
                        description = description,
                        goal = goal.toLongOrNull() ?: 0L,
                        metric = "steps", // Hardcoded for now
                        endDate = Date(), // Placeholder
                        participantIds = selectedFriendIds.toList()
                    )
                    onBackClicked() // Go back after creating
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Create Challenge")
            }
        }
    }
}

@Composable
fun FriendInviteItem(
    friend: User,
    isSelected: Boolean,
    onSelectionChanged: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectionChanged)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(friend.username ?: "Unknown User", modifier = Modifier.weight(1f))
        Checkbox(checked = isSelected, onCheckedChange = { onSelectionChanged() })
    }
}

@Preview(showBackground = true)
@Composable
fun CreateChallengeScreenPreview() {
    CreateChallengeScreen(onBackClicked = {})
}
