package com.clefrun.app.feature.sightreading

import com.clefrun.app.domain.exerciseplan.AccidentalDensity
import com.clefrun.app.domain.exerciseplan.ExercisePlan
import com.clefrun.app.domain.exerciseplan.ExercisePlanConstraints
import com.clefrun.app.domain.exerciseplan.LeftHandTexture
import com.clefrun.app.domain.exerciseplan.MaxLeap
import com.clefrun.app.domain.exerciseplan.RightHandMotion
import com.clefrun.core.GenerationAccidentalDensity
import com.clefrun.core.GenerationConstraints
import com.clefrun.core.GenerationLeftHandTexture
import com.clefrun.core.GenerationMaxLeap
import com.clefrun.core.GenerationRightHandMotion
import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.RuleBasedGenerator

internal fun generateExerciseXml(exercisePlan: ExercisePlan): String {
    val exercise = RuleBasedGenerator.generate(
        seed = exercisePlan.seed,
        difficulty = exercisePlan.difficulty,
        focus = exercisePlan.generatorFocus,
        constraints = exercisePlan.constraints.toGenerationConstraints()
    )
    return MusicXmlWriter.write(exercise)
}

internal fun ExercisePlanConstraints.toGenerationConstraints(): GenerationConstraints {
    return GenerationConstraints(
        accidentalDensity = accidentalDensity?.toGenerationAccidentalDensity(),
        rightHandMotion = rightHandMotion?.toGenerationRightHandMotion(),
        leftHandTexture = leftHandTexture?.toGenerationLeftHandTexture(),
        maxLeap = maxLeap?.toGenerationMaxLeap()
    )
}

private fun AccidentalDensity.toGenerationAccidentalDensity(): GenerationAccidentalDensity {
    return when (this) {
        AccidentalDensity.NONE -> GenerationAccidentalDensity.NONE
        AccidentalDensity.LOW -> GenerationAccidentalDensity.LOW
        AccidentalDensity.MEDIUM -> GenerationAccidentalDensity.MEDIUM
    }
}

private fun RightHandMotion.toGenerationRightHandMotion(): GenerationRightHandMotion {
    return when (this) {
        RightHandMotion.MOSTLY_STEPWISE -> GenerationRightHandMotion.MOSTLY_STEPWISE
        RightHandMotion.STEPWISE_WITH_SMALL_LEAPS -> GenerationRightHandMotion.STEPWISE_WITH_SMALL_LEAPS
    }
}

private fun LeftHandTexture.toGenerationLeftHandTexture(): GenerationLeftHandTexture {
    return when (this) {
        LeftHandTexture.SIMPLE_BASS -> GenerationLeftHandTexture.SIMPLE_BASS
        LeftHandTexture.STEADY_BASS -> GenerationLeftHandTexture.STEADY_BASS
    }
}

private fun MaxLeap.toGenerationMaxLeap(): GenerationMaxLeap {
    return when (this) {
        MaxLeap.SECOND -> GenerationMaxLeap.SECOND
        MaxLeap.THIRD -> GenerationMaxLeap.THIRD
        MaxLeap.FOURTH -> GenerationMaxLeap.FOURTH
    }
}
