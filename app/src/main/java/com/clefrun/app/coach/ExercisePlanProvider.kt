package com.clefrun.app.coach

import com.clefrun.core.ExerciseFocus

fun interface ExercisePlanProvider {
    fun nextSightReadingPlan(request: ExercisePlanRequest): ExercisePlan
}

class LocalExercisePlanProvider : ExercisePlanProvider {
    override fun nextSightReadingPlan(request: ExercisePlanRequest): ExercisePlan {
        val focus = focusFor(request.targetedPracticeText)
        val tip = tipFor(focus)
        return ExercisePlan(
            id = "local-sight-reading-${request.seed}",
            seed = request.seed,
            source = ExercisePlanSource.LOCAL,
            mode = ExercisePlanMode.SIGHT_READING,
            difficulty = request.difficulty,
            generatorFocus = focus,
            focus = tip.focusLabel,
            coach = tip,
        )
    }
}

private fun focusFor(targetedPracticeText: String?): ExerciseFocus {
    val text = targetedPracticeText?.lowercase().orEmpty()
    if (text.isBlank()) return ExerciseFocus.READ_AHEAD

    return when {
        text.contains("left") || text.contains("bass") -> ExerciseFocus.LEFT_HAND_STABILITY
        text.contains("accidental") ||
            text.contains("sharp") ||
            text.contains("flat") ||
            text.contains("sharps") ||
            text.contains("flats") -> ExerciseFocus.ACCIDENTALS
        text.contains("leap") ||
            text.contains("leaps") ||
            text.contains("jump") ||
            text.contains("jumps") -> ExerciseFocus.SMALL_LEAPS
        text.contains("chord") || text.contains("chords") -> ExerciseFocus.READ_AHEAD
        else -> ExerciseFocus.READ_AHEAD
    }
}

private fun tipFor(focus: ExerciseFocus): CoachContent {
    return when (focus) {
        ExerciseFocus.READ_AHEAD -> CoachContent(
            title = "Coach tip",
            focusLabel = "Read ahead",
            body = "Look one beat ahead and keep the pulse steady through the full phrase.",
        )
        ExerciseFocus.LEFT_HAND_STABILITY -> CoachContent(
            title = "Coach tip",
            focusLabel = "Left hand stability",
            body = "Keep the left hand steady and light while reading the right hand one beat ahead.",
        )
        ExerciseFocus.ACCIDENTALS -> CoachContent(
            title = "Coach tip",
            focusLabel = "Accidentals",
            body = "Scan for altered notes before you start, then keep the pulse steady when they appear.",
        )
        ExerciseFocus.SMALL_LEAPS -> CoachContent(
            title = "Coach tip",
            focusLabel = "Small leaps",
            body = "Read the interval shape before moving, and prepare the next hand position early.",
        )
    }
}
