package com.example.auratrackr.features.vibe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auratrackr.domain.model.Vibe

private val DarkPurple = Color(0xFF1C1B2E)
private val CardPurple = Color(0xFF2C2B3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeScreen(
    // These will be provided by a ViewModel later
    vibes: List<Vibe>,
    selectedVibeId: String?,
    onVibeSelected: (String) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent, // The background will be handled by MainScreen
        topBar = {
            TopAppBar(
                title = { Text("Select Your Vibe", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Choose a vibe to personalize your dashboard and schedule.",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(vibes) { vibe ->
                    VibeCard(
                        vibe = vibe,
                        isSelected = vibe.id == selectedVibeId,
                        onClick = { onVibeSelected(vibe.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VibeCard(
    vibe: Vibe,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // Makes the card a square
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) vibe.backgroundColor else CardPurple
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = vibe.name,
                color = if (isSelected) Color.Black else Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B2E)
@Composable
fun VibeScreenPreview() {
    val sampleVibes = listOf(
        Vibe("1", "Gym", Color(0xFFD4B42A)),
        Vibe("2", "Study", Color(0xFFD9F1F2)),
        Vibe("3", "Home", Color(0xFFF7F6CF)),
        Vibe("4", "Work", Color(0xFFFFD6F5))
    )
    VibeScreen(
        vibes = sampleVibes,
        selectedVibeId = "2",
        onVibeSelected = {}
    )
}
