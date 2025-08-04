package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for handling all user-related data operations.
 */
interface UserRepository {

    fun getUserProfile(uid: String): Flow<User?>

    suspend fun createUserProfile(user: User): Result<Unit>

    suspend fun completeOnboarding(uid: String, weightInKg: Int, heightInCm: Int): Result<Unit>

    suspend fun addAuraPoints(uid: String, pointsToAdd: Int): Result<Unit>

    /**
     * Atomically subtracts a specified number of Aura Points from a user's profile.
     * This function should check if the user has enough points before spending.
     *
     * @param uid The unique ID of the user.
     * @param pointsToSpend The number of points to spend.
     * @return A Result object indicating success or failure.
     */
    suspend fun spendAuraPoints(uid: String, pointsToSpend: Int): Result<Unit> // <-- ADDED THIS LINE

}
