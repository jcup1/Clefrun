package com.clefrun.app.feature.sightreading

import com.clefrun.app.MainDispatcherRule
import com.clefrun.app.coach.CoachContent
import com.clefrun.app.coach.ExercisePlan
import com.clefrun.app.coach.ExercisePlanMode
import com.clefrun.app.coach.ExercisePlanProvider
import com.clefrun.app.coach.ExercisePlanSource
import com.clefrun.core.Difficulty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state creates exercise plan and music xml`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()

            advanceUntilIdle()

            assertEquals("xml-1-EASY", viewModel.currentMusicXml)
            assertEquals("plan-1-EASY", viewModel.currentExercisePlan.id)
            assertEquals("Focus 1", viewModel.currentExercisePlan.coach.focusLabel)
            assertEquals(Difficulty.EASY, viewModel.currentExercisePlan.difficulty)
        }

    @Test
    fun `new exercise updates exercise plan and music xml`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onNewExercise()
            advanceUntilIdle()

            assertEquals("xml-2-EASY", viewModel.currentMusicXml)
            assertEquals("plan-2-EASY", viewModel.currentExercisePlan.id)
            assertEquals("Focus 2", viewModel.currentExercisePlan.coach.focusLabel)
        }

    @Test
    fun `new exercise uses selected difficulty for plan and generation`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onDifficultySelected(Difficulty.HARD)
            viewModel.onNewExercise()
            advanceUntilIdle()

            assertEquals("xml-2-HARD", viewModel.currentMusicXml)
            assertEquals("plan-2-HARD", viewModel.currentExercisePlan.id)
            assertEquals(Difficulty.HARD, viewModel.currentExercisePlan.difficulty)
        }

    private fun createViewModel(): ScoreViewModel {
        return ScoreViewModel(
            exercisePlanProvider = ExercisePlanProvider { seed, difficulty ->
                ExercisePlan(
                    id = "plan-$seed-$difficulty",
                    source = ExercisePlanSource.LOCAL,
                    mode = ExercisePlanMode.SIGHT_READING,
                    difficulty = difficulty,
                    focus = "Focus $seed",
                    coach = CoachContent(
                        title = "Coach tip",
                        focusLabel = "Focus $seed",
                        body = "Body $seed",
                    )
                )
            },
            generateXml = { seed, difficulty -> "xml-$seed-$difficulty" },
            generationDispatcher = mainDispatcherRule.dispatcher,
        )
    }
}
