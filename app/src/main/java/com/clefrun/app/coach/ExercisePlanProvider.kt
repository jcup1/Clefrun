package com.clefrun.app.coach

import com.clefrun.core.Difficulty

fun interface ExercisePlanProvider {
    fun nextSightReadingPlan(
        seed: Long,
        difficulty: Difficulty,
    ): ExercisePlan
}

class LocalExercisePlanProvider : ExercisePlanProvider {
    override fun nextSightReadingPlan(
        seed: Long,
        difficulty: Difficulty,
    ): ExercisePlan {
        val index = (((seed - 1L) % tips.size) + tips.size) % tips.size
        val tip = tips[index.toInt()]
        return ExercisePlan(
            id = "local-sight-reading-$seed",
            source = ExercisePlanSource.LOCAL,
            mode = ExercisePlanMode.SIGHT_READING,
            difficulty = difficulty,
            focus = tip.focusLabel,
            coach = tip,
        )
    }
}

private val tips = listOf(
    CoachContent(
        title = "Coach tip",
        focusLabel = "Read ahead",
        body = "Look one beat ahead and keep the left hand light while the right hand moves.",
    ),
    CoachContent(
        title = "Coach tip",
        focusLabel = "Block chords together",
        body = "When you see a chord, press all notes at the same time.",
        watchOut = "Avoid rolling them like an arpeggio.",
    ),
    CoachContent(
        title = "Coach tip",
        focusLabel = "Steady rhythm",
        body = "Keep counting through the bar and avoid stopping when the hands move separately.",
    ),
)
