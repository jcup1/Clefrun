package com.clefrun.app.feature.sightreading

import com.clefrun.app.domain.exerciseplan.ExercisePlan
import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.RuleBasedGenerator

internal fun generateExerciseXml(exercisePlan: ExercisePlan): String {
    val exercise = RuleBasedGenerator.generate(
        seed = exercisePlan.seed,
        difficulty = exercisePlan.difficulty,
        focus = exercisePlan.generatorFocus
    )
    return MusicXmlWriter.write(exercise)
}
