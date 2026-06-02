package com.clefrun.core

import kotlin.math.abs
import kotlin.random.Random

object RuleBasedGenerator {
    fun generate(
        seed: Long,
        difficulty: Difficulty = Difficulty.MEDIUM,
        focus: ExerciseFocus = ExerciseFocus.READ_AHEAD,
        constraints: GenerationConstraints = GenerationConstraints()
    ): Exercise {
        val random = Random(seed)
        val profile = difficulty.profile()
            .forFocus(focus)
            .forConstraints(constraints, focus)
        val chords = buildChordProgression(random)

        var previousRhMidi: Int? = null
        var previousShape: List<Int>? = null
        var recentRhMidis: List<Int> = emptyList()

        val bars = mutableListOf<Bar>()
        chords.forEachIndexed { index, chord ->
            val rightHand = generateRightHandBar(
                random = random,
                chord = chord,
                previousRhMidi = previousRhMidi,
                previousShape = previousShape,
                globalRecentMidis = recentRhMidis,
                profile = profile,
                focus = focus
            )

            maybeInsertChromaticApproach(
                random = random,
                chord = chord,
                rightHand = rightHand,
                profile = profile
            )

            if (rightHand.size > 1) {
                previousShape = rightHand.zipWithNext { a, b -> b.midi - a.midi }
            }
            previousRhMidi = rightHand.lastOrNull()?.midi ?: previousRhMidi
            recentRhMidis = (recentRhMidis + rightHand.map { it.midi }).takeLast(3)

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
                leftHand = generateLeftHand(random, chord, profile, focus)
            )
        }

        return Exercise(bars = bars)
    }
}

private data class DifficultyProfile(
    val rightHandRange: IntRange,
    val preferredOpeningRange: IntRange,
    val rhythmPatterns: List<List<Duration>>,
    val accidentalProbability: Double,
    val maxLeap: Int,
    val largeLeapWeightPenalty: Int,
    val repeatPenalty: Int,
    val leftHandStyle: LeftHandStyle,
    val rightHandMotion: GenerationRightHandMotion? = null,
    val leftHandTexture: GenerationLeftHandTexture? = null,
    val maxAccidentalsPerBar: Int = 1,
    val strictMaxLeap: Boolean = false
)

private enum class LeftHandStyle {
    EASY,
    MEDIUM,
    HARD
}

private data class MutableRhEvent(
    var midi: Int,
    val duration: Duration,
    val beatStart: Int
)

private fun Difficulty.profile(): DifficultyProfile {
    return when (this) {
        Difficulty.EASY -> DifficultyProfile(
            rightHandRange = 62..74,
            preferredOpeningRange = 65..71,
            rhythmPatterns = listOf(
                listOf(Duration.HALF, Duration.HALF),
                listOf(Duration.QUARTER, Duration.QUARTER, Duration.HALF),
                listOf(Duration.HALF, Duration.QUARTER, Duration.QUARTER),
                listOf(Duration.HALF, Duration.HALF)
            ),
            accidentalProbability = 0.0,
            maxLeap = 5,
            largeLeapWeightPenalty = 3,
            repeatPenalty = 4,
            leftHandStyle = LeftHandStyle.EASY
        )
        Difficulty.MEDIUM -> DifficultyProfile(
            rightHandRange = 60..79,
            preferredOpeningRange = 64..72,
            rhythmPatterns = listOf(
                listOf(Duration.QUARTER, Duration.QUARTER, Duration.QUARTER, Duration.QUARTER),
                listOf(Duration.QUARTER, Duration.QUARTER, Duration.HALF),
                listOf(Duration.HALF, Duration.QUARTER, Duration.QUARTER),
                listOf(Duration.QUARTER, Duration.HALF, Duration.QUARTER),
                listOf(Duration.HALF, Duration.HALF)
            ),
            accidentalProbability = 0.18,
            maxLeap = 7,
            largeLeapWeightPenalty = 2,
            repeatPenalty = 3,
            leftHandStyle = LeftHandStyle.MEDIUM
        )
        Difficulty.HARD -> DifficultyProfile(
            rightHandRange = 59..81,
            preferredOpeningRange = 63..74,
            rhythmPatterns = listOf(
                listOf(Duration.QUARTER, Duration.QUARTER, Duration.QUARTER, Duration.QUARTER),
                listOf(Duration.QUARTER, Duration.QUARTER, Duration.QUARTER, Duration.QUARTER),
                listOf(Duration.QUARTER, Duration.QUARTER, Duration.HALF),
                listOf(Duration.HALF, Duration.QUARTER, Duration.QUARTER),
                listOf(Duration.QUARTER, Duration.HALF, Duration.QUARTER)
            ),
            accidentalProbability = 0.35,
            maxLeap = 9,
            largeLeapWeightPenalty = 1,
            repeatPenalty = 2,
            leftHandStyle = LeftHandStyle.HARD
        )
    }
}

private fun DifficultyProfile.forFocus(focus: ExerciseFocus): DifficultyProfile {
    return when (focus) {
        ExerciseFocus.ACCIDENTALS -> copy(
            accidentalProbability = when {
                accidentalProbability <= 0.0 -> 0.12
                else -> (accidentalProbability + 0.12).coerceAtMost(0.48)
            }
        )
        else -> this
    }
}

private fun DifficultyProfile.forConstraints(
    constraints: GenerationConstraints,
    focus: ExerciseFocus
): DifficultyProfile {
    val effectiveAccidentalDensity = constraints.accidentalDensity
    val effectiveRightHandMotion = constraints.rightHandMotion ?: when (focus) {
        ExerciseFocus.LEFT_HAND_STABILITY -> GenerationRightHandMotion.MOSTLY_STEPWISE
        ExerciseFocus.SMALL_LEAPS -> GenerationRightHandMotion.STEPWISE_WITH_SMALL_LEAPS
        else -> null
    }
    val effectiveLeftHandTexture = when (focus) {
        ExerciseFocus.LEFT_HAND_STABILITY -> GenerationLeftHandTexture.STEADY_BASS
        else -> constraints.leftHandTexture
    }
    val effectiveMaxLeap = constraints.maxLeap ?: when (focus) {
        ExerciseFocus.SMALL_LEAPS -> GenerationMaxLeap.FOURTH
        else -> null
    }

    val constrainedAccidentalProbability = when (constraints.accidentalDensity) {
        GenerationAccidentalDensity.NONE -> 0.0
        GenerationAccidentalDensity.LOW -> if (focus == ExerciseFocus.ACCIDENTALS) 0.38 else 0.24
        GenerationAccidentalDensity.MEDIUM -> if (focus == ExerciseFocus.ACCIDENTALS) 0.96 else 0.76
        null -> accidentalProbability
    }

    val constrainedMaxLeap = when (effectiveMaxLeap) {
        GenerationMaxLeap.SECOND -> 2
        GenerationMaxLeap.THIRD -> 4
        GenerationMaxLeap.FOURTH -> 5
        null -> maxLeap
    }

    return copy(
        accidentalProbability = constrainedAccidentalProbability,
        maxLeap = constrainedMaxLeap,
        rightHandMotion = effectiveRightHandMotion,
        leftHandTexture = effectiveLeftHandTexture,
        maxAccidentalsPerBar = when (effectiveAccidentalDensity) {
            GenerationAccidentalDensity.MEDIUM -> 2
            else -> 1
        },
        strictMaxLeap = effectiveMaxLeap != null
    )
}

private fun buildChordProgression(random: Random): List<ChordFunction> {
    val phraseA = buildPhrase(random)
    val phraseB = buildPhrase(random, avoidOpenings = setOf(phraseA.first(), phraseA.last()))
    return phraseA + phraseB
}

private fun buildPhrase(
    random: Random,
    avoidOpenings: Set<ChordFunction> = emptySet()
): List<ChordFunction> {
    val openingPool = listOf(
        ChordFunction.I,
        ChordFunction.II,
        ChordFunction.III,
        ChordFunction.IV,
        ChordFunction.VI
    ).filterNot { it in avoidOpenings }
    val bar1 = openingPool[random.nextInt(openingPool.size)]

    val bar2Pool = listOf(
        ChordFunction.II,
        ChordFunction.III,
        ChordFunction.IV,
        ChordFunction.V,
        ChordFunction.VI
    ).filter { it != bar1 }
    val bar2 = bar2Pool[random.nextInt(bar2Pool.size)]

    val bar3Pool = listOf(ChordFunction.V, ChordFunction.II, ChordFunction.IV)
        .filter { it != bar2 }
    val bar3 = if (random.nextDouble() < 0.75 && bar2 != ChordFunction.V) {
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
    previousShape: List<Int>?,
    globalRecentMidis: List<Int>,
    profile: DifficultyProfile,
    focus: ExerciseFocus
): MutableList<MutableRhEvent> {
    var attempt = 0
    while (true) {
        val rhythms = rightHandRhythmPattern(random, profile)
        val events = mutableListOf<MutableRhEvent>()
        var beat = 1
        var localPrev = previousRhMidi
        val contourDirection = if (random.nextBoolean()) 1 else -1

        for (duration in rhythms) {
            val midi = chooseRightHandMidi(
                random = random,
                chord = chord,
                previousMidi = localPrev,
                mustBeChordTone = beat == 1 || beat == 3,
                contourDirection = contourDirection,
                weakBeat = beat == 2 || beat == 4,
                recent = (globalRecentMidis + events.takeLast(2).map { it.midi }).takeLast(3),
                profile = profile,
                focus = focus
            )
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
    rightHand: MutableList<MutableRhEvent>,
    profile: DifficultyProfile
) {
    if (profile.accidentalProbability <= 0.0 || random.nextDouble() >= profile.accidentalProbability) {
        return
    }

    val eligibleIndices = rightHand.indices.filter { index ->
        val event = rightHand[index]
        if (index + 1 >= rightHand.size) return@filter false
        if (event.beatStart != 2 && event.beatStart != 4) return@filter false

        val targetMidi = rightHand[index + 1].midi
        val previousMidi = rightHand.getOrNull(index - 1)?.midi
        isChordTone(targetMidi, chord) &&
            isCmajor(targetMidi) &&
            chromaticApproachCandidates(targetMidi, profile.rightHandRange, previousMidi, profile).isNotEmpty()
    }

    if (eligibleIndices.isEmpty()) return

    val selectedIndices = eligibleIndices.shuffled(random).take(profile.maxAccidentalsPerBar)
    selectedIndices.forEach { index ->
        val targetMidi = rightHand[index + 1].midi
        val previousMidi = rightHand.getOrNull(index - 1)?.midi
        val candidates = chromaticApproachCandidates(targetMidi, profile.rightHandRange, previousMidi, profile)
        rightHand[index].midi = candidates[random.nextInt(candidates.size)]
    }
}

private fun chromaticApproachCandidates(
    targetMidi: Int,
    range: IntRange,
    previousMidi: Int?,
    profile: DifficultyProfile
): List<Int> {
    return listOf(targetMidi - 1, targetMidi + 1).filter { midi ->
        midi in range &&
            isAccidentalPitchClass(pitchClass(midi)) &&
            (!profile.strictMaxLeap || previousMidi == null || abs(midi - previousMidi) <= profile.maxLeap)
    }
}

private fun rightHandRhythmPattern(random: Random, profile: DifficultyProfile): List<Duration> {
    return profile.rhythmPatterns[random.nextInt(profile.rhythmPatterns.size)]
}

private fun generateLeftHand(
    random: Random,
    chord: ChordFunction,
    profile: DifficultyProfile,
    focus: ExerciseFocus
): List<NoteEvent> {
    val root = leftHandRootMidi(chord)
    val third = leftHandThirdMidi(chord)
    val fifth = leftHandFifthMidi(chord)

    val pattern: List<Pair<Int, Duration>> = when (profile.leftHandTexture) {
        GenerationLeftHandTexture.SIMPLE_BASS -> listOf(root to Duration.WHOLE)
        GenerationLeftHandTexture.STEADY_BASS -> steadyBassPattern(random, root, fifth)
        null -> if (focus == ExerciseFocus.LEFT_HAND_STABILITY) {
            steadyBassPattern(random, root, fifth)
        } else when (profile.leftHandStyle) {
            LeftHandStyle.EASY -> if (random.nextDouble() < 0.65) {
                listOf(root to Duration.WHOLE)
            } else {
                listOf(
                    root to Duration.HALF,
                    (if (random.nextBoolean()) root else fifth) to Duration.HALF
                )
            }
            LeftHandStyle.MEDIUM -> {
                if (random.nextDouble() < 0.55) {
                    listOf(
                        root to Duration.HALF,
                        (if (random.nextBoolean()) fifth else root) to Duration.HALF
                    )
                } else {
                    listOf(
                        root to Duration.QUARTER,
                        fifth to Duration.QUARTER,
                        third to Duration.QUARTER,
                        fifth to Duration.QUARTER
                    )
                }
            }
            LeftHandStyle.HARD -> {
                if (random.nextDouble() < 0.30) {
                    listOf(
                        root to Duration.HALF,
                        fifth to Duration.QUARTER,
                        third to Duration.QUARTER
                    )
                } else {
                    listOf(
                        root to Duration.QUARTER,
                        fifth to Duration.QUARTER,
                        third to Duration.QUARTER,
                        (if (random.nextBoolean()) fifth else root) to Duration.QUARTER
                    )
                }
            }
        }
    }

    var beatStart = 1
    return pattern.map { (midi, duration) ->
        noteFromMidi(
            midi = midi,
            duration = duration,
            staff = 2,
            voice = 2,
            beatStart = beatStart
        ).also {
            beatStart += duration.beats
        }
    }
}

private fun steadyBassPattern(
    random: Random,
    root: Int,
    fifth: Int
): List<Pair<Int, Duration>> {
    return when (random.nextInt(3)) {
        0 -> listOf(
            root to Duration.HALF,
            fifth to Duration.HALF
        )
        1 -> listOf(
            root to Duration.QUARTER,
            fifth to Duration.QUARTER,
            root to Duration.HALF
        )
        else -> listOf(
            root to Duration.QUARTER,
            fifth to Duration.QUARTER,
            root to Duration.QUARTER,
            fifth to Duration.QUARTER
        )
    }
}

private fun chooseRightHandMidi(
    random: Random,
    chord: ChordFunction,
    previousMidi: Int?,
    mustBeChordTone: Boolean,
    contourDirection: Int,
    weakBeat: Boolean,
    recent: List<Int>,
    profile: DifficultyProfile,
    focus: ExerciseFocus
): Int {
    val candidates = profile.rightHandRange.filter { midi ->
        isCmajor(midi) && (!mustBeChordTone || isChordTone(midi, chord))
    }

    if (previousMidi == null) {
        val pool = candidates.filter { it in profile.preferredOpeningRange }.ifEmpty { candidates }
        return pool[random.nextInt(pool.size)]
    }

    val repeatAware = candidates.filterNot { midi ->
        recent.size >= 2 && recent.takeLast(2).all { it == midi }
    }.ifEmpty { candidates }

    val intervalAware = repeatAware.filter { midi ->
        abs(midi - previousMidi) <= profile.maxLeap
    }.ifEmpty {
        if (!profile.strictMaxLeap) {
            repeatAware
        } else {
            val relaxedCandidates = profile.rightHandRange.filter { midi ->
                isCmajor(midi) && abs(midi - previousMidi) <= profile.maxLeap
            }
            relaxedCandidates.filterNot { midi ->
                recent.size >= 2 && recent.takeLast(2).all { it == midi }
            }.ifEmpty { relaxedCandidates }
        }
    }.ifEmpty {
        listOf(nearestMidi(previousMidi, profile.rightHandRange))
    }

    val weighted = intervalAware
        .map { midi ->
            val diff = abs(midi - previousMidi)
            var weight = when {
                diff == 0 -> 2
                diff <= 1 -> 7
                diff <= 2 -> 6
                diff <= 4 -> 4
                diff <= 5 -> 2
                else -> 1
            }

            if (diff >= 5) {
                weight = (weight - profile.largeLeapWeightPenalty).coerceAtLeast(1)
            }

            weight = weight.forMotion(diff, profile.rightHandMotion)

            if (focus == ExerciseFocus.SMALL_LEAPS) {
                weight = when {
                    diff in 3..5 -> weight + 3
                    diff > 5 -> (weight - 2).coerceAtLeast(1)
                    diff == 0 -> (weight - 1).coerceAtLeast(1)
                    else -> weight
                }
            }

            if (weakBeat) {
                val direction = midi - previousMidi
                if (direction != 0 && direction / abs(direction) == contourDirection) {
                    weight += 2
                }
            }

            if (recent.isNotEmpty() && midi == recent.last()) {
                weight = (weight - profile.repeatPenalty).coerceAtLeast(1)
            }

            midi to weight
        }
        .flatMap { (midi, weight) -> List(weight) { midi } }

    return weighted[random.nextInt(weighted.size)]
}

private fun Int.forMotion(diff: Int, motion: GenerationRightHandMotion?): Int {
    return when (motion) {
        GenerationRightHandMotion.MOSTLY_STEPWISE -> when {
            diff <= 2 -> this + 6
            diff <= 4 -> 1
            else -> 1
        }
        GenerationRightHandMotion.STEPWISE_WITH_SMALL_LEAPS -> when {
            diff in 3..5 -> this + 12
            diff > 5 -> 1
            diff == 0 -> 1
            diff <= 2 -> (this - 3).coerceAtLeast(2)
            else -> this
        }
        null -> this
    }
}

private fun nearestMidi(previousMidi: Int, range: IntRange): Int {
    return previousMidi.coerceIn(range.first, range.last)
}

private fun isAccidentalPitchClass(pitchClass: Int): Boolean = pitchClass in setOf(1, 3, 6, 8, 10)

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
    return when (chord) {
        ChordFunction.I -> 36
        ChordFunction.II -> 38
        ChordFunction.III -> 40
        ChordFunction.IV -> 41
        ChordFunction.V -> 43
        ChordFunction.VI -> 45
    }
}

private fun leftHandThirdMidi(chord: ChordFunction): Int {
    return leftHandRootMidi(chord) + when (chord) {
        ChordFunction.I, ChordFunction.IV, ChordFunction.V -> 4
        ChordFunction.II, ChordFunction.III, ChordFunction.VI -> 3
    }
}

private fun leftHandFifthMidi(chord: ChordFunction): Int = leftHandRootMidi(chord) + 7

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
