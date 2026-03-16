package com.clefrun.core

import kotlin.math.abs
import kotlin.random.Random

object RuleBasedGenerator {
    fun generate(seed: Long): Exercise {
        val random = Random(seed)
        val chords = buildChordProgression(random)

        var previousRhMidi: Int? = null
        var usedPerfectFifth = false
        var previousShape: List<Int>? = null
        var recentRhMidis: List<Int> = emptyList()

        val bars = mutableListOf<Bar>()
        chords.forEachIndexed { index, chord ->
            val rightHand = generateRightHandBar(
                random = random,
                chord = chord,
                previousRhMidi = previousRhMidi,
                usedPerfectFifth = usedPerfectFifth,
                previousShape = previousShape,
                globalRecentMidis = recentRhMidis
            )

            maybeInsertChromaticApproach(random = random, chord = chord, rightHand = rightHand)

            if (rightHand.size > 1) {
                previousShape = rightHand.zipWithNext { a, b -> b.midi - a.midi }
            }

            if (rightHand.size >= 2) {
                for (i in 1 until rightHand.size) {
                    if (abs(rightHand[i].midi - rightHand[i - 1].midi) == 7) {
                        usedPerfectFifth = true
                    }
                }
            }
            previousRhMidi = rightHand.lastOrNull()?.midi ?: previousRhMidi
            recentRhMidis = (recentRhMidis + rightHand.map { it.midi }).takeLast(2)

            bars += Bar(
                number = index + 1,
                chord = chord,
                rightHand = rightHand.map {
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

private fun buildChordProgression(random: Random): List<ChordFunction> {
    val openingPool = listOf(ChordFunction.I, ChordFunction.II, ChordFunction.III, ChordFunction.IV, ChordFunction.VI)
    val bar1 = openingPool[random.nextInt(openingPool.size)]

    val bar2Pool = listOf(ChordFunction.II, ChordFunction.III, ChordFunction.IV, ChordFunction.V, ChordFunction.VI)
        .filter { it != bar1 }
    val bar2 = bar2Pool[random.nextInt(bar2Pool.size)]

    val bar3Pool = listOf(ChordFunction.V, ChordFunction.II, ChordFunction.IV)
        .filter { it != bar2 }
    val bar3 = if (random.nextDouble() < 0.80 && bar2 != ChordFunction.V) {
        ChordFunction.V
    } else {
        bar3Pool[random.nextInt(bar3Pool.size)]
    }

    return listOf(bar1, bar2, bar3, ChordFunction.I)
}

private fun generateRightHandBar(
    random: Random,
    chord: ChordFunction,
    previousRhMidi: Int?,
    usedPerfectFifth: Boolean,
    previousShape: List<Int>?,
    globalRecentMidis: List<Int>
): MutableList<MutableRhEvent> {
    val rhythms = rightHandRhythmPattern(random)
    var attempt = 0
    while (true) {
        val events = mutableListOf<MutableRhEvent>()
        var beat = 1
        var localPrev = previousRhMidi
        var localUsedPerfectFifth = usedPerfectFifth
        val contourDirection = if (random.nextBoolean()) 1 else -1

        for (duration in rhythms) {
            val mustBeChordTone = beat == 1 || beat == 3
            val midi = chooseRightHandMidi(
                random = random,
                chord = chord,
                previousMidi = localPrev,
                mustBeChordTone = mustBeChordTone,
                canUsePerfectFifth = !localUsedPerfectFifth,
                contourDirection = contourDirection,
                weakBeat = beat == 2 || beat == 4,
                recent = (globalRecentMidis + events.takeLast(2).map { it.midi }).takeLast(2)
            )
            if (localPrev != null && abs(midi - localPrev) == 7) {
                localUsedPerfectFifth = true
            }
            events += MutableRhEvent(midi = midi, duration = duration, beatStart = beat)
            localPrev = midi
            beat += duration.beats
        }

        val shape = events.zipWithNext { a, b -> b.midi - a.midi }
        val shapeIsSame = previousShape != null && previousShape == shape
        if (!shapeIsSame || attempt >= 4) {
            return events
        }
        attempt += 1
    }
}

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
    canUsePerfectFifth: Boolean,
    contourDirection: Int,
    weakBeat: Boolean,
    recent: List<Int>
): Int {
    val candidates = rightHandRange().filter { midi ->
        isCmajor(midi) && (!mustBeChordTone || isChordTone(midi, chord))
    }

    if (previousMidi == null) {
        val preferred = candidates.filter { it in 64..72 }
        val pool = if (preferred.isNotEmpty()) preferred else candidates
        return pool[random.nextInt(pool.size)]
    }

    val filteredByRepeat = candidates.filterNot { midi ->
        recent.size == 2 && recent[0] == recent[1] && recent[1] == midi
    }
    val repeatAware = if (filteredByRepeat.isNotEmpty()) filteredByRepeat else candidates

    val intervalConstrained = repeatAware.filter { midi ->
        val diff = abs(midi - previousMidi)
        diff <= 5 || (diff == 7 && canUsePerfectFifth)
    }
    val pool = if (intervalConstrained.isNotEmpty()) intervalConstrained else repeatAware

    val weighted = pool
        .map { midi ->
            val diff = abs(midi - previousMidi)
            var weight = when {
                diff <= 1 -> 7
                diff <= 2 -> 5
                diff <= 4 -> 2
                diff == 5 -> 1
                diff == 7 -> 1
                else -> 1
            }

            if (weakBeat) {
                val direction = midi - previousMidi
                if (direction != 0 && direction / abs(direction) == contourDirection) {
                    weight += 2
                }
            }
            if (recent.isNotEmpty() && midi == recent.last()) {
                weight = (weight - 3).coerceAtLeast(1)
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
        ChordFunction.II -> setOf(2, 5, 9)
        ChordFunction.III -> setOf(4, 7, 11)
        ChordFunction.IV -> setOf(5, 9, 0)
        ChordFunction.V -> setOf(7, 11, 2)
        ChordFunction.VI -> setOf(9, 0, 4)
    }
    return pitchClass(midi) in tones
}

private fun leftHandRootMidi(chord: ChordFunction): Int {
    // C2..C3 preferred low register
    return when (chord) {
        ChordFunction.I -> 36   // C2
        ChordFunction.II -> 38  // D2
        ChordFunction.III -> 40 // E2
        ChordFunction.IV -> 41  // F2
        ChordFunction.V -> 43   // G2
        ChordFunction.VI -> 45  // A2
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
