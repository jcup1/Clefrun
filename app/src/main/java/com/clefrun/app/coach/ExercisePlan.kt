package com.clefrun.app.coach

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
    val constraints: List<String> = emptyList(),
    val coach: CoachContent,
)

data class CoachContent(
    val title: String,
    val focusLabel: String,
    val body: String,
    val watchOut: String? = null,
)

enum class ExercisePlanSource {
    LOCAL
}

enum class ExercisePlanMode {
    SIGHT_READING
}
