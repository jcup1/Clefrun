package com.clefrun.core

data class Exercise(
    val bars: List<Bar>,
    val beats: Int = 4,
    val beatType: Int = 4,
    val keyFifths: Int = 0
)

data class Bar(
    val number: Int,
    val rightHand: List<NoteEvent>,
    val leftHand: List<NoteEvent>
)

data class NoteEvent(
    val step: String,
    val octave: Int,
    val duration: Duration,
    val staff: Int,
    val voice: Int
)

enum class Duration {
    QUARTER,
    HALF,
    WHOLE
}
