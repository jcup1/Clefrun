package com.clefrun.core

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedGeneratorTest {
    @Test
    fun generatedExerciseHas4Bars() {
        val exercise = RuleBasedGenerator.generate(seed = 42L)
        assertEquals(4, exercise.bars.size)
    }

    @Test
    fun eachBarSumsTo4Beats() {
        val seeds = listOf(1L, 2L, 42L, 100L)
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed)
            exercise.bars.forEach { bar ->
                assertEquals(4, bar.rightHand.sumOf { it.duration.beats })
                assertEquals(4, bar.leftHand.sumOf { it.duration.beats })
            }
        }
    }

    @Test
    fun accidentalRhNotesResolveBySemitoneIntoChordTone() {
        val seeds = (1L..150L).toList()
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed)
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
            val exercise = RuleBasedGenerator.generate(seed)
            val roots = exercise.bars.map { toMidi(requireNotNull(it.leftHand.first().pitch)) }
            var consecutiveRepeats = 0
            for (i in 1 until roots.size) {
                if (roots[i] == roots[i - 1]) consecutiveRepeats += 1
            }
            assertTrue(
                "LH root repeats too often (seed=$seed, roots=$roots)",
                consecutiveRepeats <= 1
            )
        }
    }

    @Test
    fun rightHandDoesNotContainLongRunsOfSamePitch() {
        val seeds = (1L..120L).toList()
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed)
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
        val exercise = RuleBasedGenerator.generate(seed = 77L)
        assertEquals(4, exercise.bars.size)
        exercise.bars.forEach { bar ->
            assertEquals(4, bar.rightHand.sumOf { it.duration.beats })
            assertEquals(4, bar.leftHand.sumOf { it.duration.beats })
        }

        val xml = MusicXmlWriter.write(exercise)
        assertTrue(xml.startsWith("<?xml"))
        assertTrue(xml.contains("<score-partwise"))
        assertTrue(xml.contains("<part id=\"P1\">"))
        assertEquals(4, "<measure number=\"".toRegex().findAll(xml).count())
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
