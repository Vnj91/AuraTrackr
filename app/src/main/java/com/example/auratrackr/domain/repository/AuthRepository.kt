package com.example.auratrackr.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun currentUserId(): String?
    fun observeAuthState(): Flow<String?>
}
