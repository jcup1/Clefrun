package com.clefrun.core

data class Exercise(
    val bars: List<Bar>,
    val beats: Int = 4,
    val beatType: Int = 4,
    val keyFifths: Int = 0
)

data class Bar(
    val number: Int,
    val chord: ChordFunction,
    val rightHand: List<NoteEvent>,
    val leftHand: List<NoteEvent>
)

data class NoteEvent(
    val step: String?,
    val octave: Int?,
    val duration: Duration,
    val staff: Int,
    val voice: Int,
    val beatStart: Int,
    val isRest: Boolean = false
) {
    init {
        if (!isRest) {
            require(step != null) { "Non-rest NoteEvent must define step." }
            require(octave != null) { "Non-rest NoteEvent must define octave." }
        }
    }
}

enum class Duration(val beats: Int, val musicXmlType: String) {
    QUARTER(1, "quarter"),
    HALF(2, "half"),
    WHOLE(4, "whole")
}

enum class ChordFunction {
    I,
    IV,
    V,
    VI
}
