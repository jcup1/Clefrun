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
            val exercisePlan = checkNotNull(viewModel.currentExercisePlan)
            assertEquals("plan-1-EASY", exercisePlan.id)
            assertEquals("Focus 1", exercisePlan.coach.focusLabel)
            assertEquals(Difficulty.EASY, exercisePlan.difficulty)
        }

    @Test
    fun `initial state requests exercise plan once for easy seed one`() =
        runTest(mainDispatcherRule.dispatcher) {
            val providerRequests = mutableListOf<Pair<Long, Difficulty>>()

            createViewModel(
                exercisePlanProvider = ExercisePlanProvider { seed, difficulty ->
                    providerRequests += seed to difficulty
                    createExercisePlan(seed = seed, difficulty = difficulty)
                }
            )

            assertEquals(listOf(1L to Difficulty.EASY), providerRequests)
        }

    @Test
    fun `new exercise updates exercise plan and music xml`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onNewExercise()
            advanceUntilIdle()

            assertEquals("xml-2-EASY", viewModel.currentMusicXml)
            val exercisePlan = checkNotNull(viewModel.currentExercisePlan)
            assertEquals("plan-2-EASY", exercisePlan.id)
            assertEquals("Focus 2", exercisePlan.coach.focusLabel)
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
            val exercisePlan = checkNotNull(viewModel.currentExercisePlan)
            assertEquals("plan-2-HARD", exercisePlan.id)
            assertEquals(Difficulty.HARD, exercisePlan.difficulty)
        }

    private fun createViewModel(
        exercisePlanProvider: ExercisePlanProvider = ExercisePlanProvider { seed, difficulty ->
            createExercisePlan(seed = seed, difficulty = difficulty)
        },
    ): ScoreViewModel {
        return ScoreViewModel(
            exercisePlanProvider = exercisePlanProvider,
            generateXml = { seed, difficulty -> "xml-$seed-$difficulty" },
            generationDispatcher = mainDispatcherRule.dispatcher,
        )
    }

    private fun createExercisePlan(seed: Long, difficulty: Difficulty): ExercisePlan {
        return ExercisePlan(
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
    }
}
