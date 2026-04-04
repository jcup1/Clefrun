package com.clefrun.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TechnicalPracticeGeneratorTest {
    @Test
    fun generatesCombinedPracticePageWithExpectedSections() {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = PracticeTonic.F
        )

        assertEquals(15, exercise.bars.size)
        exercise.bars.forEach { bar ->
            assertEquals(StaffLayout.GRAND_STAFF, bar.staffLayout)
            assertTrue(bar.leftHand.isNotEmpty())
        }
        assertEquals("Scale", exercise.bars[0].sectionLabel)
        assertEquals("Arpeggio", exercise.bars[8].sectionLabel)
        assertEquals(null, exercise.bars[12].sectionLabel)
        assertEquals("Cadence", exercise.bars[13].sectionLabel)
        assertTrue(exercise.bars[13].startsNewSystem)
        assertEquals(150, exercise.bars[13].systemDistance)
    }

    @Test
    fun exposesCuratedMajorSubsetOnly() {
        assertEquals(setOf(PracticeMode.MAJOR), supportedTechnicalPracticeModes())
        assertEquals(
            setOf(PracticeTonic.C, PracticeTonic.F, PracticeTonic.G),
            supportedTechnicalPracticeTonics(PracticeMode.MAJOR)
        )
        assertTrue(isTechnicalPracticeSupported(PracticeMode.MAJOR, PracticeTonic.C))
        assertFalse(isTechnicalPracticeSupported(PracticeMode.NATURAL_MINOR, PracticeTonic.C))
        assertFalse(isTechnicalPracticeSupported(PracticeMode.MAJOR, PracticeTonic.D))
    }

    @Test
    fun curatedScaleFingeringsAreCorrectForSupportedKeys() {
        assertScaleFingerings(
            tonic = PracticeTonic.C,
            expectedRightAscending = listOf(1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 5),
            expectedRightDescending = listOf(4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
            expectedLeftAscending = listOf(5, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
            expectedLeftDescending = listOf(2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4)
        )
        assertScaleFingerings(
            tonic = PracticeTonic.F,
            expectedRightAscending = listOf(1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4),
            expectedRightDescending = listOf(3, 2, 1, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1),
            expectedLeftAscending = listOf(5, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
            expectedLeftDescending = listOf(2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4)
        )
        assertScaleFingerings(
            tonic = PracticeTonic.G,
            expectedRightAscending = listOf(1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 5),
            expectedRightDescending = listOf(4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
            expectedLeftAscending = listOf(5, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
            expectedLeftDescending = listOf(2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4)
        )
    }

    @Test
    fun scaleIsTwoOctavesFingeredInBothHandsAndKeepsOctaveContinuity() {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = PracticeTonic.F
        )

        val scaleBars = exercise.bars.take(8)
        val ascendingRight = scaleBars.take(4).flatMap { it.rightHand }.filterNot { it.isRest }
        val ascendingLeft = scaleBars.take(4).flatMap { it.leftHand }.filterNot { it.isRest }
        val descendingRight = scaleBars.drop(4).flatMap { it.rightHand }.filterNot { it.isRest }
        val descendingLeft = scaleBars.drop(4).flatMap { it.leftHand }.filterNot { it.isRest }

        assertEquals(15, ascendingRight.size)
        assertEquals(15, ascendingLeft.size)
        assertEquals(14, descendingRight.size)
        assertEquals(14, descendingLeft.size)
        assertTrue(scaleBars.flatMap { it.rightHand + it.leftHand }.all { it.duration == Duration.QUARTER })
        assertTrue(ascendingRight.all { it.staff == 1 && it.fingerings.size == 1 })
        assertTrue(ascendingLeft.all { it.staff == 2 && it.fingerings.size == 1 })
        assertTrue(descendingRight.all { it.staff == 1 && it.fingerings.size == 1 })
        assertTrue(descendingLeft.all { it.staff == 2 && it.fingerings.size == 1 })
        assertEquals(
            listOf(
                Pitch(Step.F, 0, 4),
                Pitch(Step.G, 0, 4),
                Pitch(Step.A, 0, 4),
                Pitch(Step.B, -1, 4),
                Pitch(Step.C, 0, 5),
                Pitch(Step.D, 0, 5),
                Pitch(Step.E, 0, 5),
                Pitch(Step.F, 0, 5)
            ),
            ascendingRight.take(8).map { it.pitch }
        )
        assertEquals(
            listOf(1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4),
            ascendingRight.map { it.fingerings.single() }
        )
        assertEquals(
            listOf(3, 2, 1, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1),
            descendingRight.map { it.fingerings.single() }
        )
        assertEquals(Pitch(step = Step.F, alter = 0, octave = 6), ascendingRight.last().pitch)
        assertEquals(Pitch(step = Step.E, alter = 0, octave = 6), descendingRight.first().pitch)
        assertEquals(Pitch(step = Step.F, alter = 0, octave = 2), ascendingLeft.first().pitch)
        assertEquals(Pitch(step = Step.C, alter = 0, octave = 3), ascendingLeft[4].pitch)
    }

    @Test
    fun curatedArpeggioFingeringsAreCorrectForSupportedKeys() {
        assertArpeggioFingerings(
            tonic = PracticeTonic.C,
            expectedRightAscending = listOf(1, 2, 3, 1, 2, 3, 5),
            expectedRightDescending = listOf(3, 2, 1, 3, 2, 1),
            expectedLeftAscending = listOf(5, 4, 2, 1, 4, 2, 1),
            expectedLeftDescending = listOf(2, 4, 1, 2, 4, 5)
        )
        assertArpeggioFingerings(
            tonic = PracticeTonic.F,
            expectedRightAscending = listOf(1, 2, 4, 1, 2, 3, 5),
            expectedRightDescending = listOf(3, 2, 1, 4, 2, 1),
            expectedLeftAscending = listOf(5, 3, 2, 1, 4, 2, 1),
            expectedLeftDescending = listOf(2, 4, 1, 2, 3, 5)
        )
        assertArpeggioFingerings(
            tonic = PracticeTonic.G,
            expectedRightAscending = listOf(1, 2, 3, 1, 2, 3, 5),
            expectedRightDescending = listOf(3, 2, 1, 3, 2, 1),
            expectedLeftAscending = listOf(5, 4, 2, 1, 4, 2, 1),
            expectedLeftDescending = listOf(2, 4, 1, 2, 4, 5)
        )
    }

    @Test
    fun arpeggioUsesTwoHandsTwoOctavesAndCuratedFingerings() {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = PracticeTonic.F
        )

        val arpeggioBars = exercise.bars.drop(8).take(4)
        val ascendingNotes = arpeggioBars.take(2)
        val descendingNotes = arpeggioBars.drop(2)
        val ascendingRight = ascendingNotes.flatMap { it.rightHand }.filterNot { it.isRest }
        val ascendingLeft = ascendingNotes.flatMap { it.leftHand }.filterNot { it.isRest }
        val descendingRight = descendingNotes.flatMap { it.rightHand }.filterNot { it.isRest }
        val descendingLeft = descendingNotes.flatMap { it.leftHand }.filterNot { it.isRest }

        assertEquals(7, ascendingRight.size)
        assertEquals(7, ascendingLeft.size)
        assertEquals(6, descendingRight.size)
        assertEquals(6, descendingLeft.size)
        assertTrue(arpeggioBars.flatMap { it.rightHand + it.leftHand }.all { it.duration == Duration.QUARTER })
        assertTrue(ascendingRight.all { it.staff == 1 && it.fingerings.size == 1 })
        assertTrue(ascendingLeft.all { it.staff == 2 && it.fingerings.size == 1 })
        assertEquals(Pitch(Step.C, 0, 5), ascendingRight[2].pitch)
        assertEquals(Pitch(Step.F, 0, 4), ascendingLeft.last().pitch)
        assertEquals(Pitch(Step.C, 0, 6), descendingRight.first().pitch)
        assertEquals(ascendingRight.last().pitch, Pitch(Step.F, 0, 6))
        assertEquals(Pitch(Step.A, 0, 5), descendingRight[1].pitch)
        assertFalse(descendingRight.first().pitch == ascendingRight.last().pitch)
    }

    @Test
    fun cadenceUsesRhChordsLhSingleNotesWithoutFingerings() {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = PracticeTonic.C
        )

        val cadenceBars = exercise.bars.drop(13)
        assertTrue(cadenceBars.flatMap { it.rightHand }.all { it.additionalPitches.isNotEmpty() })
        assertTrue(cadenceBars.flatMap { it.leftHand }.all { it.additionalPitches.isEmpty() })
        assertTrue(cadenceBars.flatMap { it.rightHand }.all { it.fingerings.isEmpty() })
        assertTrue(cadenceBars.flatMap { it.leftHand }.all { it.fingerings.isEmpty() })
    }

    @Test
    fun writerEmitsSectionLabelsAndFingeringsForTechnicalPractice() {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = PracticeTonic.F
        )
        val xml = MusicXmlWriter.write(exercise)
        val expectedFingeringCount = exercise.bars
            .take(12)
            .sumOf { bar -> bar.rightHand.sumOf { it.pitchCount } + bar.leftHand.sumOf { it.pitchCount } }
        val hiddenRestCount = exercise.bars
            .flatMap { it.rightHand + it.leftHand }
            .count { it.isRest && !it.printObject }
        val cadenceMeasures = xml.substringAfter("<measure number=\"14\"").substringAfter(">")

        assertTrue(xml.contains("<words default-x=\"-72\" font-style=\"italic\">Scale</words>"))
        assertTrue(xml.contains("<words default-x=\"-36\" font-style=\"italic\">Arpeggio</words>"))
        assertTrue(xml.contains("<words default-x=\"-36\" font-style=\"italic\">Cadence</words>"))
        assertEquals(expectedFingeringCount, xml.split("<fingering>").size - 1)
        assertTrue(xml.contains("<staves>2</staves>"))
        assertEquals(hiddenRestCount, xml.split("<rest/>").size - 1)
        assertTrue(xml.contains("<time print-object=\"no\" symbol=\"none\">"))
        assertTrue(xml.contains("<bottom-margin>220</bottom-margin>"))
        assertTrue(cadenceMeasures.contains("<print new-system=\"yes\">"))
        assertTrue(cadenceMeasures.contains("<system-distance>150</system-distance>"))
    }

    private fun assertScaleFingerings(
        tonic: PracticeTonic,
        expectedRightAscending: List<Int>,
        expectedRightDescending: List<Int>,
        expectedLeftAscending: List<Int>,
        expectedLeftDescending: List<Int>
    ) {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = tonic
        )
        val scaleBars = exercise.bars.take(8)

        val ascendingRight = scaleBars.take(4).flatMap { it.rightHand }.filterNot { it.isRest }
        val ascendingLeft = scaleBars.take(4).flatMap { it.leftHand }.filterNot { it.isRest }
        val descendingRight = scaleBars.drop(4).flatMap { it.rightHand }.filterNot { it.isRest }
        val descendingLeft = scaleBars.drop(4).flatMap { it.leftHand }.filterNot { it.isRest }

        assertEquals(expectedRightAscending, ascendingRight.map { it.fingerings.single() })
        assertEquals(expectedRightDescending, descendingRight.map { it.fingerings.single() })
        assertEquals(expectedLeftAscending, ascendingLeft.map { it.fingerings.single() })
        assertEquals(expectedLeftDescending, descendingLeft.map { it.fingerings.single() })
    }

    private fun assertArpeggioFingerings(
        tonic: PracticeTonic,
        expectedRightAscending: List<Int>,
        expectedRightDescending: List<Int>,
        expectedLeftAscending: List<Int>,
        expectedLeftDescending: List<Int>
    ) {
        val exercise = TechnicalPracticeGenerator.generate(
            mode = PracticeMode.MAJOR,
            tonic = tonic
        )
        val arpeggioBars = exercise.bars.drop(8).take(4)
        val ascendingNotes = arpeggioBars.take(2)
        val descendingNotes = arpeggioBars.drop(2)

        val ascendingRight = ascendingNotes.flatMap { it.rightHand }.filterNot { it.isRest }
        val ascendingLeft = ascendingNotes.flatMap { it.leftHand }.filterNot { it.isRest }
        val descendingRight = descendingNotes.flatMap { it.rightHand }.filterNot { it.isRest }
        val descendingLeft = descendingNotes.flatMap { it.leftHand }.filterNot { it.isRest }

        assertEquals(expectedRightAscending, ascendingRight.map { it.fingerings.single() })
        assertEquals(expectedRightDescending, descendingRight.map { it.fingerings.single() })
        assertEquals(expectedLeftAscending, ascendingLeft.map { it.fingerings.single() })
        assertEquals(expectedLeftDescending, descendingLeft.map { it.fingerings.single() })
    }
}
