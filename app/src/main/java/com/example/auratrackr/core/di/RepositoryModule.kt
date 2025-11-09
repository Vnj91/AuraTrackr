package com.example.auratrackr.core.di

import com.example.auratrackr.data.repository.AppUsageRepositoryImpl
import com.example.auratrackr.data.repository.ChallengeRepositoryImpl
import com.example.auratrackr.data.repository.ThemeRepositoryImpl
import com.example.auratrackr.data.repository.UserRepositoryImpl
import com.example.auratrackr.data.repository.VibeRepositoryImpl
import com.example.auratrackr.data.repository.WorkoutRepositoryImpl
import com.example.auratrackr.domain.repository.AppUsageRepository
import com.example.auratrackr.domain.repository.ChallengeRepository
import com.example.auratrackr.domain.repository.ThemeRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides repository implementations.
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
     * Binds the [ThemeRepository] interface to its concrete implementation, [ThemeRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        themeRepositoryImpl: ThemeRepositoryImpl
    ): ThemeRepository

    /**
     * ✅ REMOVED: The binding for HealthConnectRepository is now gone,
     * completing the removal of the feature from the project.
     */
    /*
    @Binds
    @Singleton
    abstract fun bindHealthConnectRepository(
        healthConnectRepositoryImpl: HealthConnectRepositoryImpl
    ): HealthConnectRepository
    */
}
