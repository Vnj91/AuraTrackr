package com.example.auratrackr.di

import com.example.auratrackr.data.auth.FirebaseAuthRepository
import com.example.auratrackr.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    // FirebaseAuth is provided by `com.example.auratrackr.core.di.FirebaseModule`.
}
