package com.example.auratrackr.domain.repository

import android.net.Uri
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.model.UserSummary
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for handling all user-related data operations,
 * abstracting the data sources (Firestore, Firebase Storage) from the application's business logic.
 */
interface UserRepository {

    // --- Profile Management ---

    /**
     * Retrieves a user's profile in real-time.
     * @param uid The unique ID of the user to fetch.
     * @return A [Flow] that emits the [User] object, or `null` if the user doesn't exist.
     */
    fun getUserProfile(uid: String): Flow<User?>

    /**
     * Creates a new user profile document in the database.
     * @param user The [User] object to be created.
     * @return A [Result] indicating success or failure.
     */
    suspend fun createUserProfile(user: User): Result<Unit>

    /**
     * Updates a user's profile to mark onboarding as complete and set initial stats.
     * @param uid The unique ID of the user.
     * @param weightInKg The user's weight.
     * @param heightInCm The user's height.
     * @return A [Result] indicating success or failure.
     */
    suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit>

    /**
     * Uploads a profile picture to Firebase Storage and updates the user's profilePictureUrl in Firestore.
     * @param uid The unique ID of the user.
     * @param imageUri The local URI of the image file to upload.
     * @return A [Result] containing the public download URL of the uploaded image upon success.
     */
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String>

    // --- Gamification ---

    /**
     * Atomically increments a user's Aura Points.
     * @param uid The unique ID of the user.
     * @param pointsToAdd The number of points to add.
     * @return A [Result] indicating success or failure.
     */
    suspend fun addAuraPoints(uid: String, pointsToAdd: Int): Result<Unit>

    /**
     * Atomically decrements a user's Aura Points. Fails if the user has insufficient points.
     * @param uid The unique ID of the user.
     * @param pointsToSpend The number of points to spend.
     * @return A [Result] indicating success or failure.
     */
    suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit>

    // --- Friends System ---

    /**
     * Searches for users by username, excluding the current user.
     * @param query The search query for the username.
     * @param currentUserId The UID of the user performing the search, to exclude them from results.
     * @return A [Result] containing a list of matching [User]s.
     */
    suspend fun searchUsersByUsername(query: String, currentUserId: String): Result<List<User>>

    /**
     * Sends a friend request from a sender to a receiver.
     * @param sender The [User] object of the person sending the request.
     * @param receiverId The unique ID of the user receiving the request.
     * @return A [Result] indicating success or failure.
     */
    suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit>

    /**
     * Retrieves all incoming friend requests for a user in real-time.
     * @param uid The unique ID of the user whose requests are to be fetched.
     * @return A [Flow] that emits a list of [FriendRequest]s.
     */
    fun getFriendRequests(uid: String): Flow<List<FriendRequest>>

    /**
     * Accepts a friend request, adding each user to the other's friends list and deleting the request.
     * This operation should be transactional.
     * @param request The [FriendRequest] to be accepted.
     * @return A [Result] indicating success or failure.
     */
    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit>

    /**
     * Declines a friend request by deleting the request document.
     * @param request The [FriendRequest] to be declined.
     * @return A [Result] indicating success or failure.
     */
    suspend fun declineFriendRequest(request: FriendRequest): Result<Unit>

    /**
     * Removes a friend connection between two users. This is a mutual action.
     * @param currentUserId The ID of the user initiating the removal.
     * @param friendId The ID of the friend to be removed.
     * @return A [Result] indicating success or failure.
     */
    suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit>

    /**
     * Retrieves the full user profiles of a user's friends in real-time.
     * @param uid The unique ID of the user whose friends are to be fetched.
     * @return A [Flow] that emits a list of [User] objects representing the friends.
     */
    fun getFriends(uid: String): Flow<List<User>>

    // --- Aura Wrapped Summary ---

    /**
     * Retrieves a pre-calculated summary of a user's activity for a specific year.
     * @param uid The unique ID of the user.
     * @param year The year of the summary to fetch (e.g., "2025").
     * @return A [Flow] that emits the [UserSummary] object, or `null` if it doesn't exist.
     */
    fun getUserSummary(uid: String, year: String): Flow<UserSummary?>
}