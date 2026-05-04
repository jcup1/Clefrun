package com.clefrun.app.data.exerciseplan

import com.clefrun.app.BuildConfig
import com.clefrun.app.data.exerciseplan.remote.RemoteExercisePlanConfig
import com.clefrun.app.data.exerciseplan.remote.defaultRemoteExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.ExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.FallbackExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.LocalExercisePlanProvider

object ExercisePlanProviderFactory {
    fun create(): ExercisePlanProvider {
        val localProvider = LocalExercisePlanProvider()
        val remoteProvider = defaultRemoteExercisePlanProvider(
            config = RemoteExercisePlanConfig(
                enabled = BuildConfig.REMOTE_EXERCISE_PLAN_ENABLED,
                baseUrl = BuildConfig.REMOTE_EXERCISE_PLAN_BASE_URL,
            )
        )
        return FallbackExercisePlanProvider(
            primary = remoteProvider,
            fallback = localProvider,
        )
    }
}
