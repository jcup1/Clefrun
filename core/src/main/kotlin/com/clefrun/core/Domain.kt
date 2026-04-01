package com.clefrun.core

data class Exercise(
    val bars: List<Bar>,
    val beats: Int = 4,
    val beatType: Int = 4,
    val keyFifths: Int = 0
)

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

data class Bar(
    val number: Int,
    val chord: ChordFunction,
    val rightHand: List<NoteEvent>,
    val leftHand: List<NoteEvent>
)

data class NoteEvent(
    val pitch: Pitch?,
    val duration: Duration,
    val staff: Int,
    val voice: Int,
    val beatStart: Int,
    val isRest: Boolean = false
) {
    init {
        if (!isRest) {
            require(pitch != null) { "Non-rest NoteEvent must define pitch." }
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
    II,
    III,
    IV,
    V,
    VI
}

enum class Step {
    C,
    D,
    E,
    F,
    G,
    A,
    B
}

data class Pitch(
    val step: Step,
    val alter: Int,
    val octave: Int
) {
    init {
        require(alter in -1..1) { "Pitch alter must be -1, 0, or 1." }
    }
}
