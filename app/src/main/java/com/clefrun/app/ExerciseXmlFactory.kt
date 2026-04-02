package com.clefrun.app

import com.clefrun.core.Difficulty
import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.RuleBasedGenerator

internal fun generateExerciseXml(seed: Long, difficulty: Difficulty): String {
    val exercise = RuleBasedGenerator.generate(seed, difficulty)
    return MusicXmlWriter.write(exercise)
}
