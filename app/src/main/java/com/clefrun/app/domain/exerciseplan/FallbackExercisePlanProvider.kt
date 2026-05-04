package com.clefrun.app.domain.exerciseplan

import kotlinx.coroutines.CancellationException

class FallbackExercisePlanProvider(
    private val primary: ExercisePlanProvider,
    private val fallback: ExercisePlanProvider,
) : ExercisePlanProvider {
    override suspend fun nextSightReadingPlan(request: ExercisePlanRequest): ExercisePlan {
        return try {
            primary.nextSightReadingPlan(request)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            fallback.nextSightReadingPlan(request)
        }
    }
}
