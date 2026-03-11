package com.clefrun.core

import kotlin.math.abs
import kotlin.random.Random

object RuleBasedGenerator {
    fun generate(seed: Long): Exercise {
        val random = Random(seed)
        val chords = listOf(
            randomEarlyChord(random),
            randomEarlyChord(random),
            if (random.nextDouble() < 0.85) ChordFunction.V else randomEarlyChord(random),
            ChordFunction.I
        )

        var previousRhMidi: Int? = null
        var usedPerfectFifth = false

        val bars = chords.mapIndexed { index, chord ->
            val rhythms = rightHandRhythmPattern(random)
            val rightHandMidi = mutableListOf<MutableRhEvent>()
            var beat = 1
            for (duration in rhythms) {
                val mustBeChordTone = beat == 1 || beat == 3
                val midi = chooseRightHandMidi(
                    random = random,
                    chord = chord,
                    previousMidi = previousRhMidi,
                    mustBeChordTone = mustBeChordTone,
                    canUsePerfectFifth = !usedPerfectFifth
                )
                if (previousRhMidi != null && abs(midi - previousRhMidi) == 7) {
                    usedPerfectFifth = true
                }
                previousRhMidi = midi

                rightHandMidi += MutableRhEvent(
                    midi = midi,
                    duration = duration,
                    beatStart = beat
                )
                beat += duration.beats
            }

            maybeInsertChromaticApproach(random = random, chord = chord, rightHand = rightHandMidi)

            Bar(
                number = index + 1,
                chord = chord,
                rightHand = rightHandMidi.map {
                    noteFromMidi(
                        midi = it.midi,
                        duration = it.duration,
                        staff = 1,
                        voice = 1,
                        beatStart = it.beatStart
                    )
                },
                leftHand = generateLeftHand(random, chord)
            )
        }

        return Exercise(bars = bars)
    }
}

private data class MutableRhEvent(
    var midi: Int,
    val duration: Duration,
    val beatStart: Int
)

private fun maybeInsertChromaticApproach(
    random: Random,
    chord: ChordFunction,
    rightHand: MutableList<MutableRhEvent>
) {
    if (random.nextDouble() >= 0.20) return

    val eligibleIndices = rightHand.indices.filter { index ->
        val event = rightHand[index]
        val hasNext = index + 1 < rightHand.size
        if (!hasNext) return@filter false

        val weakBeat = event.beatStart == 2 || event.beatStart == 4
        if (!weakBeat) return@filter false

        val targetMidi = rightHand[index + 1].midi
        if (!isChordTone(targetMidi, chord) || !isCmajor(targetMidi)) return@filter false

        chromaticApproachCandidates(targetMidi).isNotEmpty()
    }

    if (eligibleIndices.isEmpty()) return
    val index = eligibleIndices[random.nextInt(eligibleIndices.size)]
    val targetMidi = rightHand[index + 1].midi
    val candidates = chromaticApproachCandidates(targetMidi)
    rightHand[index].midi = candidates[random.nextInt(candidates.size)]
}

private fun chromaticApproachCandidates(targetMidi: Int): List<Int> {
    val candidates = listOf(targetMidi - 1, targetMidi + 1)
    return candidates.filter { midi ->
        midi in rightHandRange() && isAccidentalPitchClass(pitchClass(midi))
    }
}

private fun isAccidentalPitchClass(pitchClass: Int): Boolean {
    return pitchClass in setOf(1, 3, 6, 8, 10)
}

private fun randomEarlyChord(random: Random): ChordFunction {
    val pool = listOf(ChordFunction.I, ChordFunction.IV, ChordFunction.V, ChordFunction.VI)
    return pool[random.nextInt(pool.size)]
}

private fun rightHandRhythmPattern(random: Random): List<Duration> {
    val patterns = listOf(
        listOf(Duration.QUARTER, Duration.QUARTER, Duration.QUARTER, Duration.QUARTER),
        listOf(Duration.HALF, Duration.HALF),
        listOf(Duration.QUARTER, Duration.QUARTER, Duration.HALF),
        listOf(Duration.HALF, Duration.QUARTER, Duration.QUARTER)
    )
    return patterns[random.nextInt(patterns.size)]
}

private fun generateLeftHand(random: Random, chord: ChordFunction): List<NoteEvent> {
    val rootMidi = leftHandRootMidi(chord)
    val fifthMidi = leftHandFifthMidi(chord)
    val useFifthOnBeatThree = random.nextBoolean()

    return listOf(
        noteFromMidi(rootMidi, Duration.HALF, staff = 2, voice = 2, beatStart = 1),
        noteFromMidi(
            if (useFifthOnBeatThree) fifthMidi else rootMidi,
            Duration.HALF,
            staff = 2,
            voice = 2,
            beatStart = 3
        )
    )
}

private fun chooseRightHandMidi(
    random: Random,
    chord: ChordFunction,
    previousMidi: Int?,
    mustBeChordTone: Boolean,
    canUsePerfectFifth: Boolean
): Int {
    val candidates = rightHandRange().filter { midi ->
        isCmajor(midi) && (!mustBeChordTone || isChordTone(midi, chord))
    }

    if (previousMidi == null) {
        val preferred = candidates.filter { it in 64..72 }
        val pool = if (preferred.isNotEmpty()) preferred else candidates
        return pool[random.nextInt(pool.size)]
    }

    val allowed = candidates.filter { midi ->
        val diff = abs(midi - previousMidi)
        diff <= 5 || (diff == 7 && canUsePerfectFifth)
    }
    val pool = if (allowed.isNotEmpty()) {
        allowed
    } else {
        val nearest = candidates.minBy { abs(it - previousMidi) }
        listOf(nearest)
    }

    val weighted = pool
        .map { midi ->
            val diff = abs(midi - previousMidi)
            val weight = when {
                diff <= 1 -> 7
                diff <= 2 -> 5
                diff <= 4 -> 2
                diff == 5 -> 1
                diff == 7 -> 1
                else -> 1
            }
            midi to weight
        }
        .flatMap { (midi, weight) -> List(weight) { midi } }

    return weighted[random.nextInt(weighted.size)]
}

private fun rightHandRange(): IntRange = 60..79 // C4..G5

private fun isCmajor(midi: Int): Boolean = pitchClass(midi) in setOf(0, 2, 4, 5, 7, 9, 11)

private fun isChordTone(midi: Int, chord: ChordFunction): Boolean {
    val tones = when (chord) {
        ChordFunction.I -> setOf(0, 4, 7)
        ChordFunction.IV -> setOf(5, 9, 0)
        ChordFunction.V -> setOf(7, 11, 2)
        ChordFunction.VI -> setOf(9, 0, 4)
    }
    return pitchClass(midi) in tones
}

private fun leftHandRootMidi(chord: ChordFunction): Int {
    // C2..C3 preferred low register
    return when (chord) {
        ChordFunction.I -> 36  // C2
        ChordFunction.IV -> 41 // F2
        ChordFunction.V -> 43  // G2
        ChordFunction.VI -> 45 // A2
    }
}

private fun leftHandFifthMidi(chord: ChordFunction): Int {
    return leftHandRootMidi(chord) + 7
}

private fun noteFromMidi(
    midi: Int,
    duration: Duration,
    staff: Int,
    voice: Int,
    beatStart: Int
): NoteEvent {
    return NoteEvent(
        pitch = midiToPitch(midi),
        duration = duration,
        staff = staff,
        voice = voice,
        beatStart = beatStart
    )
}

private fun midiToPitch(midi: Int): Pitch {
    val pitchClass = pitchClass(midi)
    val (step, alter) = when (pitchClass) {
        0 -> Step.C to 0
        1 -> Step.C to 1
        2 -> Step.D to 0
        3 -> Step.D to 1
        4 -> Step.E to 0
        5 -> Step.F to 0
        6 -> Step.F to 1
        7 -> Step.G to 0
        8 -> Step.G to 1
        9 -> Step.A to 0
        10 -> Step.A to 1
        11 -> Step.B to 0
        else -> error("Unexpected pitch class")
    }
    val octave = (midi / 12) - 1
    return Pitch(step = step, alter = alter, octave = octave)
}

private fun pitchClass(midi: Int): Int = ((midi % 12) + 12) % 12
