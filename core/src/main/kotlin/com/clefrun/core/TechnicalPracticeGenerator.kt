package com.clefrun.core

object TechnicalPracticeGenerator {
    fun generate(
        mode: PracticeMode,
        tonic: PracticeTonic
    ): Exercise {
        val template = technicalPracticeTemplate(mode, tonic)
        val bars = mutableListOf<Bar>()
        var measureNumber = 1

        sectionBars(
            line = template.scaleAscending,
            chunkSizes = listOf(4, 4, 4, 3),
            sectionLabel = "Scale",
            sectionLabelOffsetX = -72
        ).forEach { bar -> bars += bar.copy(number = measureNumber++) }
        sectionBars(
            line = template.scaleDescending,
            chunkSizes = listOf(4, 4, 4, 2)
        ).forEach { bar -> bars += bar.copy(number = measureNumber++) }
        sectionBars(
            line = template.arpeggioAscending,
            chunkSizes = listOf(4, 3),
            sectionLabel = "Arpeggio",
            sectionLabelOffsetX = -36
        ).forEach { bar -> bars += bar.copy(number = measureNumber++) }
        sectionBars(
            line = template.arpeggioDescending,
            chunkSizes = listOf(4, 2)
        ).forEach { bar -> bars += bar.copy(number = measureNumber++) }
        bars += spacerBar(number = measureNumber++)
        cadenceBars(
            template = template.cadence,
            sectionLabel = "Cadence",
            sectionLabelOffsetX = -36
        ).forEach { bar -> bars += bar.copy(number = measureNumber++) }

        return Exercise(
            bars = bars,
            keyFifths = template.keyFifths,
            divisions = 2,
            showTimeSignature = false
        )
    }
}

fun isTechnicalPracticeSupported(
    mode: PracticeMode,
    tonic: PracticeTonic
): Boolean = TECHNICAL_PRACTICE_TEMPLATES.containsKey(TemplateKey(mode, tonic))

fun supportedTechnicalPracticeModes(): Set<PracticeMode> {
    return TECHNICAL_PRACTICE_TEMPLATES.keys.mapTo(linkedSetOf()) { it.mode }
}

fun supportedTechnicalPracticeTonics(mode: PracticeMode): Set<PracticeTonic> {
    return TECHNICAL_PRACTICE_TEMPLATES.keys
        .filter { it.mode == mode }
        .mapTo(linkedSetOf()) { it.tonic }
}

private fun technicalPracticeTemplate(
    mode: PracticeMode,
    tonic: PracticeTonic
): TechnicalPracticeTemplate {
    return TECHNICAL_PRACTICE_TEMPLATES[TemplateKey(mode, tonic)]
        ?: error("Unsupported technical practice template: $mode $tonic")
}

private fun sectionBars(
    line: PracticeLineTemplate,
    chunkSizes: List<Int>,
    sectionLabel: String? = null,
    sectionLabelOffsetX: Int = 0
): List<Bar> {
    val rightHandChunks = chunkPitches(line.rightHand, chunkSizes)
    val leftHandChunks = chunkPitches(line.leftHand, chunkSizes)
    val rightHandFingeringChunks = chunkFingerings(line.rightHandFingerings, chunkSizes)
    val leftHandFingeringChunks = chunkFingerings(line.leftHandFingerings, chunkSizes)

    return chunkSizes.indices.map { index ->
        Bar(
            number = 0,
            chord = ChordFunction.I,
            rightHand = buildQuarterBar(
                pitches = rightHandChunks[index],
                fingerings = rightHandFingeringChunks.getOrElse(index) { emptyList() },
                staff = 1,
                voice = 1
            ),
            leftHand = buildQuarterBar(
                pitches = leftHandChunks[index],
                fingerings = leftHandFingeringChunks.getOrElse(index) { emptyList() },
                staff = 2,
                voice = 2
            ),
            staffLayout = StaffLayout.GRAND_STAFF,
            sectionLabel = if (index == 0) sectionLabel else null,
            sectionLabelOffsetX = if (index == 0) sectionLabelOffsetX else 0
        )
    }
}

private fun cadenceBars(
    template: CadenceTemplate,
    sectionLabel: String,
    sectionLabelOffsetX: Int
): List<Bar> {
    return listOf(
        Bar(
            number = 0,
            chord = ChordFunction.I,
            rightHand = template.firstBarRightHand,
            leftHand = template.firstBarLeftHand,
            staffLayout = StaffLayout.GRAND_STAFF,
            sectionLabel = sectionLabel,
            sectionLabelOffsetX = sectionLabelOffsetX
        ),
        Bar(
            number = 0,
            chord = ChordFunction.I,
            rightHand = template.secondBarRightHand,
            leftHand = template.secondBarLeftHand,
            staffLayout = StaffLayout.GRAND_STAFF
        )
    )
}

private fun spacerBar(number: Int): Bar {
    return Bar(
        number = number,
        chord = ChordFunction.I,
        rightHand = listOf(
            NoteEvent(
                pitch = null,
                duration = Duration.WHOLE,
                staff = 1,
                voice = 1,
                beatStart = 1,
                isRest = true,
                printObject = false
            )
        ),
        leftHand = listOf(
            NoteEvent(
                pitch = null,
                duration = Duration.WHOLE,
                staff = 2,
                voice = 2,
                beatStart = 1,
                isRest = true,
                printObject = false
            )
        ),
        staffLayout = StaffLayout.GRAND_STAFF
    )
}

private fun buildQuarterBar(
    pitches: List<Pitch>,
    fingerings: List<Int> = emptyList(),
    staff: Int,
    voice: Int
): List<NoteEvent> {
    val notes = pitches.mapIndexed { index, pitch ->
        NoteEvent(
            pitch = pitch,
            duration = Duration.QUARTER,
            staff = staff,
            voice = voice,
            beatStart = index + 1,
            fingerings = if (fingerings.isEmpty()) emptyList() else listOf(fingerings[index])
        )
    }.toMutableList()

    repeat(4 - pitches.size) { restIndex ->
        notes += NoteEvent(
            pitch = null,
            duration = Duration.QUARTER,
            staff = staff,
            voice = voice,
            beatStart = pitches.size + restIndex + 1,
            isRest = true,
            printObject = false
        )
    }

    return notes
}

private fun buildChordEvents(
    specs: List<ChordEventTemplate>,
    staff: Int,
    voice: Int
): List<NoteEvent> {
    var beatStart = 1
    return specs.map { spec ->
        NoteEvent(
            pitch = spec.pitches.first(),
            additionalPitches = spec.pitches.drop(1),
            duration = spec.duration,
            staff = staff,
            voice = voice,
            beatStart = beatStart
        ).also {
            beatStart += spec.duration.beats
        }
    }
}

private fun buildBassEvents(
    specs: List<BassEventTemplate>,
    staff: Int,
    voice: Int
): List<NoteEvent> {
    var beatStart = 1
    return specs.map { spec ->
        NoteEvent(
            pitch = spec.pitch,
            duration = spec.duration,
            staff = staff,
            voice = voice,
            beatStart = beatStart
        ).also {
            beatStart += spec.duration.beats
        }
    }
}

private fun chunkPitches(
    pitches: List<Pitch>,
    chunkSizes: List<Int>
): List<List<Pitch>> {
    var index = 0
    return chunkSizes.map { size ->
        pitches.subList(index, index + size).also {
            index += size
        }
    }
}

private fun chunkFingerings(
    fingerings: List<Int>,
    chunkSizes: List<Int>
): List<List<Int>> {
    if (fingerings.isEmpty()) return chunkSizes.map { emptyList() }
    var index = 0
    return chunkSizes.map { size ->
        fingerings.subList(index, index + size).also {
            index += size
        }
    }
}

private data class TemplateKey(
    val mode: PracticeMode,
    val tonic: PracticeTonic
)

private data class TechnicalPracticeTemplate(
    val keyFifths: Int,
    val scaleAscending: PracticeLineTemplate,
    val scaleDescending: PracticeLineTemplate,
    val arpeggioAscending: PracticeLineTemplate,
    val arpeggioDescending: PracticeLineTemplate,
    val cadence: CadenceTemplate
)

private data class PracticeLineTemplate(
    val rightHand: List<Pitch>,
    val leftHand: List<Pitch>,
    val rightHandFingerings: List<Int> = emptyList(),
    val leftHandFingerings: List<Int> = emptyList()
)

private data class CadenceTemplate(
    val firstBarRightHand: List<NoteEvent>,
    val firstBarLeftHand: List<NoteEvent>,
    val secondBarRightHand: List<NoteEvent>,
    val secondBarLeftHand: List<NoteEvent>
)

private data class ChordEventTemplate(
    val pitches: List<Pitch>,
    val duration: Duration
)

private data class BassEventTemplate(
    val pitch: Pitch,
    val duration: Duration
)

private val TECHNICAL_PRACTICE_TEMPLATES = mapOf(
    TemplateKey(PracticeMode.MAJOR, PracticeTonic.C) to majorTemplate(
        keyFifths = 0,
        scaleAscendingRight = listOf(
            p(Step.C, 4), p(Step.D, 4), p(Step.E, 4), p(Step.F, 4),
            p(Step.G, 4), p(Step.A, 4), p(Step.B, 4), p(Step.C, 5),
            p(Step.D, 5), p(Step.E, 5), p(Step.F, 5), p(Step.G, 5),
            p(Step.A, 5), p(Step.B, 5), p(Step.C, 6)
        ),
        scaleAscendingLeft = listOf(
            p(Step.C, 2), p(Step.D, 2), p(Step.E, 2), p(Step.F, 2),
            p(Step.G, 2), p(Step.A, 2), p(Step.B, 2), p(Step.C, 3),
            p(Step.D, 3), p(Step.E, 3), p(Step.F, 3), p(Step.G, 3),
            p(Step.A, 3), p(Step.B, 3), p(Step.C, 4)
        ),
        scaleDescendingRight = listOf(
            p(Step.B, 5), p(Step.A, 5), p(Step.G, 5), p(Step.F, 5),
            p(Step.E, 5), p(Step.D, 5), p(Step.C, 5), p(Step.B, 4),
            p(Step.A, 4), p(Step.G, 4), p(Step.F, 4), p(Step.E, 4),
            p(Step.D, 4), p(Step.C, 4)
        ),
        scaleDescendingLeft = listOf(
            p(Step.B, 3), p(Step.A, 3), p(Step.G, 3), p(Step.F, 3),
            p(Step.E, 3), p(Step.D, 3), p(Step.C, 3), p(Step.B, 2),
            p(Step.A, 2), p(Step.G, 2), p(Step.F, 2), p(Step.E, 2),
            p(Step.D, 2), p(Step.C, 2)
        ),
        scaleAscendingFingering = listOf(1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 5),
        scaleDescendingFingering = listOf(4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
        scaleAscendingLeftFingering = listOf(5, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
        scaleDescendingLeftFingering = listOf(2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4),
        arpeggioAscendingRightFingering = listOf(1, 2, 3, 1, 2, 3, 5),
        arpeggioAscendingLeftFingering = listOf(5, 4, 2, 1, 4, 2, 1),
        arpeggioDescendingRightFingering = listOf(3, 2, 1, 3, 2, 1),
        arpeggioDescendingLeftFingering = listOf(2, 4, 1, 2, 4, 5),
        arpeggioAscendingRight = listOf(p(Step.C, 4), p(Step.E, 4), p(Step.G, 4), p(Step.C, 5), p(Step.E, 5), p(Step.G, 5), p(Step.C, 6)),
        arpeggioAscendingLeft = listOf(p(Step.C, 2), p(Step.E, 2), p(Step.G, 2), p(Step.C, 3), p(Step.E, 3), p(Step.G, 3), p(Step.C, 4)),
        arpeggioDescendingRight = listOf(p(Step.G, 5), p(Step.E, 5), p(Step.C, 5), p(Step.G, 4), p(Step.E, 4), p(Step.C, 4)),
        arpeggioDescendingLeft = listOf(p(Step.G, 3), p(Step.E, 3), p(Step.C, 3), p(Step.G, 2), p(Step.E, 2), p(Step.C, 2)),
        cadenceFirstBarRight = listOf(
            chord(Duration.HALF, p(Step.E, 4), p(Step.G, 4), p(Step.C, 5)),
            chord(Duration.HALF, p(Step.F, 4), p(Step.A, 4), p(Step.C, 5))
        ),
        cadenceFirstBarLeft = listOf(
            bass(Duration.HALF, p(Step.C, 3)),
            bass(Duration.HALF, p(Step.F, 3))
        ),
        cadenceSecondBarRight = listOf(
            chord(Duration.QUARTER, p(Step.G, 4), p(Step.C, 5), p(Step.E, 5)),
            chord(Duration.QUARTER, p(Step.G, 4), p(Step.B, 4), p(Step.D, 5)),
            chord(Duration.HALF, p(Step.C, 4), p(Step.E, 4), p(Step.G, 4))
        ),
        cadenceSecondBarLeft = listOf(
            bass(Duration.QUARTER, p(Step.G, 2)),
            bass(Duration.QUARTER, p(Step.G, 2)),
            bass(Duration.HALF, p(Step.C, 3))
        )
    ),
    TemplateKey(PracticeMode.MAJOR, PracticeTonic.F) to majorTemplate(
        keyFifths = -1,
        scaleAscendingRight = listOf(
            p(Step.F, 4), p(Step.G, 4), p(Step.A, 4), p(Step.B, 4, -1),
            p(Step.C, 5), p(Step.D, 5), p(Step.E, 5), p(Step.F, 5),
            p(Step.G, 5), p(Step.A, 5), p(Step.B, 5, -1), p(Step.C, 6),
            p(Step.D, 6), p(Step.E, 6), p(Step.F, 6)
        ),
        scaleAscendingLeft = listOf(
            p(Step.F, 2), p(Step.G, 2), p(Step.A, 2), p(Step.B, 2, -1),
            p(Step.C, 3), p(Step.D, 3), p(Step.E, 3), p(Step.F, 3),
            p(Step.G, 3), p(Step.A, 3), p(Step.B, 3, -1), p(Step.C, 4),
            p(Step.D, 4), p(Step.E, 4), p(Step.F, 4)
        ),
        scaleDescendingRight = listOf(
            p(Step.E, 6), p(Step.D, 6), p(Step.C, 6), p(Step.B, 5, -1),
            p(Step.A, 5), p(Step.G, 5), p(Step.F, 5), p(Step.E, 5),
            p(Step.D, 5), p(Step.C, 5), p(Step.B, 4, -1), p(Step.A, 4),
            p(Step.G, 4), p(Step.F, 4)
        ),
        scaleDescendingLeft = listOf(
            p(Step.E, 4), p(Step.D, 4), p(Step.C, 4), p(Step.B, 3, -1),
            p(Step.A, 3), p(Step.G, 3), p(Step.F, 3), p(Step.E, 3),
            p(Step.D, 3), p(Step.C, 3), p(Step.B, 2, -1), p(Step.A, 2),
            p(Step.G, 2), p(Step.F, 2)
        ),
        scaleAscendingFingering = listOf(1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4),
        scaleDescendingFingering = listOf(3, 2, 1, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1),
        scaleAscendingLeftFingering = listOf(5, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
        scaleDescendingLeftFingering = listOf(2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4),
        arpeggioAscendingRightFingering = listOf(1, 2, 4, 1, 2, 3, 5),
        arpeggioAscendingLeftFingering = listOf(5, 3, 2, 1, 4, 2, 1),
        arpeggioDescendingRightFingering = listOf(3, 2, 1, 4, 2, 1),
        arpeggioDescendingLeftFingering = listOf(2, 4, 1, 2, 3, 5),
        arpeggioAscendingRight = listOf(p(Step.F, 4), p(Step.A, 4), p(Step.C, 5), p(Step.F, 5), p(Step.A, 5), p(Step.C, 6), p(Step.F, 6)),
        arpeggioAscendingLeft = listOf(p(Step.F, 2), p(Step.A, 2), p(Step.C, 3), p(Step.F, 3), p(Step.A, 3), p(Step.C, 4), p(Step.F, 4)),
        arpeggioDescendingRight = listOf(p(Step.C, 6), p(Step.A, 5), p(Step.F, 5), p(Step.C, 5), p(Step.A, 4), p(Step.F, 4)),
        arpeggioDescendingLeft = listOf(p(Step.C, 4), p(Step.A, 3), p(Step.F, 3), p(Step.C, 3), p(Step.A, 2), p(Step.F, 2)),
        cadenceFirstBarRight = listOf(
            chord(Duration.HALF, p(Step.A, 4), p(Step.C, 5), p(Step.F, 5)),
            chord(Duration.HALF, p(Step.B, 4, -1), p(Step.D, 5), p(Step.F, 5))
        ),
        cadenceFirstBarLeft = listOf(
            bass(Duration.HALF, p(Step.F, 3)),
            bass(Duration.HALF, p(Step.B, 2, -1))
        ),
        cadenceSecondBarRight = listOf(
            chord(Duration.QUARTER, p(Step.C, 5), p(Step.F, 5), p(Step.A, 5)),
            chord(Duration.QUARTER, p(Step.C, 5), p(Step.E, 5), p(Step.G, 5)),
            chord(Duration.HALF, p(Step.F, 4), p(Step.A, 4), p(Step.C, 5))
        ),
        cadenceSecondBarLeft = listOf(
            bass(Duration.QUARTER, p(Step.C, 3)),
            bass(Duration.QUARTER, p(Step.C, 3)),
            bass(Duration.HALF, p(Step.F, 2))
        )
    ),
    TemplateKey(PracticeMode.MAJOR, PracticeTonic.G) to majorTemplate(
        keyFifths = 1,
        scaleAscendingRight = listOf(
            p(Step.G, 4), p(Step.A, 4), p(Step.B, 4), p(Step.C, 5),
            p(Step.D, 5), p(Step.E, 5), p(Step.F, 5, 1), p(Step.G, 5),
            p(Step.A, 5), p(Step.B, 5), p(Step.C, 6), p(Step.D, 6),
            p(Step.E, 6), p(Step.F, 6, 1), p(Step.G, 6)
        ),
        scaleAscendingLeft = listOf(
            p(Step.G, 2), p(Step.A, 2), p(Step.B, 2), p(Step.C, 3),
            p(Step.D, 3), p(Step.E, 3), p(Step.F, 3, 1), p(Step.G, 3),
            p(Step.A, 3), p(Step.B, 3), p(Step.C, 4), p(Step.D, 4),
            p(Step.E, 4), p(Step.F, 4, 1), p(Step.G, 4)
        ),
        scaleDescendingRight = listOf(
            p(Step.F, 6, 1), p(Step.E, 6), p(Step.D, 6), p(Step.C, 6),
            p(Step.B, 5), p(Step.A, 5), p(Step.G, 5), p(Step.F, 5, 1),
            p(Step.E, 5), p(Step.D, 5), p(Step.C, 5), p(Step.B, 4),
            p(Step.A, 4), p(Step.G, 4)
        ),
        scaleDescendingLeft = listOf(
            p(Step.F, 4, 1), p(Step.E, 4), p(Step.D, 4), p(Step.C, 4),
            p(Step.B, 3), p(Step.A, 3), p(Step.G, 3), p(Step.F, 3, 1),
            p(Step.E, 3), p(Step.D, 3), p(Step.C, 3), p(Step.B, 2),
            p(Step.A, 2), p(Step.G, 2)
        ),
        scaleAscendingFingering = listOf(1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 5),
        scaleDescendingFingering = listOf(4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
        scaleAscendingLeftFingering = listOf(5, 4, 3, 2, 1, 3, 2, 1, 4, 3, 2, 1, 3, 2, 1),
        scaleDescendingLeftFingering = listOf(2, 3, 4, 1, 2, 3, 1, 2, 3, 4, 1, 2, 3, 4),
        arpeggioAscendingRightFingering = listOf(1, 2, 3, 1, 2, 3, 5),
        arpeggioAscendingLeftFingering = listOf(5, 4, 2, 1, 4, 2, 1),
        arpeggioDescendingRightFingering = listOf(3, 2, 1, 3, 2, 1),
        arpeggioDescendingLeftFingering = listOf(2, 4, 1, 2, 4, 5),
        arpeggioAscendingRight = listOf(p(Step.G, 4), p(Step.B, 4), p(Step.D, 5), p(Step.G, 5), p(Step.B, 5), p(Step.D, 6), p(Step.G, 6)),
        arpeggioAscendingLeft = listOf(p(Step.G, 2), p(Step.B, 2), p(Step.D, 3), p(Step.G, 3), p(Step.B, 3), p(Step.D, 4), p(Step.G, 4)),
        arpeggioDescendingRight = listOf(p(Step.D, 6), p(Step.B, 5), p(Step.G, 5), p(Step.D, 5), p(Step.B, 4), p(Step.G, 4)),
        arpeggioDescendingLeft = listOf(p(Step.D, 4), p(Step.B, 3), p(Step.G, 3), p(Step.D, 3), p(Step.B, 2), p(Step.G, 2)),
        cadenceFirstBarRight = listOf(
            chord(Duration.HALF, p(Step.B, 4), p(Step.D, 5), p(Step.G, 5)),
            chord(Duration.HALF, p(Step.C, 5), p(Step.E, 5), p(Step.G, 5))
        ),
        cadenceFirstBarLeft = listOf(
            bass(Duration.HALF, p(Step.G, 2)),
            bass(Duration.HALF, p(Step.C, 3))
        ),
        cadenceSecondBarRight = listOf(
            chord(Duration.QUARTER, p(Step.D, 5), p(Step.G, 5), p(Step.B, 5)),
            chord(Duration.QUARTER, p(Step.D, 5), p(Step.F, 5, 1), p(Step.A, 5)),
            chord(Duration.HALF, p(Step.G, 4), p(Step.B, 4), p(Step.D, 5))
        ),
        cadenceSecondBarLeft = listOf(
            bass(Duration.QUARTER, p(Step.D, 3)),
            bass(Duration.QUARTER, p(Step.D, 3)),
            bass(Duration.HALF, p(Step.G, 2))
        )
    )
)

private fun majorTemplate(
    keyFifths: Int,
    scaleAscendingRight: List<Pitch>,
    scaleAscendingLeft: List<Pitch>,
    scaleDescendingRight: List<Pitch>,
    scaleDescendingLeft: List<Pitch>,
    scaleAscendingFingering: List<Int>,
    scaleDescendingFingering: List<Int>,
    scaleAscendingLeftFingering: List<Int>,
    scaleDescendingLeftFingering: List<Int>,
    arpeggioAscendingRightFingering: List<Int>,
    arpeggioAscendingLeftFingering: List<Int>,
    arpeggioDescendingRightFingering: List<Int>,
    arpeggioDescendingLeftFingering: List<Int>,
    arpeggioAscendingRight: List<Pitch>,
    arpeggioAscendingLeft: List<Pitch>,
    arpeggioDescendingRight: List<Pitch>,
    arpeggioDescendingLeft: List<Pitch>,
    cadenceFirstBarRight: List<ChordEventTemplate>,
    cadenceFirstBarLeft: List<BassEventTemplate>,
    cadenceSecondBarRight: List<ChordEventTemplate>,
    cadenceSecondBarLeft: List<BassEventTemplate>
): TechnicalPracticeTemplate {
    return TechnicalPracticeTemplate(
        keyFifths = keyFifths,
        scaleAscending = PracticeLineTemplate(
            rightHand = scaleAscendingRight,
            leftHand = scaleAscendingLeft,
            rightHandFingerings = scaleAscendingFingering,
            leftHandFingerings = scaleAscendingLeftFingering
        ),
        scaleDescending = PracticeLineTemplate(
            rightHand = scaleDescendingRight,
            leftHand = scaleDescendingLeft,
            rightHandFingerings = scaleDescendingFingering,
            leftHandFingerings = scaleDescendingLeftFingering
        ),
        arpeggioAscending = PracticeLineTemplate(
            rightHand = arpeggioAscendingRight,
            leftHand = arpeggioAscendingLeft,
            rightHandFingerings = arpeggioAscendingRightFingering,
            leftHandFingerings = arpeggioAscendingLeftFingering
        ),
        arpeggioDescending = PracticeLineTemplate(
            rightHand = arpeggioDescendingRight,
            leftHand = arpeggioDescendingLeft,
            rightHandFingerings = arpeggioDescendingRightFingering,
            leftHandFingerings = arpeggioDescendingLeftFingering
        ),
        cadence = CadenceTemplate(
            firstBarRightHand = buildChordEvents(cadenceFirstBarRight, staff = 1, voice = 1),
            firstBarLeftHand = buildBassEvents(cadenceFirstBarLeft, staff = 2, voice = 2),
            secondBarRightHand = buildChordEvents(cadenceSecondBarRight, staff = 1, voice = 1),
            secondBarLeftHand = buildBassEvents(cadenceSecondBarLeft, staff = 2, voice = 2)
        )
    )
}

private fun p(step: Step, octave: Int, alter: Int = 0): Pitch = Pitch(step = step, alter = alter, octave = octave)

private fun chord(duration: Duration, vararg pitches: Pitch): ChordEventTemplate {
    return ChordEventTemplate(
        pitches = pitches.toList(),
        duration = duration
    )
}

private fun bass(duration: Duration, pitch: Pitch): BassEventTemplate {
    return BassEventTemplate(
        pitch = pitch,
        duration = duration
    )
}
