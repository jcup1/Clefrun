package com.clefrun.app.domain.exerciseplan

import com.clefrun.core.ExerciseFocus

fun ExerciseFocus.toDisplayLabel(): String {
    return when (this) {
        ExerciseFocus.READ_AHEAD -> "Read ahead"
        ExerciseFocus.LEFT_HAND_STABILITY -> "Left hand stability"
        ExerciseFocus.ACCIDENTALS -> "Accidentals"
        ExerciseFocus.SMALL_LEAPS -> "Small leaps"
    }
}
