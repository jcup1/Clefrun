package com.clefrun.core

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
    fun rightHandBeats1And3AreChordTones() {
        val seeds = listOf(3L, 8L, 21L, 55L)
        seeds.forEach { seed ->
            val exercise = RuleBasedGenerator.generate(seed)
            exercise.bars.forEach { bar ->
                val beat1 = bar.rightHand.firstOrNull { it.beatStart == 1 && !it.isRest }
                val beat3 = bar.rightHand.firstOrNull { it.beatStart == 3 && !it.isRest }
                assertTrue("Missing RH note on beat 1 for bar ${bar.number}", beat1 != null)
                assertTrue("Missing RH note on beat 3 for bar ${bar.number}", beat3 != null)
                assertTrue(isChordTone(beat1!!, bar.chord))
                assertTrue(isChordTone(beat3!!, bar.chord))
            }
        }
    }

    private fun isChordTone(note: NoteEvent, chord: ChordFunction): Boolean {
        val pitchClass = stepToPitchClass(note.step ?: return false)
        val tones = when (chord) {
            ChordFunction.I -> setOf(0, 4, 7)
            ChordFunction.IV -> setOf(5, 9, 0)
            ChordFunction.V -> setOf(7, 11, 2)
            ChordFunction.VI -> setOf(9, 0, 4)
        }
        return pitchClass in tones
    }

    private fun stepToPitchClass(step: String): Int {
        return when (step) {
            "C" -> 0
            "D" -> 2
            "E" -> 4
            "F" -> 5
            "G" -> 7
            "A" -> 9
            "B" -> 11
            else -> error("Unexpected step $step")
        }
    }
}
