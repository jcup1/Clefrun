package com.clefrun.app.feature.scales

import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import com.clefrun.core.TechnicalPracticeGenerator

internal fun generateTechnicalPracticeXml(
    mode: PracticeMode,
    tonic: PracticeTonic
): String {
    return MusicXmlWriter.write(
        TechnicalPracticeGenerator.generate(
            mode = mode,
            tonic = tonic
        )
    )
}
