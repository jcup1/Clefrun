package com.clefrun.app.feature.scales

import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic

data class ScaleSelection(
    val mode: PracticeMode,
    val tonic: PracticeTonic
)