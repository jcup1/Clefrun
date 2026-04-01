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
}
