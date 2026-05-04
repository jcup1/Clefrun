package com.clefrun.app.domain.exerciseplan

import com.clefrun.core.Difficulty
import com.clefrun.core.ExerciseFocus

data class ExercisePlanRequest(
    val seed: Long,
    val difficulty: Difficulty,
    val targetedPracticeText: String? = null,
)

data class ExercisePlan(
    val id: String,
    val seed: Long,
    val source: ExercisePlanSource,
    val mode: ExercisePlanMode,
    val difficulty: Difficulty,
    val generatorFocus: ExerciseFocus,
    val focus: String,
    val constraints: ExercisePlanConstraints = ExercisePlanConstraints(),
    val coach: CoachContent,
)

data class ExercisePlanConstraints(
    val accidentalDensity: AccidentalDensity? = null,
    val rightHandMotion: RightHandMotion? = null,
    val leftHandTexture: LeftHandTexture? = null,
    val maxLeap: MaxLeap? = null,
)

enum class AccidentalDensity {
    NONE,
    LOW,
    MEDIUM
}

enum class RightHandMotion {
    MOSTLY_STEPWISE,
    STEPWISE_WITH_SMALL_LEAPS
}

enum class LeftHandTexture {
    SIMPLE_BASS,
    STEADY_BASS
}

enum class MaxLeap {
    SECOND,
    THIRD,
    FOURTH
}

data class CoachContent(
    val title: String,
    val focusLabel: String,
    val body: String,
    val watchOut: String? = null,
)

enum class ExercisePlanSource {
    LOCAL,
    REMOTE
}

enum class ExercisePlanMode {
    SIGHT_READING
}
