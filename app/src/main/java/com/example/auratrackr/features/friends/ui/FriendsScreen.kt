package com.example.auratrackr.features.friends.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.core.ui.LoadState
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.features.friends.viewmodel.FriendsEvent
import com.example.auratrackr.features.friends.viewmodel.FriendsViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.Dimensions
import kotlinx.coroutines.flow.collectLatest

// Layout constants for Friends screen
private val FRIENDS_SCREEN_HORIZONTAL_PADDING = 16.dp
private val FRIENDS_SPACER_MEDIUM = 12.dp
private val FRIENDS_ICON_SIZE = 48.dp
private val FRIENDS_EMPTY_ICON_SIZE = 64.dp

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

    // Collect UI events from the ViewModel and show snackbars when needed.
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is FriendsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is FriendsEvent.UndoDecline -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Request declined.",
                        actionLabel = "Undo"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDeclineFriendRequest(event.request)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FriendsTopBar(
                onBackClicked = onBackClicked,
                onChallengesClicked = onChallengesClicked,
                onLeaderboardClicked = onLeaderboardClicked,
                onFindFriendsClicked = onFindFriendsClicked
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(title)
                                if (index == 1 && uiState.friendRequests.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    // Pulsing badge animation
                                    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
                                    val badgeScale by infiniteTransition.animateFloat(
                                        initialValue = 1f,
                                        targetValue = 1.15f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "badge_scale"
                                    )
                                    
                                    Badge(
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = badgeScale
                                            scaleY = badgeScale
                                        }
                                    ) {
                                        Text("${uiState.friendRequests.size}")
                                    }
                                }
                            }
                        }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedTabIndex to uiState.pageState,
                label = "PageStateAnimation",
                transitionSpec = {
                    val direction = if (targetState.first > initialState.first) 1 else -1
                    slideInHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        initialOffsetX = { it * direction }
                    ) + fadeIn() togetherWith
                    slideOutHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        targetOffsetX = { -it * direction }
                    ) + fadeOut()
                }
            ) { (tabIndex, pageState) ->
                when (pageState) {
                    is LoadState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is LoadState.Error -> {
                        ErrorState(
                            message = pageState.error.message,
                            onRetry = { /* Implement retry logic if needed */ }
                        )
                    }
                    is LoadState.Success -> {
                        when (tabIndex) {
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
                    else -> {} // Handle other states if needed
                }
            }
        }
    }
}

@Composable
fun FriendsList(friends: List<User>, onFindFriendsClicked: () -> Unit) {
    if (friends.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Groups,
            title = "No Friends Yet",
            message = "Find friends to start competing and challenging each other!",
            actionText = "Find Friends",
            onActionClicked = onFindFriendsClicked
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(FRIENDS_SCREEN_HORIZONTAL_PADDING),
            verticalArrangement = Arrangement.spacedBy(FRIENDS_SPACER_MEDIUM)
        ) {
            items(friends.size, key = { friends[it].uid }) { index ->
                val friend = friends[index]
                var isVisible by remember { mutableIntStateOf(0) }
                
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 50L)
                    isVisible = 1
                }
                
                val scale by animateFloatAsState(
                    targetValue = if (isVisible == 1) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "friend_entrance"
                )
                
                Box(modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }) {
                    FriendItem(user = friend)
                }
                Divider(modifier = Modifier.padding(top = FRIENDS_SPACER_MEDIUM))
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
            title = "All Caught Up!",
            message = "You have no pending friend requests.",
            actionText = null,
            onActionClicked = {}
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(FRIENDS_SCREEN_HORIZONTAL_PADDING),
            verticalArrangement = Arrangement.spacedBy(FRIENDS_SPACER_MEDIUM)
        ) {
            items(requests, key = { it.id }) { request ->
                var offsetX by remember { mutableIntStateOf(0) }
                var isDismissed by remember { mutableIntStateOf(0) }
                
                if (isDismissed == 0) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX, 0) }
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (offsetX.absoluteValue > 200) {
                                            if (offsetX > 0) {
                                                onAccept(request)
                                            } else {
                                                onDecline(request)
                                            }
                                            isDismissed = 1
                                        } else {
                                            offsetX = 0
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        offsetX = (offsetX + dragAmount.toInt()).coerceIn(-300, 300)
                                    }
                                )
                            }
                    ) {
                        FriendRequestItem(
                            request = request,
                            isProcessing = request.id in processingIds,
                            onAccept = { onAccept(request) },
                            onDecline = { onDecline(request) }
                        )
                    }
                    Divider(modifier = Modifier.padding(top = FRIENDS_SPACER_MEDIUM))
                }
            }
        }
    }
}

@Composable
fun FriendItem(user: User) {
    var isLoaded by remember { mutableIntStateOf(0) }
    val avatarScale by animateFloatAsState(
        targetValue = if (isLoaded == 1) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "avatar_bounce"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            contentDescription = "${user.username}'s Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(FRIENDS_ICON_SIZE)
                .clip(CircleShape)
                .graphicsLayer {
                    scaleX = avatarScale
                    scaleY = avatarScale
                },
            onSuccess = { isLoaded = 1 }
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
        modifier = Modifier.fillMaxWidth(),
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
            contentDescription = "Sender Profile Picture",
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
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Small)) {
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
    actionText: String?,
    onActionClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FRIENDS_SCREEN_HORIZONTAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(FRIENDS_EMPTY_ICON_SIZE),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FRIENDS_SPACER_MEDIUM))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FriendsTopBar(
    onBackClicked: () -> Unit,
    onChallengesClicked: () -> Unit,
    onLeaderboardClicked: () -> Unit,
    onFindFriendsClicked: () -> Unit
) {
    TopAppBar(
        title = { Text("Friends") },
        navigationIcon = {
            IconButton(
                onClick = onBackClicked,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(
                onClick = onChallengesClicked,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(Icons.Default.Groups, contentDescription = "Group Challenges")
            }
            IconButton(
                onClick = onLeaderboardClicked,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard")
            }
            IconButton(
                onClick = onFindFriendsClicked,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            ) {
                Icon(Icons.Default.PersonSearch, contentDescription = "Find Friends")
            }
        }
    )
}
