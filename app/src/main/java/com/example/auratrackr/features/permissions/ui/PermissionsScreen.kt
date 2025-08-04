package com.example.auratrackr.features.permissions.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private val DarkPurple = Color(0xFF1C1B2E)
private val LightGray = Color(0xFFF0F0F0)
private val AccentGreen = Color(0xFF4CAF50)

@Composable
fun PermissionsScreen(
    usageAccessGranted: Boolean,
    accessibilityGranted: Boolean,
    onGrantUsageAccess: () -> Unit,
    onGrantAccessibility: () -> Unit,
    onContinue: () -> Unit,
    onRefresh: () -> Unit // <-- THIS PARAMETER IS NOW CORRECTLY DEFINED
) {
    // This effect observes the lifecycle and calls onRefresh when the app resumes.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = LightGray,
        bottomBar = {
            Button(
                onClick = onContinue,
                enabled = usageAccessGranted && accessibilityGranted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkPurple,
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Continue", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "One Last Step!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkPurple,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "AuraTrackr needs these permissions to monitor app usage and help you focus.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Usage Access Permission Card
            PermissionCard(
                icon = Icons.Default.QueryStats,
                title = "Usage Access",
                description = "Allows the app to see which apps you are using and for how long. This is essential for tracking your screen time.",
                isGranted = usageAccessGranted,
                onGrantClicked = onGrantUsageAccess
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Accessibility Service Permission Card
            PermissionCard(
                icon = Icons.Default.Lock,
                title = "Accessibility Service",
                description = "Allows the app to identify the current app being used and overlay a blocking screen when your time limit is reached.",
                isGranted = accessibilityGranted,
                onGrantClicked = onGrantAccessibility
            )
        }
    }
}

@Composable
fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onGrantClicked: () -> Unit
) {
    val backgroundColor by animateColorAsState(if (isGranted) AccentGreen.copy(alpha = 0.1f) else Color.White)
    val borderColor by animateColorAsState(if (isGranted) AccentGreen else Color.LightGray)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkPurple.copy(alpha = 0.1f))
                    .padding(10.dp),
                tint = DarkPurple
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkPurple)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Granted",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                        .padding(4.dp),
                    tint = Color.White
                )
            } else {
                Button(
                    onClick = onGrantClicked,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPurple)
                ) {
                    Text("Grant")
                }
            }
        }
    }
}
