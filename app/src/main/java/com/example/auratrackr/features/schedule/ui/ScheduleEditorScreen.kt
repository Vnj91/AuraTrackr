package com.example.auratrackr.features.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.features.schedule.viewmodel.ScheduleEditorViewModel
import java.util.UUID

private val DarkPurple = Color(0xFF1C1B2E)
private val OffWhite = Color(0xFFF8F8F8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleEditorScreen(
    viewModel: ScheduleEditorViewModel = hiltViewModel(),
    onBackClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = OffWhite,
        topBar = {
            TopAppBar(
                title = { Text("Edit Schedule", fontWeight = FontWeight.Bold, color = DarkPurple) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkPurple)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.onSaveChanges()
                            onBackClicked() // Navigate back after saving
                        }
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = DarkPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OffWhite)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddActivityClicked() },
                shape = CircleShape,
                containerColor = DarkPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity", tint = Color.White)
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = uiState.nickname,
                        onValueChange = { viewModel.onNicknameChange(it) },
                        label = { Text("Schedule Nickname") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                item {
                    Text(
                        "Activities",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                }

                items(uiState.workouts, key = { it.id }) { workout ->
                    EditorWorkoutItem(
                        workout = workout,
                        onDelete = { viewModel.onDeleteActivityClicked(workout.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EditorWorkoutItem(
    workout: Workout,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(workout.title, fontWeight = FontWeight.Bold)
                Text(workout.description, color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Activity", tint = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleEditorScreenPreview() {
    val sampleWorkouts = listOf(
        Workout(id = UUID.randomUUID().toString(), title = "WarmUp", description = "Run 02 km", status = WorkoutStatus.PENDING),
        Workout(id = UUID.randomUUID().toString(), title = "Muscle Up", description = "10 reps, 3 sets", status = WorkoutStatus.PENDING)
    )
    // This is a simplified preview and won't have ViewModel logic
    ScheduleEditorScreen(onBackClicked = {})
}
