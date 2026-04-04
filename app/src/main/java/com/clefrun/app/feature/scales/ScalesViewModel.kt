package com.clefrun.app.feature.scales

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import com.clefrun.core.isTechnicalPracticeSupported
import com.clefrun.core.supportedTechnicalPracticeModes
import com.clefrun.core.supportedTechnicalPracticeTonics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScalesViewModel : ViewModel() {
    private var generationJob: Job? = null

    var selectedMode by mutableStateOf(PracticeMode.MAJOR)
        private set

    var selectedTonic by mutableStateOf(PracticeTonic.F)
        private set

    var currentMusicXml by mutableStateOf("")
        private set

    init {
        regenerate()
    }

    fun onModeSelected(mode: PracticeMode) {
        if (selectedMode == mode || !isModeSupported(mode)) return
        selectedMode = mode
        if (!isTonicSupported(selectedTonic)) {
            selectedTonic = supportedTechnicalPracticeTonics(mode).first()
        }
        regenerate()
    }

    fun onTonicSelected(tonic: PracticeTonic) {
        if (selectedTonic == tonic || !isTonicSupported(tonic)) return
        selectedTonic = tonic
        regenerate()
    }

    fun isModeSupported(mode: PracticeMode): Boolean {
        return supportedTechnicalPracticeModes().contains(mode)
    }

    fun isTonicSupported(tonic: PracticeTonic): Boolean {
        return isTechnicalPracticeSupported(selectedMode, tonic)
    }

    private fun regenerate() {
        if (!isTechnicalPracticeSupported(selectedMode, selectedTonic)) return
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val xml = withContext(Dispatchers.Default) {
                generateTechnicalPracticeXml(selectedMode, selectedTonic)
            }
            currentMusicXml = xml
        }
    }
}
