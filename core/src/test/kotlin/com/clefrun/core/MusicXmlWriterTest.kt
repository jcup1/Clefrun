package com.clefrun.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicXmlWriterTest {
    @Test
    fun sharpNaturalNaturalInOneMeasureSuppressesRepeatedNatural() {
        val xml = MusicXmlWriter.write(
            exerciseWithBars(
                listOf(
                    bar(
                        number = 1,
                        rightHand = listOf(
                            note(Step.A, 1, 4, Duration.QUARTER, beatStart = 1, staff = 1),
                            note(Step.A, 0, 4, Duration.QUARTER, beatStart = 2, staff = 1),
                            note(Step.A, 0, 4, Duration.HALF, beatStart = 3, staff = 1)
                        )
                    )
                )
            )
        )

        assertEquals(1, "<accidental>sharp</accidental>".toRegex().findAll(xml).count())
        assertEquals(1, "<accidental>natural</accidental>".toRegex().findAll(xml).count())
    }

    @Test
    fun repeatedSharpInOneMeasureDoesNotRepeatAccidental() {
        val xml = MusicXmlWriter.write(
            exerciseWithBars(
                listOf(
                    bar(
                        number = 1,
                        rightHand = listOf(
                            note(Step.F, 1, 4, Duration.QUARTER, beatStart = 1, staff = 1),
                            note(Step.F, 1, 4, Duration.QUARTER, beatStart = 2, staff = 1),
                            note(Step.F, 1, 4, Duration.HALF, beatStart = 3, staff = 1)
                        )
                    )
                )
            )
        )

        assertEquals(1, "<accidental>sharp</accidental>".toRegex().findAll(xml).count())
        assertFalse(xml.contains("<accidental>natural</accidental>"))
    }

    @Test
    fun accidentalStateResetsAcrossMeasures() {
        val xml = MusicXmlWriter.write(
            exerciseWithBars(
                listOf(
                    bar(
                        number = 1,
                        rightHand = listOf(
                            note(Step.F, 1, 4, Duration.WHOLE, beatStart = 1, staff = 1)
                        )
                    ),
                    bar(
                        number = 2,
                        rightHand = listOf(
                            note(Step.F, 1, 4, Duration.WHOLE, beatStart = 1, staff = 1)
                        )
                    )
                )
            )
        )

        assertEquals(2, "<accidental>sharp</accidental>".toRegex().findAll(xml).count())
    }

    @Test
    fun accidentalStateIsIndependentAcrossOctaves() {
        val xml = MusicXmlWriter.write(
            exerciseWithBars(
                listOf(
                    bar(
                        number = 1,
                        rightHand = listOf(
                            note(Step.A, 1, 4, Duration.QUARTER, beatStart = 1, staff = 1),
                            note(Step.A, 1, 5, Duration.QUARTER, beatStart = 2, staff = 1),
                            note(Step.A, 1, 4, Duration.HALF, beatStart = 3, staff = 1)
                        )
                    )
                )
            )
        )

        assertEquals(2, "<accidental>sharp</accidental>".toRegex().findAll(xml).count())
    }

    private fun exerciseWithBars(bars: List<Bar>): Exercise {
        return Exercise(bars = bars)
    }

    private fun bar(number: Int, rightHand: List<NoteEvent>): Bar {
        return Bar(
            number = number,
            chord = ChordFunction.I,
            rightHand = rightHand,
            leftHand = listOf(
                note(Step.C, 0, 3, Duration.WHOLE, beatStart = 1, staff = 2)
            )
        )
    }

    private fun note(
        step: Step,
        alter: Int,
        octave: Int,
        duration: Duration,
        beatStart: Int,
        staff: Int
    ): NoteEvent {
        return NoteEvent(
            pitch = Pitch(step = step, alter = alter, octave = octave),
            duration = duration,
            staff = staff,
            voice = if (staff == 1) 1 else 2,
            beatStart = beatStart
        )
    }
}
