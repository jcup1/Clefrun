package com.clefrun.app.feature.scales

import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import javax.inject.Inject

fun interface TechnicalPracticeXmlGenerator {
    suspend fun generate(mode: PracticeMode, tonic: PracticeTonic): String
}

class DefaultTechnicalPracticeXmlGenerator @Inject constructor() : TechnicalPracticeXmlGenerator {
    override suspend fun generate(mode: PracticeMode, tonic: PracticeTonic): String {
        return generateTechnicalPracticeXml(mode, tonic)
    }
}
