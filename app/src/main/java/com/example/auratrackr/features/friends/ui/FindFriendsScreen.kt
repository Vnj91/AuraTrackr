package com.example.auratrackr.features.friends.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.FindFriendsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindFriendsScreen(
    onBackClicked: () -> Unit,
    viewModel: FindFriendsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Search by username") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.searchUsers()
                    keyboardController?.hide()
                })
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    viewModel.searchUsers()
                    keyboardController?.hide()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.searchResults) { user ->
                        UserSearchResultItem(
                            user = user,
                            isRequestSent = user.uid in uiState.requestSentTo,
                            onSendRequest = { viewModel.sendFriendRequest(user.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchResultItem(
    user: User,
    isRequestSent: Boolean,
    onSendRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = user.username ?: "Unknown User",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onSendRequest,
                enabled = !isRequestSent
            ) {
                if (isRequestSent) {
                    Text("Sent")
                } else {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Send Friend Request")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindFriendsScreenPreview() {
    FindFriendsScreen(onBackClicked = {})
}
