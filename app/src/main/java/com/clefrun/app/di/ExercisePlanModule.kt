package com.clefrun.app.di

import com.clefrun.app.BuildConfig
import com.clefrun.app.data.exerciseplan.remote.RemoteExercisePlanApi
import com.clefrun.app.data.exerciseplan.remote.RemoteExercisePlanConfig
import com.clefrun.app.data.exerciseplan.remote.RemoteExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.ExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.FallbackExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.LocalExercisePlanProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExercisePlanModule {
    @Provides
    @Singleton
    fun provideRemoteExercisePlanConfig(): RemoteExercisePlanConfig {
        return RemoteExercisePlanConfig(
            enabled = BuildConfig.REMOTE_EXERCISE_PLAN_ENABLED,
            baseUrl = BuildConfig.REMOTE_EXERCISE_PLAN_BASE_URL,
        )
    }

    @Provides
    fun provideLocalExercisePlanProvider(): LocalExercisePlanProvider {
        return LocalExercisePlanProvider()
    }

    @Provides
    fun provideRemoteExercisePlanProvider(
        config: RemoteExercisePlanConfig,
        api: RemoteExercisePlanApi,
    ): RemoteExercisePlanProvider {
        return RemoteExercisePlanProvider(config, api)
    }

    @Provides
    fun provideExercisePlanProvider(
        remoteProvider: RemoteExercisePlanProvider,
        localProvider: LocalExercisePlanProvider,
    ): ExercisePlanProvider {
        return FallbackExercisePlanProvider(
            primary = remoteProvider,
            fallback = localProvider,
        )
    }
}
