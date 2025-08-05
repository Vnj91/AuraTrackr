package com.example.auratrackr.core.di

import com.example.auratrackr.data.repository.*
import com.example.auratrackr.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        appUsageRepositoryImpl: AppUsageRepositoryImpl
    ): AppUsageRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindVibeRepository(
        vibeRepositoryImpl: VibeRepositoryImpl
    ): VibeRepository

    /**
     * Binds the ChallengeRepository interface to its implementation, ChallengeRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository // <-- ADDED THIS BINDING

}
