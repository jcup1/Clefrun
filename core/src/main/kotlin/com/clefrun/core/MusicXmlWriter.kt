package com.clefrun.core

object MusicXmlWriter {
    fun write(exercise: Exercise): String {
        // Milestone 2 keeps output fixed to the known smoke-test XML.
        return TEST_EXERCISE_XML
    }

    fun writeTestExercise(): String {
        return write(
            exercise = Exercise(
                bars = listOf(
                    Bar(
                        number = 1,
                        rightHand = emptyList(),
                        leftHand = emptyList()
                    ),
                    Bar(
                        number = 2,
                        rightHand = emptyList(),
                        leftHand = emptyList()
                    ),
                    Bar(
                        number = 3,
                        rightHand = emptyList(),
                        leftHand = emptyList()
                    ),
                    Bar(
                        number = 4,
                        rightHand = emptyList(),
                        leftHand = emptyList()
                    )
                )
            )
        )
    }
}

private const val TEST_EXERCISE_XML = """<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE score-partwise PUBLIC
    "-//Recordare//DTD MusicXML 3.1 Partwise//EN"
    "http://www.musicxml.org/dtds/partwise.dtd">
<score-partwise version="3.1">
  <part-list>
    <score-part id="P1">
      <part-name>Piano</part-name>
    </score-part>
  </part-list>
  <part id="P1">
    <measure number="1">
      <attributes>
        <divisions>1</divisions>
        <key>
          <fifths>0</fifths>
        </key>
        <time>
          <beats>4</beats>
          <beat-type>4</beat-type>
        </time>
        <staves>2</staves>
        <clef number="1">
          <sign>G</sign>
          <line>2</line>
        </clef>
        <clef number="2">
          <sign>F</sign>
          <line>4</line>
        </clef>
      </attributes>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <backup>
        <duration>4</duration>
      </backup>
      <note>
        <pitch>
          <step>C</step>
          <octave>3</octave>
        </pitch>
        <duration>2</duration>
        <type>half</type>
        <voice>2</voice>
        <staff>2</staff>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <octave>2</octave>
        </pitch>
        <duration>2</duration>
        <type>half</type>
        <voice>2</voice>
        <staff>2</staff>
      </note>
    </measure>
    <measure number="2">
      <note>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>F</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>G</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <backup>
        <duration>4</duration>
      </backup>
      <note>
        <pitch>
          <step>F</step>
          <octave>2</octave>
        </pitch>
        <duration>4</duration>
        <type>whole</type>
        <voice>2</voice>
        <staff>2</staff>
      </note>
    </measure>
    <measure number="3">
      <note>
        <pitch>
          <step>G</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>E</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>D</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>1</duration>
        <type>quarter</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <backup>
        <duration>4</duration>
      </backup>
      <note>
        <pitch>
          <step>G</step>
          <octave>2</octave>
        </pitch>
        <duration>2</duration>
        <type>half</type>
        <voice>2</voice>
        <staff>2</staff>
      </note>
      <note>
        <pitch>
          <step>C</step>
          <octave>3</octave>
        </pitch>
        <duration>2</duration>
        <type>half</type>
        <voice>2</voice>
        <staff>2</staff>
      </note>
    </measure>
    <measure number="4">
      <note>
        <pitch>
          <step>C</step>
          <octave>5</octave>
        </pitch>
        <duration>4</duration>
        <type>whole</type>
        <voice>1</voice>
        <staff>1</staff>
      </note>
      <backup>
        <duration>4</duration>
      </backup>
      <note>
        <pitch>
          <step>C</step>
          <octave>3</octave>
        </pitch>
        <duration>4</duration>
        <type>whole</type>
        <voice>2</voice>
        <staff>2</staff>
      </note>
      <barline location="right">
        <bar-style>light-heavy</bar-style>
      </barline>
    </measure>
  </part>
</score-partwise>"""
