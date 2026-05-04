package com.clefrun.app.data.exerciseplan.remote

import com.clefrun.app.domain.exerciseplan.AccidentalDensity
import com.clefrun.app.domain.exerciseplan.CoachContent
import com.clefrun.app.domain.exerciseplan.ExercisePlan
import com.clefrun.app.domain.exerciseplan.ExercisePlanConstraints
import com.clefrun.app.domain.exerciseplan.ExercisePlanMode
import com.clefrun.app.domain.exerciseplan.ExercisePlanRequest
import com.clefrun.app.domain.exerciseplan.ExercisePlanSource
import com.clefrun.app.domain.exerciseplan.LeftHandTexture
import com.clefrun.app.domain.exerciseplan.MaxLeap
import com.clefrun.app.domain.exerciseplan.RightHandMotion
import com.clefrun.app.domain.exerciseplan.toDisplayLabel
import com.clefrun.core.ExerciseFocus

internal fun ExercisePlanRequest.toRemoteDto(): RemoteExercisePlanRequestDto {
    return RemoteExercisePlanRequestDto(
        difficulty = difficulty.name,
        targetedPracticeText = targetedPracticeText,
        supportedFocuses = ExerciseFocus.entries.map { it.name }
    )
}

internal fun RemoteExercisePlanResponseDto.toExercisePlan(request: ExercisePlanRequest): ExercisePlan {
    val focus = focus.toExerciseFocus()
    val coachTitle = coach.title.trim()
    val coachBody = coach.body.trim()
    if (coachTitle.isBlank() || coachBody.isBlank()) {
        throw RemoteExercisePlanException("Remote exercise plan coach content is blank.")
    }

    return ExercisePlan(
        id = "remote-sight-reading-${request.seed}",
        seed = request.seed,
        source = ExercisePlanSource.REMOTE,
        mode = ExercisePlanMode.SIGHT_READING,
        difficulty = request.difficulty,
        generatorFocus = focus,
        focus = focus.toDisplayLabel(),
        constraints = constraints.toExercisePlanConstraints(),
        coach = CoachContent(
            title = coachTitle,
            focusLabel = focus.toDisplayLabel(),
            body = coachBody,
            watchOut = coach.watchOut?.trim()?.ifBlank { null },
        ),
    )
}

private fun RemoteExerciseFocusDto.toExerciseFocus(): ExerciseFocus {
    return when (this) {
        RemoteExerciseFocusDto.READ_AHEAD -> ExerciseFocus.READ_AHEAD
        RemoteExerciseFocusDto.LEFT_HAND_STABILITY -> ExerciseFocus.LEFT_HAND_STABILITY
        RemoteExerciseFocusDto.ACCIDENTALS -> ExerciseFocus.ACCIDENTALS
        RemoteExerciseFocusDto.SMALL_LEAPS -> ExerciseFocus.SMALL_LEAPS
    }
}

private fun RemoteExercisePlanConstraintsDto.toExercisePlanConstraints(): ExercisePlanConstraints {
    return ExercisePlanConstraints(
        accidentalDensity = when (accidentalDensity) {
            RemoteAccidentalDensityDto.NONE -> AccidentalDensity.NONE
            RemoteAccidentalDensityDto.LOW -> AccidentalDensity.LOW
            RemoteAccidentalDensityDto.MEDIUM -> AccidentalDensity.MEDIUM
        },
        rightHandMotion = when (rightHandMotion) {
            RemoteRightHandMotionDto.MOSTLY_STEPWISE -> RightHandMotion.MOSTLY_STEPWISE
            RemoteRightHandMotionDto.STEPWISE_WITH_SMALL_LEAPS -> RightHandMotion.STEPWISE_WITH_SMALL_LEAPS
        },
        leftHandTexture = when (leftHandTexture) {
            RemoteLeftHandTextureDto.SIMPLE_BASS -> LeftHandTexture.SIMPLE_BASS
            RemoteLeftHandTextureDto.STEADY_BASS -> LeftHandTexture.STEADY_BASS
        },
        maxLeap = when (maxLeap) {
            RemoteMaxLeapDto.SECOND -> MaxLeap.SECOND
            RemoteMaxLeapDto.THIRD -> MaxLeap.THIRD
            RemoteMaxLeapDto.FOURTH -> MaxLeap.FOURTH
        },
    )
}
