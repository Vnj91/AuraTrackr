package com.example.auratrackr.domain.repository

import com.example.auratrackr.domain.model.Challenge
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for managing Group Challenge data in Firestore.
 */
interface ChallengeRepository {

    /**
     * Creates a new group challenge in the database.
     *
     * @param challenge The Challenge object to be created.
     * @return A Result object indicating success or failure.
     */
    suspend fun createChallenge(challenge: Challenge): Result<Unit>

    /**
     * Fetches all challenges that a specific user is a participant in.
     *
     * @param uid The unique ID of the user.
     * @return A Flow that emits a list of Challenge objects.
     */
    fun getUserChallenges(uid: String): Flow<List<Challenge>>

    /**
     * Adds a user to an existing challenge.
     * (This would be used for accepting invites in the future).
     *
     * @param challengeId The ID of the challenge to join.
     * @param uid The ID of the user to add.
     * @return A Result object indicating success or failure.
     */
    suspend fun joinChallenge(challengeId: String, uid: String): Result<Unit>

    /**
     * Updates the progress of a specific challenge.
     * This should be an atomic operation.
     *
     * @param challengeId The ID of the challenge to update.
     * @param progressToAdd The amount of progress to add to the current total.
     * @return A Result object indicating success or failure.
     */
    suspend fun updateChallengeProgress(challengeId: String, progressToAdd: Long): Result<Unit>
}
