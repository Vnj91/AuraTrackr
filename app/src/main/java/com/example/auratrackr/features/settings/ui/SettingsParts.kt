package com.example.auratrackr.features.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.ui.theme.Dimensions

/**
 * Configuration data class for ProfileSection to reduce parameter count.
 */
data class ProfileConfig(
    val isLoading: Boolean,
    val isUploading: Boolean,
    val username: String,
    val height: String,
    val weight: String,
    val profilePictureUrl: String?,
    val onEditPictureClicked: () -> Unit
)

/**
 * Profile avatar with edit button and upload indicator.
 */
@Composable
fun ProfileAvatar(
    profilePictureUrl: String?,
    isUploading: Boolean,
    onEditClicked: () -> Unit
) {
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
        IconButton(
            onClick = onEditClicked,
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
                modifier = Modifier.padding(Dimensions.Small),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Profile section displaying user avatar, name, and stats.
 */
@Composable
fun ProfileSection(config: ProfileConfig) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileAvatar(
            profilePictureUrl = config.profilePictureUrl,
            isUploading = config.isUploading,
            onEditClicked = config.onEditPictureClicked
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (config.isLoading) {
            Box(modifier = Modifier.height(60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    config.username,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(Dimensions.Small))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatChip(label = "Height", value = config.height)
                    StatChip(label = "Weight", value = config.weight)
                }
            }
        }
    }
}

/**
 * Section header for settings groups.
 */
@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = Dimensions.Large, bottom = Dimensions.Small, top = Dimensions.Small),
        style = MaterialTheme.typography.titleMedium
    )
}

/**
 * Card container for grouping related settings items.
 */
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

/**
 * Individual settings item with title, optional value, and click action.
 */
@Composable
fun SettingsItem(
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                enabled = enabled
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                )
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.width(Dimensions.Small))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Theme toggle buttons for light, dark, and system themes.
 */
@Composable
fun ThemeToggleButtons(
    selectedTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Small),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
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

/**
 * Chip displaying user statistics like height and weight.
 */
@Composable
fun StatChip(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Dimensions.Large, vertical = Dimensions.Small),
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

/**
 * Confirmation dialog for logout action.
 */
@Composable
fun LogoutConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Out") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
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
