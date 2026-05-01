package com.clefrun.app.feature.sightreading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clefrun.app.coach.ExercisePlan
import com.clefrun.app.coach.ExercisePlanRequest
import com.clefrun.app.coach.ExercisePlanProvider
import com.clefrun.app.coach.LocalExercisePlanProvider
import com.clefrun.core.Difficulty
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScoreViewModel(
    private val exercisePlanProvider: ExercisePlanProvider = LocalExercisePlanProvider(),
    private val generateXml: suspend (ExercisePlan) -> String = ::generateExerciseXml,
    private val generationDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private var nextSeed by mutableLongStateOf(2L)
    private var generationJob: Job? = null

    var selectedDifficulty by mutableStateOf(Difficulty.EASY)
        private set

    var currentMusicXml by mutableStateOf("")
        private set

    var currentExercisePlan: ExercisePlan? by mutableStateOf(null)
        private set

    var targetedPracticeText by mutableStateOf("")
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

    fun onTargetedPracticeTextChange(text: String) {
        targetedPracticeText = text.take(MaxTargetedPracticeTextLength)
    }

    private fun generateExercise(seed: Long, difficulty: Difficulty) {
        generationJob?.cancel()
        val request = ExercisePlanRequest(
            seed = seed,
            difficulty = difficulty,
            targetedPracticeText = targetedPracticeText.ifBlank { null }
        )
        val exercisePlan = exercisePlanProvider.nextSightReadingPlan(request)
        currentExercisePlan = exercisePlan
        generationJob = viewModelScope.launch {
            val musicXml = withContext(generationDispatcher) {
                generateXml(exercisePlan)
            }
            currentMusicXml = musicXml
        }
    }

    private companion object {
        const val MaxTargetedPracticeTextLength = 255
    }
}
