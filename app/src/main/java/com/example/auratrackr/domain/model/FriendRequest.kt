package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Represents the status of a [FriendRequest].
 * Firestore serializes enums to their string names by default (e.g., "PENDING").
 */
enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

/**
 * Represents a friend request sent from one user to another. This data class is designed
 * for use with Firestore, with default values provided for all properties to ensure
 * seamless deserialization.
 *
 * This object would typically be stored in a "friend_requests" sub-collection
 * under the receiving user's document.
 *
 * @property id The unique ID of the friend request document, automatically populated by Firestore.
 * @property senderId The UID of the user who sent the request.
 * @property senderUsername The username of the user who sent the request, denormalized for easy display.
 * @property senderProfileImageUrl Optional URL for the sender's profile image.
 * @property receiverId The UID of the user who is intended to receive the request.
 * @property status The current status of the request, managed by the [RequestStatus] enum.
 * @property timestamp The server-generated timestamp of when the request was created.
 */
data class FriendRequest(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val senderProfileImageUrl: String? = null,
    val receiverId: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    @ServerTimestamp val timestamp: Date? = null
)