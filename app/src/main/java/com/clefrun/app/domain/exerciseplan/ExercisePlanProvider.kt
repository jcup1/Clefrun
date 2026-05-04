package com.clefrun.app.domain.exerciseplan

import com.clefrun.core.ExerciseFocus
import java.util.Locale

fun interface ExercisePlanProvider {
    suspend fun nextSightReadingPlan(request: ExercisePlanRequest): ExercisePlan
}

class LocalExercisePlanProvider : ExercisePlanProvider {
    override suspend fun nextSightReadingPlan(request: ExercisePlanRequest): ExercisePlan {
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
    val tokens = targetedPracticeText
        ?.lowercase(Locale.ROOT)
        ?.split(NonLetterRegex)
        ?.filter { it.isNotBlank() }
        ?.toSet()
        .orEmpty()
    if (tokens.isEmpty()) return ExerciseFocus.READ_AHEAD

    return when {
        tokens.any { it in leftHandTokens } -> ExerciseFocus.LEFT_HAND_STABILITY
        tokens.any { it in accidentalTokens } -> ExerciseFocus.ACCIDENTALS
        tokens.any { it in smallLeapTokens } -> ExerciseFocus.SMALL_LEAPS
        tokens.any { it in chordTokens } -> ExerciseFocus.READ_AHEAD
        else -> ExerciseFocus.READ_AHEAD
    }
}

private val NonLetterRegex = Regex("[^\\p{L}]+")
private val leftHandTokens = setOf("left", "bass")
private val accidentalTokens = setOf("accidental", "accidentals", "sharp", "sharps", "flat", "flats")
private val smallLeapTokens = setOf("leap", "leaps", "jump", "jumps")
private val chordTokens = setOf("chord", "chords")

private fun tipFor(focus: ExerciseFocus): CoachContent {
    return when (focus) {
        ExerciseFocus.READ_AHEAD -> CoachContent(
            title = "Coach tip",
            focusLabel = focus.toDisplayLabel(),
            body = "Look one beat ahead and keep the pulse steady through the full phrase.",
        )
        ExerciseFocus.LEFT_HAND_STABILITY -> CoachContent(
            title = "Coach tip",
            focusLabel = focus.toDisplayLabel(),
            body = "Keep the left hand steady and light while reading the right hand one beat ahead.",
        )
        ExerciseFocus.ACCIDENTALS -> CoachContent(
            title = "Coach tip",
            focusLabel = focus.toDisplayLabel(),
            body = "Scan for altered notes before you start, then keep the pulse steady when they appear.",
        )
        ExerciseFocus.SMALL_LEAPS -> CoachContent(
            title = "Coach tip",
            focusLabel = focus.toDisplayLabel(),
            body = "Read the interval shape before moving, and prepare the next hand position early.",
        )
    }
}
