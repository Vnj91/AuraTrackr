package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Challenge
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for managing Group Challenge data.
 *
 * This repository abstracts the underlying data source (e.g., Firestore) and provides
 * a clean API for all challenge-related operations.
 */
interface ChallengeRepository {

    /**
     * Creates a new group challenge in the database.
     *
     * @param challenge The [Challenge] object to be created.
     * @return A [Result] object indicating success or failure of the operation.
     */
    suspend fun createChallenge(challenge: Challenge): Result<Unit>

    /**
     * Fetches a single challenge by its unique ID, providing real-time updates.
     *
     * @param challengeId The unique ID of the challenge to retrieve.
     * @return A [Flow] that emits the [Challenge] object when found, or `null` if it doesn't exist.
     */
    fun getChallenge(challengeId: String): Flow<Challenge?>

    /**
     * Fetches all challenges that a specific user is a participant in.
     *
     * @param uid The unique ID of the user.
     * @return A [Flow] that emits a list of [Challenge] objects, updating in real-time.
     */
    fun getUserChallenges(uid: String): Flow<List<Challenge>>

    /**
     * Adds a user to an existing challenge's participant list. This operation
     * should be idempotent (running it multiple times has the same effect as running it once).
     *
     * @param challengeId The ID of the challenge to join.
     * @param uid The ID of the user to add.
     * @return A [Result] object indicating success or failure.
     */
    suspend fun joinChallenge(challengeId: String, uid: String): Result<Unit>

    /**
     * Removes a user from an existing challenge's participant list. This operation
     * should be idempotent.
     *
     * @param challengeId The ID of the challenge to leave.
     * @param uid The ID of the user to remove.
     * @return A [Result] object indicating success or failure.
     */
    suspend fun leaveChallenge(challengeId: String, uid: String): Result<Unit>

    /**
     * Atomically increments the progress of a specific challenge.
     *
     * @param challengeId The ID of the challenge to update.
     * @param progressToAdd The amount of progress to add to the current total.
     * @return A [Result] object indicating success or failure.
     */
    suspend fun updateChallengeProgress(challengeId: String, progressToAdd: Long): Result<Unit>
}
