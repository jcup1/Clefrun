package com.clefrun.core

data class Exercise(
    val bars: List<Bar>,
    val beats: Int = 4,
    val beatType: Int = 4,
    val keyFifths: Int = 0,
    val divisions: Int = 2,
    val showTimeSignature: Boolean = true,
    val pageBottomMarginTenths: Int? = null
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
    val leftHand: List<NoteEvent>,
    val staffLayout: StaffLayout = StaffLayout.GRAND_STAFF,
    val sectionLabel: String? = null,
    val sectionLabelOffsetX: Int = 0,
    val startsNewSystem: Boolean = false,
    val systemDistance: Int? = null
)

data class NoteEvent(
    val pitch: Pitch?,
    val additionalPitches: List<Pitch> = emptyList(),
    val duration: Duration,
    val staff: Int,
    val voice: Int,
    val beatStart: Int,
    val isRest: Boolean = false,
    val fingerings: List<Int> = emptyList(),
    val printObject: Boolean = true
) {
    init {
        if (!isRest) {
            require(pitch != null) { "Non-rest NoteEvent must define pitch." }
        }
        if (isRest) {
            require(additionalPitches.isEmpty()) { "Rest NoteEvent cannot define additional pitches." }
        }
        if (fingerings.isNotEmpty()) {
            require(fingerings.size == pitchCount) { "Fingerings must match the number of noteheads." }
        }
    }

    val pitchCount: Int
        get() = if (isRest || pitch == null) 0 else 1 + additionalPitches.size
}

enum class Duration(
    val beats: Int,
    val musicXmlType: String,
    val divisions: Int
) {
    EIGHTH(0, "eighth", 1),
    QUARTER(1, "quarter", 2),
    HALF(2, "half", 4),
    WHOLE(4, "whole", 8)
}

enum class StaffLayout {
    SINGLE_TREBLE,
    GRAND_STAFF
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

enum class PracticeMode {
    MAJOR,
    NATURAL_MINOR,
    HARMONIC_MINOR,
    MELODIC_MINOR
}

enum class PracticeTonic {
    C,
    D,
    E,
    F,
    G,
    A,
    B
}
