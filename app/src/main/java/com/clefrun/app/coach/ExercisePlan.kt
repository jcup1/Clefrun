package com.clefrun.app.coach

import com.clefrun.core.Difficulty

data class ExercisePlan(
    val id: String,
    val source: ExercisePlanSource,
    val mode: ExercisePlanMode,
    val difficulty: Difficulty,
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
