package com.clefrun.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.clefrun.core.Difficulty

class ScoreViewModel : ViewModel() {
    private var nextSeed by mutableLongStateOf(2L)

    var selectedDifficulty by mutableStateOf(Difficulty.EASY)
        private set

    var currentMusicXml by mutableStateOf(generateExerciseXml(seed = 1L, difficulty = Difficulty.EASY))
        private set

    fun onDifficultySelected(difficulty: Difficulty) {
        selectedDifficulty = difficulty
    }

    fun onNewExercise() {
        currentMusicXml = generateExerciseXml(seed = nextSeed, difficulty = selectedDifficulty)
        nextSeed += 1
    }
}
