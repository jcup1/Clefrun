package com.clefrun.app.data.exerciseplan.remote

import com.clefrun.app.domain.exerciseplan.ExercisePlan
import com.clefrun.app.domain.exerciseplan.ExercisePlanProvider
import com.clefrun.app.domain.exerciseplan.ExercisePlanRequest
import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class RemoteExercisePlanProvider(
    private val config: RemoteExercisePlanConfig,
    private val api: RemoteExercisePlanApi,
) : ExercisePlanProvider {
    override suspend fun nextSightReadingPlan(request: ExercisePlanRequest): ExercisePlan {
        if (!config.enabled) {
            throw RemoteExercisePlanException("Remote exercise plan provider is disabled.")
        }

        val responseDto = try {
            api.createExercisePlan(request.toRemoteDto())
        } catch (exception: HttpException) {
            throw RemoteExercisePlanException("Remote exercise plan returned HTTP ${exception.code()}.", exception)
        } catch (exception: IOException) {
            throw RemoteExercisePlanException("Remote exercise plan request failed.", exception)
        } catch (exception: SerializationException) {
            throw RemoteExercisePlanException("Remote exercise plan response is invalid.", exception)
        }

        return responseDto.toExercisePlan(request)
    }
}
