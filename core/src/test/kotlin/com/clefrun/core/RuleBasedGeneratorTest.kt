package com.clefrun.core

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedGeneratorTest {
    @Test
    fun generatedExerciseHas8BarsForAllDifficulties() {
        Difficulty.entries.forEach { difficulty ->
            val exercise = RuleBasedGenerator.generate(seed = 42L, difficulty = difficulty)
            assertEquals(8, exercise.bars.size)
        }
    }

    @Test
    fun eachBarSumsTo4Beats() {
        val seeds = listOf(1L, 2L, 42L, 100L)
        Difficulty.entries.forEach { difficulty ->
            seeds.forEach { seed ->
                val exercise = RuleBasedGenerator.generate(seed = seed, difficulty = difficulty)
                exercise.bars.forEach { bar ->
                    assertEquals(4, bar.rightHand.sumOf { it.duration.beats })
                    assertEquals(4, bar.leftHand.sumOf { it.duration.beats })
                }
            }
        }
    }

    @Test
    fun accidentalRhNotesResolveBySemitoneIntoChordTone() {
        val seeds = (1L..150L).toList()
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.HARD)
            exercise.bars.forEach { bar ->
                bar.rightHand.forEachIndexed { index, note ->
                    val pitch = note.pitch ?: return@forEachIndexed
                    if (pitch.alter == 0) return@forEachIndexed

                    assertTrue(
                        "Accidental note must not be last in bar (seed=$seed, bar=${bar.number})",
                        index + 1 < bar.rightHand.size
                    )
                    val next = bar.rightHand[index + 1]
                    val nextPitch = next.pitch
                    assertTrue("Resolution target must be pitched note", nextPitch != null)

                    val semitone = abs(toMidi(pitch) - toMidi(nextPitch!!))
                    assertEquals("Accidental note must resolve by 1 semitone", 1, semitone)
                    assertTrue("Resolution note must be chord tone", isChordTone(nextPitch, bar.chord))
                }
            }
        }
    }

    @Test
    fun leftHandRootDoesNotRepeatExcessivelyAcrossBars() {
        val seeds = (1L..120L).toList()
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.MEDIUM)
            val roots = exercise.bars.map { toMidi(requireNotNull(it.leftHand.first().pitch)) }
            var consecutiveRepeats = 0
            for (i in 1 until roots.size) {
                if (roots[i] == roots[i - 1]) consecutiveRepeats += 1
            }
            assertTrue(
                "LH root repeats too often (seed=$seed, roots=$roots)",
                consecutiveRepeats == 0
            )
        }
    }

    @Test
    fun rightHandDoesNotContainLongRunsOfSamePitch() {
        val seeds = (1L..120L).toList()
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.MEDIUM)
            val rhMidis = exercise.bars
                .flatMap { it.rightHand }
                .map { toMidi(requireNotNull(it.pitch)) }

            var runLength = 1
            var maxRun = 1
            for (i in 1 until rhMidis.size) {
                runLength = if (rhMidis[i] == rhMidis[i - 1]) runLength + 1 else 1
                maxRun = maxOf(maxRun, runLength)
            }
            assertTrue(
                "RH pitch run too long (seed=$seed, maxRun=$maxRun)",
                maxRun <= 2
            )
        }
    }

    @Test
    fun generatedExerciseStillMeetsDurationAndWriterConstraints() {
        val exercise = RuleBasedGenerator.generate(seed = 77L, difficulty = Difficulty.MEDIUM)
        assertEquals(8, exercise.bars.size)
        exercise.bars.forEach { bar ->
            assertEquals(4, bar.rightHand.sumOf { it.duration.beats })
            assertEquals(4, bar.leftHand.sumOf { it.duration.beats })
        }

        val xml = MusicXmlWriter.write(exercise)
        assertTrue(xml.startsWith("<?xml"))
        assertTrue(xml.contains("<score-partwise"))
        assertTrue(xml.contains("<part id=\"P1\">"))
        assertEquals(8, "<measure number=\"".toRegex().findAll(xml).count())
    }

    @Test
    fun harderDifficultiesProduceMoreMotionAndAccidentals() {
        val seeds = (1L..24L).toList()

        val easyExercises = seeds.map { RuleBasedGenerator.generate(seed = it, difficulty = Difficulty.EASY) }
        val mediumExercises = seeds.map { RuleBasedGenerator.generate(seed = it, difficulty = Difficulty.MEDIUM) }
        val hardExercises = seeds.map { RuleBasedGenerator.generate(seed = it, difficulty = Difficulty.HARD) }

        val easyAccidentals = easyExercises.sumOf { countAccidentals(it) }
        val mediumAccidentals = mediumExercises.sumOf { countAccidentals(it) }
        val hardAccidentals = hardExercises.sumOf { countAccidentals(it) }

        assertEquals(0, easyAccidentals)
        assertTrue("Medium should introduce accidentals", mediumAccidentals > easyAccidentals)
        assertTrue("Hard should introduce at least as many accidentals as medium", hardAccidentals >= mediumAccidentals)

        val easyRhEvents = easyExercises.sumOf { countRightHandEvents(it) }
        val hardRhEvents = hardExercises.sumOf { countRightHandEvents(it) }
        assertTrue("Hard should be denser than easy", hardRhEvents > easyRhEvents)

        val easyRange = easyExercises.maxOf { rightHandSpan(it) }
        val hardRange = hardExercises.maxOf { rightHandSpan(it) }
        assertTrue("Hard should cover a wider RH span than easy", hardRange > easyRange)
    }

    @Test
    fun leftHandStabilityFocusUsesRegularRepeatedPattern() {
        val seeds = (1L..12L).toList()

        Difficulty.entries.forEach { difficulty ->
            seeds.forEach { seed ->
                val exercise = RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = difficulty,
                    focus = ExerciseFocus.LEFT_HAND_STABILITY
                )

                exercise.bars.forEach { bar ->
                    assertTrue(
                        "LH focus must have at least two attacks per bar (seed=$seed, bar=${bar.number})",
                        bar.leftHand.size >= 2
                    )
                    assertTrue(
                        "LH focus must use a stable bass template (seed=$seed, bar=${bar.number})",
                        bar.leftHand.map { it.duration } in SteadyBassDurationTemplates
                    )
                    val midis = bar.leftHand.map { toMidi(requireNotNull(it.pitch)) }
                    assertStableBassPattern(midis)
                    assertTrue(
                        "LH must stay below RH (seed=$seed, bar=${bar.number})",
                        bar.leftHand.maxOf { toMidi(requireNotNull(it.pitch)) } <
                            bar.rightHand.minOf { toMidi(requireNotNull(it.pitch)) }
                    )
                }
            }
        }
    }

    @Test
    fun leftHandStabilityOverridesConflictingSimpleBassConstraint() {
        val seeds = (1L..24L).toList()

        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(
                seed = seed,
                difficulty = Difficulty.MEDIUM,
                focus = ExerciseFocus.LEFT_HAND_STABILITY,
                constraints = GenerationConstraints(
                    leftHandTexture = GenerationLeftHandTexture.SIMPLE_BASS
                )
            )

            exercise.bars.forEach { bar ->
                assertTrue(
                    "LH focus should not collapse to simple bass (seed=$seed, bar=${bar.number})",
                    bar.leftHand.size >= 2
                )
                assertTrue(
                    "LH focus must use a stable bass template despite conflicting constraint",
                    bar.leftHand.map { it.duration } in SteadyBassDurationTemplates
                )
            }
        }
    }

    @Test
    fun accidentalsFocusBiasesExistingChromaticApproachBehavior() {
        val seeds = (1L..80L).toList()

        val defaultAccidentals = seeds.sumOf { seed ->
            countAccidentals(RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.MEDIUM))
        }
        val focusedAccidentals = seeds.sumOf { seed ->
            countAccidentals(
                RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.MEDIUM,
                    focus = ExerciseFocus.ACCIDENTALS
                )
            )
        }

        assertTrue("Accidentals focus should increase altered RH notes", focusedAccidentals > defaultAccidentals)
    }

    @Test
    fun accidentalsFocusKeepsResolutionConstraint() {
        val seeds = (1L..120L).toList()

        Difficulty.entries.forEach { difficulty ->
            seeds.forEach { seed ->
                val exercise = RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = difficulty,
                    focus = ExerciseFocus.ACCIDENTALS
                )
                exercise.bars.forEach { bar ->
                    bar.rightHand.forEachIndexed { index, note ->
                        val pitch = note.pitch ?: return@forEachIndexed
                        if (pitch.alter == 0) return@forEachIndexed

                        assertTrue(
                            "Accidental note must not be last in bar (seed=$seed, bar=${bar.number})",
                            index + 1 < bar.rightHand.size
                        )
                        val nextPitch = requireNotNull(bar.rightHand[index + 1].pitch)
                        assertEquals(1, abs(toMidi(pitch) - toMidi(nextPitch)))
                        assertTrue("Resolution note must be chord tone", isChordTone(nextPitch, bar.chord))
                    }
                }
            }
        }
    }

    @Test
    fun smallLeapsFocusBiasesRightHandTowardControlledLeaps() {
        val seeds = (1L..80L).toList()

        val defaultSmallLeaps = seeds.sumOf { seed ->
            countSmallLeaps(RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.MEDIUM))
        }
        val focusedSmallLeaps = seeds.sumOf { seed ->
            countSmallLeaps(
                RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.MEDIUM,
                    focus = ExerciseFocus.SMALL_LEAPS
                )
            )
        }

        assertTrue("Small leaps focus should increase RH intervals of a third to fourth", focusedSmallLeaps > defaultSmallLeaps)
    }

    @Test
    fun accidentalDensityNoneSuppressesAccidentals() {
        val seeds = (1L..80L).toList()

        val accidentalCount = seeds.sumOf { seed ->
            countAccidentals(
                RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.HARD,
                    focus = ExerciseFocus.ACCIDENTALS,
                    constraints = GenerationConstraints(
                        accidentalDensity = GenerationAccidentalDensity.NONE
                    )
                )
            )
        }

        assertEquals(0, accidentalCount)
    }

    @Test
    fun accidentalsFocusWithMediumDensityCreatesVisibleAccidentals() {
        val seeds = (1L..24L).toList()

        val accidentalCount = seeds.sumOf { seed ->
            countAccidentals(
                RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.MEDIUM,
                    focus = ExerciseFocus.ACCIDENTALS,
                    constraints = GenerationConstraints(
                        accidentalDensity = GenerationAccidentalDensity.MEDIUM
                    )
                )
            )
        }

        assertTrue("Medium accidental density should create clearly visible altered RH notes", accidentalCount >= 24)
    }

    @Test
    fun maxLeapConstraintCapsRightHandMelodicMovement() {
        val cases = mapOf(
            GenerationMaxLeap.SECOND to 2,
            GenerationMaxLeap.THIRD to 4,
            GenerationMaxLeap.FOURTH to 5
        )

        cases.forEach { (maxLeap, maxSemitones) ->
            val seeds = (1L..40L).toList()
            seeds.forEach { seed ->
                val exercise = RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.HARD,
                    constraints = GenerationConstraints(maxLeap = maxLeap)
                )

                rightHandIntervals(exercise).forEach { interval ->
                    assertTrue(
                        "RH interval $interval exceeds $maxSemitones semitones for $maxLeap (seed=$seed)",
                        interval <= maxSemitones
                    )
                }
            }
        }
    }

    @Test
    fun rightHandMotionConstraintChangesIntervalProfile() {
        val seeds = (1L..120L).toList()

        val mostlyStepwiseIntervals = seeds.flatMap { seed ->
            rightHandIntervals(
                RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.MEDIUM,
                    constraints = GenerationConstraints(
                        rightHandMotion = GenerationRightHandMotion.MOSTLY_STEPWISE
                    )
                )
            )
        }
        val smallLeapIntervals = seeds.flatMap { seed ->
            rightHandIntervals(
                RuleBasedGenerator.generate(
                    seed = seed,
                    difficulty = Difficulty.MEDIUM,
                    constraints = GenerationConstraints(
                        rightHandMotion = GenerationRightHandMotion.STEPWISE_WITH_SMALL_LEAPS
                    )
                )
            )
        }

        val mostlyStepwiseCount = mostlyStepwiseIntervals.count { it <= 2 }
        val controlledLeapCount = smallLeapIntervals.count { it in 3..5 }

        assertTrue(
            "Mostly stepwise should produce more steps than the small-leap profile",
            mostlyStepwiseCount > smallLeapIntervals.count { it <= 2 }
        )
        assertTrue(
            "Small-leap profile should produce more thirds/fourths than mostly stepwise",
            controlledLeapCount > mostlyStepwiseIntervals.count { it in 3..5 }
        )
    }

    @Test
    fun leftHandTextureConstraintsSelectExpectedPatterns() {
        val simpleExercise = RuleBasedGenerator.generate(
            seed = 42L,
            difficulty = Difficulty.HARD,
            constraints = GenerationConstraints(
                leftHandTexture = GenerationLeftHandTexture.SIMPLE_BASS
            )
        )
        simpleExercise.bars.forEach { bar ->
            assertEquals(listOf(Duration.WHOLE), bar.leftHand.map { it.duration })
        }

        val steadyExercise = RuleBasedGenerator.generate(
            seed = 42L,
            difficulty = Difficulty.HARD,
            constraints = GenerationConstraints(
                leftHandTexture = GenerationLeftHandTexture.STEADY_BASS
            )
        )
        steadyExercise.bars.forEach { bar ->
            assertTrue(
                "Steady bass must have at least two attacks per bar",
                bar.leftHand.size >= 2
            )
            assertTrue(
                "Steady bass must use a stable duration template",
                bar.leftHand.map { it.duration } in SteadyBassDurationTemplates
            )
            val midis = bar.leftHand.map { toMidi(requireNotNull(it.pitch)) }
            assertStableBassPattern(midis)
        }
    }

    @Test
    fun constrainedExerciseStillWritesValidMusicXml() {
        val exercise = RuleBasedGenerator.generate(
            seed = 77L,
            difficulty = Difficulty.MEDIUM,
            focus = ExerciseFocus.ACCIDENTALS,
            constraints = GenerationConstraints(
                accidentalDensity = GenerationAccidentalDensity.MEDIUM,
                rightHandMotion = GenerationRightHandMotion.STEPWISE_WITH_SMALL_LEAPS,
                leftHandTexture = GenerationLeftHandTexture.STEADY_BASS,
                maxLeap = GenerationMaxLeap.FOURTH
            )
        )

        val xml = MusicXmlWriter.write(exercise)

        assertTrue(xml.startsWith("<?xml"))
        assertTrue(xml.contains("<score-partwise"))
        assertEquals(8, "<measure number=\"".toRegex().findAll(xml).count())
    }

    @Test
    fun defaultExerciseStaysLessChromaticAndJumpyThanFocusedPlans() {
        val seeds = (1L..48L).toList()

        val defaultExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.MEDIUM)
        }
        val accidentalExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(
                seed = seed,
                difficulty = Difficulty.MEDIUM,
                focus = ExerciseFocus.ACCIDENTALS,
                constraints = GenerationConstraints(
                    accidentalDensity = GenerationAccidentalDensity.MEDIUM
                )
            )
        }
        val smallLeapExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(
                seed = seed,
                difficulty = Difficulty.MEDIUM,
                focus = ExerciseFocus.SMALL_LEAPS
            )
        }

        assertTrue(
            "Default plans should stay less chromatic than accidentals practice",
            defaultExercises.sumOf { countAccidentals(it) } < accidentalExercises.sumOf { countAccidentals(it) }
        )
        assertTrue(
            "Default plans should have fewer thirds/fourths than small-leap practice",
            defaultExercises.sumOf { countSmallLeaps(it) } < smallLeapExercises.sumOf { countSmallLeaps(it) }
        )
    }

    @Test
    fun focusedPlansAreVisiblyDistinctFromReadAhead() {
        val seeds = (1L..48L).toList()

        val readAheadExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(seed = seed, difficulty = Difficulty.MEDIUM)
        }
        val leftHandExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(
                seed = seed,
                difficulty = Difficulty.MEDIUM,
                focus = ExerciseFocus.LEFT_HAND_STABILITY
            )
        }
        val smallLeapExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(
                seed = seed,
                difficulty = Difficulty.MEDIUM,
                focus = ExerciseFocus.SMALL_LEAPS
            )
        }
        val mostlyStepwiseExercises = seeds.map { seed ->
            RuleBasedGenerator.generate(
                seed = seed,
                difficulty = Difficulty.MEDIUM,
                constraints = GenerationConstraints(
                    rightHandMotion = GenerationRightHandMotion.MOSTLY_STEPWISE
                )
            )
        }

        assertTrue(
            "Left-hand focus should be more LH-active than read-ahead",
            leftHandExercises.sumOf { countLeftHandAttacks(it) } > readAheadExercises.sumOf { countLeftHandAttacks(it) }
        )
        assertTrue(
            "Small-leap focus should visibly prefer thirds/fourths over read-ahead",
            smallLeapExercises.sumOf { countSmallLeaps(it) } > readAheadExercises.sumOf { countSmallLeaps(it) }
        )
        assertTrue(
            "Small-leap focus should visibly prefer thirds/fourths over mostly-stepwise motion",
            smallLeapExercises.sumOf { countSmallLeaps(it) } > mostlyStepwiseExercises.sumOf { countSmallLeaps(it) }
        )
    }

    private fun isChordTone(pitch: Pitch, chord: ChordFunction): Boolean {
        val pitchClass = ((stepToPitchClass(pitch.step) + pitch.alter) % 12 + 12) % 12
        val tones = when (chord) {
            ChordFunction.I -> setOf(0, 4, 7)
            ChordFunction.II -> setOf(2, 5, 9)
            ChordFunction.III -> setOf(4, 7, 11)
            ChordFunction.IV -> setOf(5, 9, 0)
            ChordFunction.V -> setOf(7, 11, 2)
            ChordFunction.VI -> setOf(9, 0, 4)
        }
        return pitchClass in tones
    }

    private fun toMidi(pitch: Pitch): Int {
        val base = (pitch.octave + 1) * 12
        return base + stepToPitchClass(pitch.step) + pitch.alter
    }

    private fun countAccidentals(exercise: Exercise): Int {
        return exercise.bars
            .flatMap { it.rightHand }
            .count { note -> note.pitch?.alter?.let { it != 0 } == true }
    }

    private fun countRightHandEvents(exercise: Exercise): Int {
        return exercise.bars.sumOf { it.rightHand.size }
    }

    private fun rightHandSpan(exercise: Exercise): Int {
        val midis = exercise.bars
            .flatMap { it.rightHand }
            .map { toMidi(requireNotNull(it.pitch)) }
        if (midis.isEmpty()) return 0
        return midis.maxOrNull()!! - midis.minOrNull()!!
    }

    private fun countSmallLeaps(exercise: Exercise): Int {
        return exercise.bars.sumOf { bar ->
            bar.rightHand
                .zipWithNext { a, b -> abs(toMidi(requireNotNull(a.pitch)) - toMidi(requireNotNull(b.pitch))) }
                .count { it in 3..5 }
        }
    }

    private fun countLeftHandAttacks(exercise: Exercise): Int {
        return exercise.bars.sumOf { it.leftHand.size }
    }

    private fun assertStableBassPattern(midis: List<Int>) {
        val distinctPitchCount = midis.toSet().size
        assertTrue("LH stable pattern should use root-only or root/fifth motion", distinctPitchCount <= 2)
        if (midis.size >= 3) {
            assertEquals(midis[0], midis[2])
        }
        if (midis.size >= 4) {
            assertEquals(midis[1], midis[3])
        }
    }

    private fun rightHandIntervals(exercise: Exercise): List<Int> {
        return exercise.bars
            .flatMap { it.rightHand }
            .map { toMidi(requireNotNull(it.pitch)) }
            .zipWithNext { a, b -> abs(b - a) }
    }

    private fun stepToPitchClass(step: Step): Int {
        return when (step) {
            Step.C -> 0
            Step.D -> 2
            Step.E -> 4
            Step.F -> 5
            Step.G -> 7
            Step.A -> 9
            Step.B -> 11
        }
    }

    private companion object {
        val SteadyBassDurationTemplates = setOf(
            listOf(Duration.HALF, Duration.HALF),
            listOf(Duration.QUARTER, Duration.QUARTER, Duration.HALF),
            listOf(Duration.QUARTER, Duration.QUARTER, Duration.QUARTER, Duration.QUARTER)
        )
    }
}
