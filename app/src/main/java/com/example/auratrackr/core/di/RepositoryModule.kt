package com.example.auratrackr.core.di

import com.example.auratrackr.data.repository.*
import com.example.auratrackr.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations.
 *
 * This abstract module uses Dagger's @Binds annotation to efficiently provide
 * concrete implementations for the domain layer's repository interfaces. Each binding
 * is scoped as a @Singleton to ensure a single, shared instance throughout the app's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the [UserRepository] interface to its concrete implementation, [UserRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    /**
     * Binds the [AppUsageRepository] interface to its concrete implementation, [AppUsageRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        appUsageRepositoryImpl: AppUsageRepositoryImpl
    ): AppUsageRepository

    /**
     * Binds the [WorkoutRepository] interface to its concrete implementation, [WorkoutRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    /**
     * Binds the [VibeRepository] interface to its concrete implementation, [VibeRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindVibeRepository(
        vibeRepositoryImpl: VibeRepositoryImpl
    ): VibeRepository

    /**
     * Binds the [ChallengeRepository] interface to its concrete implementation, [ChallengeRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindChallengeRepository(
        challengeRepositoryImpl: ChallengeRepositoryImpl
    ): ChallengeRepository

    /**
     * ✅ ADDED: Binds the [ThemeRepository] interface to its concrete implementation, [ThemeRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository
}
