package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.dashboard.viewmodel.DashboardUiState
import com.example.auratrackr.features.dashboard.viewmodel.DashboardViewModel
import com.example.auratrackr.ui.theme.AuraTrackrTheme

// Common spacing and sizing constants used in this file
private val HORIZONTAL_PADDING = 24.dp
private val LARGE_VERTICAL_SPACER = 32.dp
private val AVATAR_SIZE = 48.dp
private val contentVerticalPadding = 16.dp
private val mediumSpacer = 16.dp
private val SPACER_12 = 12.dp

@Composable
fun DashboardHeader(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SPACER_12)) {
            AvatarWithInitials(
                imageUrl = uiState.profilePictureUrl,
                name = uiState.username,
                modifier = Modifier.size(AVATAR_SIZE)
            )
            Column {
                Text(
                    "Hi, ${uiState.username}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        AuraPointsChip(points = uiState.auraPoints)
    }
}

@Composable
fun ScheduleHeader(onVibeClicked: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Your Schedule",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text("Today's Activity", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = onVibeClicked) {
            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Vibe")
        }
    }
}

@Composable
fun DashboardScreenContent(
    viewModel: DashboardViewModel = hiltViewModel(),
    mainNavController: NavController,
    onVibeClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Delegate the heavy LazyColumn content to a small helper composable to keep
    // this top-level function under the detekt LongMethod threshold.
    DashboardContentList(
        uiState = uiState,
        viewModel = viewModel,
        mainNavController = mainNavController,
        onVibeClicked = onVibeClicked
    )
}

@Composable
private fun DashboardContentList(
    uiState: com.example.auratrackr.features.dashboard.viewmodel.DashboardUiState,
    viewModel: DashboardViewModel,
    mainNavController: NavController,
    onVibeClicked: () -> Unit
) {
    // iOS-style parallax scroll state
    val listState = rememberLazyListState()
    val scrollOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset.toFloat() } }
    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    
    // Parallax effect calculations
    val headerParallaxOffset by animateFloatAsState(
        targetValue = if (firstVisibleIndex == 0) scrollOffset * 0.5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "header_parallax"
    )
    
    val headerScale by animateFloatAsState(
        targetValue = (1f + (scrollOffset / 1000f)).coerceIn(1f, 1.2f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "header_scale"
    )
    
    val headerBlur by animateFloatAsState(
        targetValue = (scrollOffset / 100f).coerceIn(0f, 10f),
        animationSpec = tween(durationMillis = 150),
        label = "header_blur"
    )
    
    // Card entrance animations with stagger
    var cardsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(200)
        cardsVisible = true
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Dynamic gradient background with parallax
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.03f)
                        ),
                        startY = -headerParallaxOffset
                    )
                )
        )
        
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentPadding = PaddingValues(vertical = contentVerticalPadding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = -headerParallaxOffset
                            scaleX = headerScale
                            scaleY = headerScale
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                        }
                        .blur(headerBlur.dp)
                ) {
                    DashboardHeader(
                        uiState = uiState,
                        modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                    )
                }
            }

            item {
                val cardOffset by animateFloatAsState(
                    targetValue = if (cardsVisible) 0f else 100f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "card_slide"
                )
                
                val cardScale by animateFloatAsState(
                    targetValue = if (cardsVisible) 1f else 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "card_scale"
                )
                
                Spacer(modifier = Modifier.height(LARGE_VERTICAL_SPACER))
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationY = cardOffset
                            scaleX = cardScale
                            scaleY = cardScale
                            alpha = if (cardsVisible) 1f else 0f
                        }
                ) {
                    PointsByVibeCard(
                        isLoading = uiState.isLoading,
                        pointsByVibe = uiState.pointsByVibe,
                        modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                    )
                }
            }

        item {
            val scheduleHeaderOffset by animateFloatAsState(
                targetValue = if (cardsVisible) 0f else 80f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "schedule_header_slide"
            )
            
            Spacer(modifier = Modifier.height(LARGE_VERTICAL_SPACER))
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = scheduleHeaderOffset
                        alpha = if (cardsVisible) 1f else 0f
                    }
            ) {
                ScheduleHeader(
                    onVibeClicked = onVibeClicked,
                    modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                )
            }
        }

        item {
            var timelineVisible by remember { mutableStateOf(false) }
            LaunchedEffect(cardsVisible) {
                if (cardsVisible) {
                    delay(400)
                    timelineVisible = true
                }
            }
            
            val timelineOffset by animateFloatAsState(
                targetValue = if (timelineVisible) 0f else 60f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "timeline_entrance"
            )
            
            val timelineScale by animateFloatAsState(
                targetValue = if (timelineVisible) 1f else 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "timeline_scale"
            )
            
            Spacer(modifier = Modifier.height(mediumSpacer))
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = timelineOffset
                        scaleX = timelineScale
                        scaleY = timelineScale
                        alpha = if (timelineVisible) 1f else 0f
                    }
            ) {
                if (uiState.isLoading) {
                    Text(
                        "Loading schedule...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                    )
                } else {
                    uiState.todaysSchedule?.let { schedule ->
                        ScheduleTimeline(
                            schedule = schedule,
                            onStartWorkout = { workout ->
                                viewModel.startWorkout(schedule.id, workout.id)
                                mainNavController.navigate(
                                    Screen.WorkoutInProgress.createRoute(
                                        scheduleId = schedule.id,
                                        workoutId = workout.id
                                    )
                                )
                            },
                            modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING)
                        )
                    } ?: EmptySchedulePlaceholder(onAddScheduleClicked = {
                        mainNavController.navigate(Screen.ScheduleEditor.createRoute())
                    })
                }
            }
        }
    }
}

// Moved several helper composables (WorkoutItem, WorkoutDetails, WorkoutTimelineDot,
// WorkoutProgressBar, ScheduleTimeline, EmptySchedulePlaceholder, PointsByVibeCard,
// PointsByVibeBarChart, AuraPointsChip, AvatarWithInitials) into
// `DashboardParts.kt` to reduce function count and LongMethod weight in this file.

@Preview(showBackground = true)
@Composable
fun DashboardScreenContentPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        DashboardScreenContent(mainNavController = rememberNavController(), onVibeClicked = {})
    }
}
}
