package com.clefrun.app.feature.sightreading

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clefrun.app.di.GenerationDispatcher
import com.clefrun.app.domain.exerciseplan.ExercisePlan
import com.clefrun.app.domain.exerciseplan.ExercisePlanRequest
import com.clefrun.app.domain.exerciseplan.ExercisePlanProvider
import com.clefrun.core.Difficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ScoreViewModel @Inject constructor(
    private val exercisePlanProvider: ExercisePlanProvider,
    private val exerciseXmlGenerator: ExerciseXmlGenerator,
    @param:GenerationDispatcher private val generationDispatcher: CoroutineDispatcher,
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
        generationJob = viewModelScope.launch {
            // TODO: Add an explicit loading state so remote plan requests do not leave the score blank.
            val exercisePlan = exercisePlanProvider.nextSightReadingPlan(request)
            currentExercisePlan = exercisePlan
            val musicXml = withContext(generationDispatcher) {
                exerciseXmlGenerator.generate(exercisePlan)
            }
            currentMusicXml = musicXml
        }
    }

    private companion object {
        const val MaxTargetedPracticeTextLength = 255
    }
}
