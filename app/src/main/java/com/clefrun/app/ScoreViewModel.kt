package com.clefrun.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clefrun.core.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScoreViewModel : ViewModel() {
    private var nextSeed by mutableLongStateOf(2L)
    private var generationJob: Job? = null

    var selectedDifficulty by mutableStateOf(Difficulty.EASY)
        private set

    var currentMusicXml by mutableStateOf("")
        private set

    init {
        generateExercise(seed = 1L, difficulty = Difficulty.EASY)
    }

    fun onDifficultySelected(difficulty: Difficulty) {
        selectedDifficulty = difficulty
    }

    fun onNewExercise() {
        generateExercise(seed = nextSeed, difficulty = selectedDifficulty)
        nextSeed += 1
    }

    private fun generateExercise(seed: Long, difficulty: Difficulty) {
        generationJob?.cancel()
        generationJob = viewModelScope.launch {
            val musicXml = withContext(Dispatchers.Default) {
                generateExerciseXml(seed = seed, difficulty = difficulty)
            }
            currentMusicXml = musicXml
        }
    }
}
