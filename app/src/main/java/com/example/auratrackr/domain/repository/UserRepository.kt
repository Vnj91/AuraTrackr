package com.example.auratrackr.domain.repository

import android.net.Uri
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.model.UserSummary
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for handling all user-related data operations.
 */
interface UserRepository {

    // --- Profile Management ---
    fun getUserProfile(uid: String): Flow<User?>
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit>
    /**
     * Uploads a profile picture to Firebase Storage and updates the user's profilePictureUrl in Firestore.
     *
     * @param uid The unique ID of the user.
     * @param imageUri The local URI of the image file to upload.
     * @return A Result containing the public download URL of the uploaded image.
     */
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String> // <-- ADDED THIS LINE

    // --- Gamification ---
    suspend fun addAuraPoints(uid: String, pointsToAdd: Int): Result<Unit>
    suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit>

    // --- Friends System ---
    suspend fun searchUsersByUsername(query: String): Result<List<User>>
    suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit>
    fun getFriendRequests(uid: String): Flow<List<FriendRequest>>
    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit>
    suspend fun declineFriendRequest(request: FriendRequest): Result<Unit>
    fun getFriends(uid: String): Flow<List<User>>

    // --- Aura Wrapped Summary ---
    fun getUserSummary(uid: String, year: String): Flow<UserSummary?>

}
