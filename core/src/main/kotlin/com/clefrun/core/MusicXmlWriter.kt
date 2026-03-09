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
            xml.append("    <measure number=\"${bar.number}\">").append('\n')

            if (index == 0) {
                xml.append("      <attributes>").append('\n')
                xml.append("        <divisions>1</divisions>").append('\n')
                xml.append("        <key>").append('\n')
                xml.append("          <fifths>${exercise.keyFifths}</fifths>").append('\n')
                xml.append("        </key>").append('\n')
                xml.append("        <time>").append('\n')
                xml.append("          <beats>${exercise.beats}</beats>").append('\n')
                xml.append("          <beat-type>${exercise.beatType}</beat-type>").append('\n')
                xml.append("        </time>").append('\n')
                xml.append("        <staves>2</staves>").append('\n')
                xml.append("        <clef number=\"1\">").append('\n')
                xml.append("          <sign>G</sign>").append('\n')
                xml.append("          <line>2</line>").append('\n')
                xml.append("        </clef>").append('\n')
                xml.append("        <clef number=\"2\">").append('\n')
                xml.append("          <sign>F</sign>").append('\n')
                xml.append("          <line>4</line>").append('\n')
                xml.append("        </clef>").append('\n')
                xml.append("      </attributes>").append('\n')
            }

            bar.rightHand.forEach { note -> appendNote(xml, note) }
            xml.append("      <backup>").append('\n')
            xml.append("        <duration>${exercise.beats}</duration>").append('\n')
            xml.append("      </backup>").append('\n')
            bar.leftHand.forEach { note -> appendNote(xml, note) }

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

    private fun appendNote(xml: StringBuilder, note: NoteEvent) {
        xml.append("      <note>").append('\n')
        if (note.isRest) {
            xml.append("        <rest/>").append('\n')
        } else {
            val step = requireNotNull(note.step) { "Non-rest note must include step." }
            val octave = requireNotNull(note.octave) { "Non-rest note must include octave." }
            xml.append("        <pitch>").append('\n')
            xml.append("          <step>$step</step>").append('\n')
            xml.append("          <octave>$octave</octave>").append('\n')
            xml.append("        </pitch>").append('\n')
        }
        xml.append("        <duration>${note.duration.beats}</duration>").append('\n')
        xml.append("        <type>${note.duration.musicXmlType}</type>").append('\n')
        xml.append("        <voice>${note.voice}</voice>").append('\n')
        xml.append("        <staff>${note.staff}</staff>").append('\n')
        xml.append("      </note>").append('\n')
    }
}
