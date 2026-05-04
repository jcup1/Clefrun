package com.clefrun.app.feature.sightreading

import com.clefrun.app.domain.exerciseplan.ExercisePlan
import javax.inject.Inject

fun interface ExerciseXmlGenerator {
    suspend fun generate(exercisePlan: ExercisePlan): String
}

class DefaultExerciseXmlGenerator @Inject constructor() : ExerciseXmlGenerator {
    override suspend fun generate(exercisePlan: ExercisePlan): String {
        return generateExerciseXml(exercisePlan)
    }
}
