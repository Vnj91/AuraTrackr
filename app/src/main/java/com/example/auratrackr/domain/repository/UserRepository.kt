package com.example.auratrackr.domain.repository

import android.net.Uri
import com.example.auratrackr.domain.model.FriendRequest
import com.example.auratrackr.domain.model.PointsHistory
import com.example.auratrackr.domain.model.User
import com.example.auratrackr.domain.model.UserSummary
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for handling all user-related data operations,
 * abstracting the data sources (Firestore, Firebase Storage) from the application's business logic.
 */
interface UserRepository {

    // --- Profile Management ---
    fun getUserProfile(uid: String): Flow<User?>
    suspend fun createUserProfile(user: User): Result<Unit>
    suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit>
    suspend fun uploadProfilePicture(uid: String, imageUri: Uri): Result<String>

    // --- Gamification ---

    /**
     * Atomically increments a user's Aura Points and records the transaction.
     * @param uid The unique ID of the user.
     * @param pointsToAdd The number of points to add.
     * @param vibeId The ID of the Vibe active when the points were earned.
     * @return A [Result] indicating success or failure.
     */
    suspend fun addAuraPoints(uid: String, pointsToAdd: Int, vibeId: String): Result<Unit>

    suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit>

    /**
     * Retrieves the history of points earned for a user.
     * @param uid The unique ID of the user.
     * @return A [Flow] that emits a list of [PointsHistory] objects.
     */
    fun getPointsHistory(uid: String): Flow<List<PointsHistory>>

    // --- Friends System ---
    suspend fun searchUsersByUsername(query: String, currentUserId: String): Result<List<User>>
    suspend fun sendFriendRequest(sender: User, receiverId: String): Result<Unit>
    fun getFriendRequests(uid: String): Flow<List<FriendRequest>>
    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit>
    suspend fun declineFriendRequest(request: FriendRequest): Result<Unit>
    suspend fun removeFriend(currentUserId: String, friendId: String): Result<Unit>
    fun getFriends(uid: String): Flow<List<User>>

    // --- Aura Wrapped Summary ---
    fun getUserSummary(uid: String, year: String): Flow<UserSummary?>
}

