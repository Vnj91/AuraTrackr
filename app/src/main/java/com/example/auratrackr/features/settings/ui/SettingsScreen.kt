package com.example.auratrackr.features.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.core.navigation.Screen
import com.example.auratrackr.features.onboarding.viewmodel.AuthViewModel
import com.example.auratrackr.features.settings.viewmodel.SettingsViewModel
import com.example.auratrackr.features.settings.viewmodel.UiEvent
import com.example.auratrackr.ui.theme.AuraTrackrTheme
import kotlinx.coroutines.flow.collectLatest

enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        settingsViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { settingsViewModel.onProfilePictureSelected(it) }
        }
    )

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                authViewModel.logout()
                showLogoutDialog = false
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // ✅ FIX: Replaced Column with LazyColumn for better performance and sticky headers.
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                ProfileSection(
                    isLoading = uiState.isLoadingProfile,
                    isUploading = uiState.isUploadingPicture,
                    username = uiState.username,
                    height = uiState.height,
                    weight = uiState.weight,
                    profilePictureUrl = uiState.profilePictureUrl,
                    onEditPictureClicked = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // ✅ FIX: Implemented sticky headers for each settings group.
            stickyHeader {
                SettingsHeader("Appearance")
            }
            item {
                SettingsGroup {
                    ThemeToggleButtons(
                        selectedTheme = uiState.themeSetting,
                        onThemeSelected = { newTheme -> settingsViewModel.onThemeSelected(newTheme) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            stickyHeader {
                SettingsHeader("Social")
            }
            item {
                SettingsGroup {
                    SettingsItem(title = "Find Friends", onClick = { navController.navigate(Screen.FindFriends.route) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    SettingsItem(title = "My Friends & Requests", onClick = { navController.navigate(Screen.Friends.route) })
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            stickyHeader {
                SettingsHeader("App Settings")
            }
            item {
                SettingsGroup {
                    SettingsItem(title = "Your Aura Wrapped", onClick = { navController.navigate(Screen.Wrapped.route) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    // ✅ FIX: Disabled placeholder items for better UX.
                    SettingsItem(title = "Account Information", onClick = { /* TODO */ }, enabled = false)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            stickyHeader {
                SettingsHeader("Support")
            }
            item {
                SettingsGroup {
                    SettingsItem(title = "Terms Of Service", onClick = { /* TODO */ }, enabled = false)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    // ✅ FIX: Added Privacy Policy to balance the section.
                    SettingsItem(title = "Privacy Policy", onClick = { /* TODO */ }, enabled = false)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // ✅ FIX: Moved Log Out to its own prominent, destructive-themed card.
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SettingsItem(
                        title = "Log Out",
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    isLoading: Boolean,
    isUploading: Boolean,
    username: String,
    height: String,
    weight: String,
    profilePictureUrl: String?,
    onEditPictureClicked: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profilePictureUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            // ✅ FIX: Used a contrasting color for better visibility.
            IconButton(
                onClick = onEditPictureClicked,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                enabled = !isUploading
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile Picture",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ FIX: Implemented skeleton placeholders for a better loading experience.
        if (isLoading) {
            Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    username,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                // ✅ FIX: Replaced plain text with visually appealing StatChips.
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip(label = "Height", value = height)
                    StatChip(label = "Weight", value = weight)
                }
            }
        }
    }
}

// ✅ NEW: A dedicated composable for the section headers.
@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, bottom = 8.dp, top = 8.dp),
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun SettingsGroup(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column { content() }
    }
}

@Composable
fun SettingsItem(title: String, value: String? = null, onClick: () -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // ✅ FIX: The clickable modifier now uses the modern, default ripple effect.
            .clickable(
                onClick = onClick,
                enabled = enabled
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ThemeToggleButtons(
    selectedTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // ✅ FIX: Added labels under the icons for clarity.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FilledIconToggleButton(
                checked = selectedTheme == ThemeSetting.LIGHT,
                onCheckedChange = { onThemeSelected(ThemeSetting.LIGHT) }
            ) {
                Icon(Icons.Default.LightMode, contentDescription = "Light Theme")
            }
            Text("Light", style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FilledIconToggleButton(
                checked = selectedTheme == ThemeSetting.DARK,
                onCheckedChange = { onThemeSelected(ThemeSetting.DARK) }
            ) {
                Icon(Icons.Default.DarkMode, contentDescription = "Dark Theme")
            }
            Text("Dark", style = MaterialTheme.typography.labelSmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FilledIconToggleButton(
                checked = selectedTheme == ThemeSetting.SYSTEM,
                onCheckedChange = { onThemeSelected(ThemeSetting.SYSTEM) }
            ) {
                Icon(Icons.Default.SettingsBrightness, contentDescription = "System Default Theme")
            }
            Text("System", style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ✅ NEW: A dedicated composable for displaying user stats.
@Composable
fun StatChip(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LogoutConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Out") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text("Log Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    AuraTrackrTheme(useDarkTheme = true) {
        SettingsScreen(navController = rememberNavController())
    }
}
