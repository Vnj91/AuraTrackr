package com.example.auratrackr.core.util

import timber.log.Timber

/**
 * Helper to wrap suspend operations in a Result while logging failures to Timber.
 * Keeps calling sites concise and behavior-preserving.
 */
@Suppress("TooGenericExceptionCaught")
suspend fun <T> safeResult(tag: String, opName: String, block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Timber.tag(tag).e(e, "$opName failed")
        Result.failure(e)
    }
}
