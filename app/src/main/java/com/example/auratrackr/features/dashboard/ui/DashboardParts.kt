package com.example.auratrackr.features.dashboard.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.domain.model.Vibe
import com.example.auratrackr.domain.model.Workout
import com.example.auratrackr.domain.model.WorkoutStatus
import com.example.auratrackr.ui.components.PremiumCard
import com.example.auratrackr.ui.components.PremiumButton
import com.example.auratrackr.ui.theme.Dimensions
import com.example.auratrackr.ui.theme.SuccessGreen
import com.example.auratrackr.ui.theme.PremiumAnimations
import com.example.auratrackr.ui.theme.pressAnimation

private val POINTS_CHART_HEIGHT_DP = 150.dp
private val POINTS_BAR_RECT_HEIGHT_DP = 100.dp
private val POINTS_BAR_RECT_WIDTH_DP = 20.dp
private val POINTS_BAR_CORNER_DP = Dimensions.Small
private val SMALL_SPACER = Dimensions.Small
private val ICON_NO_SCHEDULE_SIZE = 64.dp
private val ACTION_BUTTON_HORIZONTAL_PADDING = 24.dp
private val ACTION_BUTTON_VERTICAL_PADDING = 12.dp
private val CHIP_ICON_SIZE = 20.dp

// Named constants for timeline / drawing to avoid magic numbers flagged by detekt
private val TIMELINE_CANVAS_WIDTH_DP = 30.dp
private val TIMELINE_CIRCLE_RADIUS_DP = 4.dp
private val TIMELINE_STROKE_WIDTH_DP = 2.dp
private const val TIMELINE_DASH_LENGTH = 10f
private const val TIMELINE_DASH_GAP = 10f
private val TIMELINE_DASH_PATTERN = floatArrayOf(TIMELINE_DASH_LENGTH, TIMELINE_DASH_GAP)

private val EMPTY_PLACEHOLDER_PADDING_DP = 24.dp
private val BUTTON_ICON_SPACING_DP = 8.dp
private const val POINTS_BAR_ANIM_MS = 1000

@Composable
fun AvatarWithInitials(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            val request = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .error(R.drawable.ic_person_placeholder)
                .placeholder(R.drawable.ic_person_placeholder)
                .build()
            AsyncImage(
                model = request,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AuraPointsChip(points: Int) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .pressAnimation(),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Aura Points",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$points",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun PointsByVibeCard(
    isLoading: Boolean,
    pointsByVibe: Map<Vibe, Int>,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 6.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp)
    ) {
        Column {
            Text(
                "Aura Points by Vibe",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Box(
                    modifier = Modifier.height(POINTS_CHART_HEIGHT_DP).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PointsByVibeBarChart(pointsByVibe = pointsByVibe)
            }
        }
    }
}

@Composable
fun PointsByVibeBarChart(pointsByVibe: Map<Vibe, Int>) {
    val maxPoints = remember(pointsByVibe) { (pointsByVibe.values.maxOrNull() ?: 1).coerceAtLeast(1) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(POINTS_CHART_HEIGHT_DP),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        pointsByVibe.entries.forEach { (vibe, points) ->
            val barHeightFraction = (points.toFloat() / maxPoints.toFloat()).coerceIn(0f, 1f)
            val animatedBarHeightFraction by animateFloatAsState(
                targetValue = barHeightFraction,
                animationSpec = PremiumAnimations.smoothSpring,
                label = "BarHeightAnimation"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                if (points > 0) {
                    Text(
                        text = "$points pts",
                        style = MaterialTheme.typography.labelSmall,
                        color = vibe.backgroundColor
                    )
                }
                Canvas(modifier = Modifier.height(POINTS_BAR_RECT_HEIGHT_DP).width(POINTS_BAR_RECT_WIDTH_DP)) {
                    drawRoundRect(
                        color = vibe.backgroundColor,
                        topLeft = Offset(x = 0f, y = size.height * (1 - animatedBarHeightFraction)),
                        size = Size(width = size.width, height = size.height * animatedBarHeightFraction),
                        cornerRadius = CornerRadius(x = POINTS_BAR_CORNER_DP.toPx(), y = POINTS_BAR_CORNER_DP.toPx())
                    )
                }
                Spacer(modifier = Modifier.height(SMALL_SPACER))
                Text(
                    text = vibe.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun DrawScope.drawTimelineLine(color: Color, start: Offset, end: Offset, isDashed: Boolean) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = TIMELINE_STROKE_WIDTH_DP.toPx(),
        pathEffect = if (isDashed) androidx.compose.ui.graphics.PathEffect.dashPathEffect(TIMELINE_DASH_PATTERN, 0f) else null
    )
}

@Composable
fun EmptySchedulePlaceholder(onAddScheduleClicked: () -> Unit) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.ic_no_schedule),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )
            Text(
                "No schedule for today",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Relax or add new activities!",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
            PremiumButton(
                onClick = onAddScheduleClicked,
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = 6.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 32.dp,
                    vertical = 16.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(BUTTON_ICON_SPACING_DP))
                Text("Add Schedule", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ScheduleTimeline(
    schedule: com.example.auratrackr.domain.model.Schedule,
    onStartWorkout: (Workout) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Small)
    ) {
        schedule.workouts.forEachIndexed { index, workout ->
            WorkoutItem(
                workout = workout,
                isFirstItem = index == 0,
                isLastItem = index == schedule.workouts.lastIndex,
                onStartClicked = { onStartWorkout(workout) }
            )
        }
    }
}

@Composable
fun WorkoutItem(workout: Workout, isFirstItem: Boolean, isLastItem: Boolean, onStartClicked: () -> Unit) {
    val activeColor = MaterialTheme.colorScheme.primary
    val completedColor = SuccessGreen
    val pendingColor = MaterialTheme.colorScheme.outline

    val timelineColor = when (workout.status) {
        WorkoutStatus.ACTIVE -> activeColor
        WorkoutStatus.COMPLETED -> completedColor
        WorkoutStatus.PENDING -> pendingColor
    }

    Row(
        modifier = Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WorkoutTimelineDot(status = workout.status, isFirstItem = isFirstItem, isLastItem = isLastItem)
        Spacer(modifier = Modifier.width(16.dp))
        WorkoutDetails(workout = workout, timelineColor = timelineColor)
        Spacer(modifier = Modifier.width(16.dp))
        if (workout.status == WorkoutStatus.COMPLETED) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = completedColor,
                modifier = Modifier.size(32.dp)
            )
        } else {
            PremiumButton(
                onClick = onStartClicked,
                backgroundColor = if (workout.status == WorkoutStatus.ACTIVE) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (workout.status == WorkoutStatus.ACTIVE)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer,
                elevation = if (workout.status == WorkoutStatus.ACTIVE) 6.dp else 2.dp,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 24.dp, 
                    vertical = 12.dp
                )
            ) {
                Text(
                    text = if (workout.status == WorkoutStatus.ACTIVE) "Resume" else "Start",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun WorkoutTimelineDot(
    status: WorkoutStatus,
    isFirstItem: Boolean,
    isLastItem: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val completedColor = SuccessGreen
    val pendingColor = MaterialTheme.colorScheme.outline
    val timelineColor = when (status) {
        WorkoutStatus.ACTIVE -> activeColor
        WorkoutStatus.COMPLETED -> completedColor
        WorkoutStatus.PENDING -> pendingColor
    }
    val isDashed = status == WorkoutStatus.PENDING

    Canvas(modifier = modifier.width(TIMELINE_CANVAS_WIDTH_DP).fillMaxHeight()) {
        val circleRadius = TIMELINE_CIRCLE_RADIUS_DP.toPx()
        val circleCenterY = size.height / 2
        if (!isFirstItem) {
            drawTimelineLine(
                timelineColor,
                Offset(center.x, 0f),
                Offset(center.x, circleCenterY - circleRadius),
                isDashed
            )
        }
        if (!isLastItem) {
            drawTimelineLine(
                timelineColor,
                Offset(center.x, circleCenterY + circleRadius),
                Offset(center.x, size.height),
                isDashed
            )
        }
        when (status) {
            WorkoutStatus.COMPLETED -> drawCircle(color = completedColor, radius = circleRadius)
            WorkoutStatus.ACTIVE -> drawCircle(color = activeColor, radius = circleRadius)
            WorkoutStatus.PENDING -> drawCircle(
                color = pendingColor,
                radius = circleRadius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = TIMELINE_STROKE_WIDTH_DP.toPx())
            )
        }
    }
}

@Composable
fun RowScope.WorkoutDetails(workout: Workout, timelineColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier.weight(1f).padding(vertical = 12.dp)) {
        Text(
            workout.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            workout.description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(SMALL_SPACER))
        WorkoutProgressBar(
            progress = if (workout.status == WorkoutStatus.COMPLETED) 1f else 0f,
            color = timelineColor
        )
    }
}
// Note: WorkoutProgressBar extracted to a dedicated file to reduce function count for detekt.
