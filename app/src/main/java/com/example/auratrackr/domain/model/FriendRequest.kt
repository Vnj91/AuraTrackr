package com.example.auratrackr.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

/**
 * Represents a friend request sent from one user to another.
 * This will be stored in a "friend_requests" sub-collection under the receiving user's document.
 *
 * @property id The unique ID of the friend request document.
 * @property senderId The UID of the user who sent the request.
 * @property senderUsername The username of the user who sent the request.
 * @property receiverId The UID of the user who received the request.
 * @property status The current status of the request (PENDING, ACCEPTED, or DECLINED).
 * @property timestamp The server timestamp of when the request was created.
 */
data class FriendRequest(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val receiverId: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    @ServerTimestamp val timestamp: Date? = null
) {
    // Firestore requires a no-argument constructor
    constructor() : this("", "", "", "", RequestStatus.PENDING, null)
}
