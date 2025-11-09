package com.example.auratrackr.data.repository

import com.example.auratrackr.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber

// Local constants used by repository helpers. Kept private to this file to avoid
// cross-file visibility and to keep the original repo file focused.
private const val USERS_COLLECTION = "users"
private const val FIELD_UID = "uid"

// Retry defaults for transient network operations.
private const val RETRY_DEFAULT_TIMES = 3
private const val RETRY_DEFAULT_INITIAL_DELAY_MS = 500L
private const val RETRY_DEFAULT_MAX_DELAY_MS = 2000L
private const val RETRY_DEFAULT_FACTOR = 2.0

/**
 * Retry helper intended for transient network calls.
 * The suppression is intentional: the helper retries on any exception to
 * increase resilience for idempotent network operations (uploads, url fetches).
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T> retryWithDelay(
    times: Int = RETRY_DEFAULT_TIMES,
    initialDelay: Long = RETRY_DEFAULT_INITIAL_DELAY_MS,
    maxDelay: Long = RETRY_DEFAULT_MAX_DELAY_MS,
    factor: Double = RETRY_DEFAULT_FACTOR,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            Timber.w(e, "Operation failed. Retrying in $currentDelay ms.")
        }
        kotlinx.coroutines.delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block()
}

/**
 * Query helper to fetch users for a list of ids in a single whereIn call.
 * Keeps the chunking logic concise when used from repositories.
 */
suspend fun fetchUsersByIds(firestore: FirebaseFirestore, ids: List<String>): List<User> {
    if (ids.isEmpty()) return emptyList()
    return try {
        val snapshot = firestore.collection(USERS_COLLECTION)
            .whereIn(FIELD_UID, ids)
            .get()
            .await()
        snapshot.toObjects(User::class.java)
    } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
        Timber.e(e, "fetchUsersByIds firestore error for ids=${ids.joinToString(",")}")
        emptyList()
    } catch (e: com.google.firebase.FirebaseException) {
        Timber.e(e, "fetchUsersByIds firebase error for ids=${ids.joinToString(",")}")
        emptyList()
    }
}
