package com.example.auratrackr.features.vibe.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import com.example.auratrackr.ui.theme.VibeGymColor
import com.example.auratrackr.ui.theme.VibeHomeColor
import com.example.auratrackr.ui.theme.VibeStudyColor
import com.example.auratrackr.ui.theme.VibeWorkColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibeScreen(
    vibes: List<Vibe>,
    selectedVibeId: String?,
    onVibeSelected: (String) -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent, // Background is handled by MainScreen
        topBar = {
            TopAppBar(
                title = { Text("Select Your Vibe", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(vibes, key = { it.id }) { vibe ->
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
    // The vibe's specific color is used when selected, otherwise fallback to the theme's surface variant.
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) vibe.backgroundColor else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(400),
        label = "VibeCardBackground"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        label = "VibeCardElevation"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        label = "VibeCardBorderWidth"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "VibeCardScale"
    )
    val haptic = LocalHapticFeedback.current

    // Dynamically determine the best content color for readability.
    val contentColor = if (backgroundColor.luminance() > 0.5f) {
        Color.Black
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = true),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                    role = Role.Button
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = vibe.name,
                color = contentColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VibeScreenPreview() {
    val sampleVibes = listOf(
        Vibe("1", "Gym", VibeGymColor.value.toLong(), null),
        Vibe("2", "Study", VibeStudyColor.value.toLong(), null),
        Vibe("3", "Home", VibeHomeColor.value.toLong(), null),
        Vibe("4", "Work", VibeWorkColor.value.toLong(), null)
    )
    AuraTrackrTheme(useDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            VibeScreen(
                vibes = sampleVibes,
                selectedVibeId = "2",
                onVibeSelected = {}
            )
        }
    }
}
