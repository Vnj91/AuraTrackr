package com.example.auratrackr.core.di

import com.example.auratrackr.data.repository.AppUsageRepositoryImpl
import com.example.auratrackr.data.repository.UserRepositoryImpl
import com.example.auratrackr.data.repository.VibeRepositoryImpl
import com.example.auratrackr.data.repository.WorkoutRepositoryImpl
import com.example.auratrackr.domain.repository.AppUsageRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.VibeRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
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

    /**
     * Binds the VibeRepository interface to its implementation, VibeRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindVibeRepository(
        vibeRepositoryImpl: VibeRepositoryImpl
    ): VibeRepository // <-- ADDED THIS BINDING

}
