package com.example.auratrackr.features.challenges.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auratrackr.R
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.ui.theme.Dimensions

private val LOCAL_CHALLENGE_CARD_CORNER = 16.dp
private val LOCAL_CHALLENGE_ITEM_SPACER_SMALL = Dimensions.Small
private val LOCAL_CHALLENGE_BUTTON_HEIGHT = 56.dp

@Composable
fun FriendInviteItem(
    friend: com.example.auratrackr.domain.model.User,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LOCAL_CHALLENGE_ITEM_SPACER_SMALL)
            .clip(RoundedCornerShape(LOCAL_CHALLENGE_CARD_CORNER)),
        shape = RoundedCornerShape(LOCAL_CHALLENGE_CARD_CORNER),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = LOCAL_CHALLENGE_ITEM_SPACER_SMALL,
                vertical = LOCAL_CHALLENGE_ITEM_SPACER_SMALL
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LOCAL_CHALLENGE_ITEM_SPACER_SMALL)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(friend.profilePictureUrl)
                    .crossfade(true)
                    .error(R.drawable.ic_person_placeholder)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .build(),
                contentDescription = "${friend.username}'s Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(LOCAL_CHALLENGE_BUTTON_HEIGHT)
                    .clip(CircleShape)
            )
            Text(
                friend.username ?: "Unknown User",
                modifier = Modifier.weight(1f),
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
            Checkbox(checked = isSelected, onCheckedChange = { onSelectionChanged(it) })
        }
    }
}
