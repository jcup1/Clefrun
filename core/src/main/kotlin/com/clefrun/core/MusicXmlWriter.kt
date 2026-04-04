package com.clefrun.core

object MusicXmlWriter {
    fun write(exercise: Exercise): String {
        val xml = StringBuilder()
        xml.append("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>""").append('\n')
        xml.append("<score-partwise version=\"3.1\">").append('\n')
        xml.append("  <part-list>").append('\n')
        xml.append("    <score-part id=\"P1\">").append('\n')
        xml.append("      <part-name>Piano</part-name>").append('\n')
        xml.append("    </score-part>").append('\n')
        xml.append("  </part-list>").append('\n')
        xml.append("  <part id=\"P1\">").append('\n')

        exercise.bars.forEachIndexed { index, bar ->
            val accidentalStateByPitch = mutableMapOf<AccidentalKey, Int>()
            xml.append("    <measure number=\"${bar.number}\">").append('\n')

            val previousLayout = exercise.bars.getOrNull(index - 1)?.staffLayout
            val writeAttributes = index == 0 || previousLayout != bar.staffLayout
            if (writeAttributes) {
                xml.append("      <attributes>").append('\n')
                xml.append("        <divisions>${exercise.divisions}</divisions>").append('\n')
                if (index == 0) {
                    xml.append("        <key>").append('\n')
                    xml.append("          <fifths>${exercise.keyFifths}</fifths>").append('\n')
                    xml.append("        </key>").append('\n')
                    if (exercise.showTimeSignature) {
                        xml.append("        <time>").append('\n')
                    } else {
                        xml.append("        <time print-object=\"no\" symbol=\"none\">").append('\n')
                    }
                    xml.append("          <beats>${exercise.beats}</beats>").append('\n')
                    xml.append("          <beat-type>${exercise.beatType}</beat-type>").append('\n')
                    xml.append("        </time>").append('\n')
                }
                when (bar.staffLayout) {
                    StaffLayout.SINGLE_TREBLE -> {
                        xml.append("        <staves>1</staves>").append('\n')
                        xml.append("        <clef>").append('\n')
                        xml.append("          <sign>G</sign>").append('\n')
                        xml.append("          <line>2</line>").append('\n')
                        xml.append("        </clef>").append('\n')
                    }
                    StaffLayout.GRAND_STAFF -> {
                        xml.append("        <staves>2</staves>").append('\n')
                        xml.append("        <clef number=\"1\">").append('\n')
                        xml.append("          <sign>G</sign>").append('\n')
                        xml.append("          <line>2</line>").append('\n')
                        xml.append("        </clef>").append('\n')
                        xml.append("        <clef number=\"2\">").append('\n')
                        xml.append("          <sign>F</sign>").append('\n')
                        xml.append("          <line>4</line>").append('\n')
                        xml.append("        </clef>").append('\n')
                    }
                }
                xml.append("      </attributes>").append('\n')
            }

            if (bar.sectionLabel != null) {
                xml.append("      <direction placement=\"above\">").append('\n')
                xml.append("        <direction-type>").append('\n')
                xml.append("          <words default-x=\"${bar.sectionLabelOffsetX}\" font-style=\"italic\">${bar.sectionLabel}</words>").append('\n')
                xml.append("        </direction-type>").append('\n')
                xml.append("      </direction>").append('\n')
            }

            bar.rightHand.forEach { note -> appendNote(xml, note, accidentalStateByPitch, exercise.divisions) }
            if (bar.leftHand.isNotEmpty()) {
                xml.append("      <backup>").append('\n')
                xml.append("        <duration>${exercise.beats * exercise.divisions}</duration>").append('\n')
                xml.append("      </backup>").append('\n')
                bar.leftHand.forEach { note -> appendNote(xml, note, accidentalStateByPitch, exercise.divisions) }
            }

            if (index == exercise.bars.lastIndex) {
                xml.append("      <barline location=\"right\">").append('\n')
                xml.append("        <bar-style>light-heavy</bar-style>").append('\n')
                xml.append("      </barline>").append('\n')
            }

            xml.append("    </measure>").append('\n')
        }

        xml.append("  </part>").append('\n')
        xml.append("</score-partwise>")
        return xml.toString()
    }

    private fun appendNote(
        xml: StringBuilder,
        note: NoteEvent,
        accidentalStateByPitch: MutableMap<AccidentalKey, Int>,
        divisionsPerQuarter: Int
    ) {
        val pitches = buildList {
            note.pitch?.let { add(it) }
            addAll(note.additionalPitches)
        }
        if (note.isRest) {
            xml.append(noteOpenTag(note)).append('\n')
            xml.append("        <rest/>").append('\n')
            xml.append("        <duration>${note.duration.beats * divisionsPerQuarter}</duration>").append('\n')
            xml.append("        <type>${note.duration.musicXmlType}</type>").append('\n')
            xml.append("        <voice>${note.voice}</voice>").append('\n')
            xml.append("        <staff>${note.staff}</staff>").append('\n')
            xml.append("      </note>").append('\n')
            return
        }

        pitches.forEachIndexed { index, pitch ->
            xml.append(noteOpenTag(note)).append('\n')
            if (index > 0) {
                xml.append("        <chord/>").append('\n')
            }
            val accidentalKey = AccidentalKey(
                step = pitch.step,
                octave = pitch.octave,
                staff = note.staff
            )
            val accidental = accidentalSymbolForDisplay(
                currentAlter = pitch.alter,
                previousAlter = accidentalStateByPitch[accidentalKey]
            )

            xml.append("        <pitch>").append('\n')
            xml.append("          <step>${pitch.step.name}</step>").append('\n')
            xml.append("          <alter>${pitch.alter}</alter>").append('\n')
            xml.append("          <octave>${pitch.octave}</octave>").append('\n')
            xml.append("        </pitch>").append('\n')
            xml.append("        <duration>${note.duration.beats * divisionsPerQuarter}</duration>").append('\n')
            xml.append("        <type>${note.duration.musicXmlType}</type>").append('\n')
            if (accidental != null) {
                xml.append("        <accidental>$accidental</accidental>").append('\n')
            }
            if (note.fingerings.isNotEmpty()) {
                xml.append("        <notations>").append('\n')
                xml.append("          <technical>").append('\n')
                xml.append("            <fingering>${note.fingerings[index]}</fingering>").append('\n')
                xml.append("          </technical>").append('\n')
                xml.append("        </notations>").append('\n')
            }
            xml.append("        <voice>${note.voice}</voice>").append('\n')
            xml.append("        <staff>${note.staff}</staff>").append('\n')
            xml.append("      </note>").append('\n')

            accidentalStateByPitch[accidentalKey] = pitch.alter
        }
    }

    private fun accidentalSymbolForDisplay(currentAlter: Int, previousAlter: Int?): String? {
        if (previousAlter == currentAlter) return null
        return when (currentAlter) {
            1 -> "sharp"
            -1 -> "flat"
            0 -> if (previousAlter == null) null else "natural"
            else -> error("Unexpected alter value: $currentAlter")
        }
    }

    private fun noteOpenTag(note: NoteEvent): String {
        return if (note.printObject) {
            "      <note>"
        } else {
            "      <note print-object=\"no\">"
        }
    }
}

private data class AccidentalKey(
    val step: Step,
    val octave: Int,
    val staff: Int
)
