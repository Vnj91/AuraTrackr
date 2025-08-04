package com.example.auratrackr.core.di

import com.example.auratrackr.data.repository.AppUsageRepositoryImpl
import com.example.auratrackr.data.repository.UserRepositoryImpl
import com.example.auratrackr.data.repository.WorkoutRepositoryImpl
import com.example.auratrackr.domain.repository.AppUsageRepository
import com.example.auratrackr.domain.repository.UserRepository
import com.example.auratrackr.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository implementations.
 * This abstract class uses the @Binds annotation, which is the recommended
 * and more efficient way to tell Hilt how to provide an implementation for an interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the UserRepository interface to its implementation, UserRepositoryImpl.
     * When a component requests a UserRepository, Hilt will provide an instance of UserRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    /**
     * Binds the AppUsageRepository interface to its implementation, AppUsageRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindAppUsageRepository(
        appUsageRepositoryImpl: AppUsageRepositoryImpl
    ): AppUsageRepository

    /**
     * Binds the WorkoutRepository interface to its implementation, WorkoutRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository
}
