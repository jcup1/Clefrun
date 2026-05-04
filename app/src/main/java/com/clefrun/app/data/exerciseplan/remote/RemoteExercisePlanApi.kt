package com.clefrun.app.data.exerciseplan.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface RemoteExercisePlanApi {
    @POST("exercise-plan")
    suspend fun createExercisePlan(
        @Body request: RemoteExercisePlanRequestDto,
    ): RemoteExercisePlanResponseDto
}
