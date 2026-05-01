package com.clefrun.app.feature.sightreading

import com.clefrun.app.MainDispatcherRule
import com.clefrun.app.coach.CoachContent
import com.clefrun.app.coach.ExercisePlan
import com.clefrun.app.coach.ExercisePlanMode
import com.clefrun.app.coach.ExercisePlanProvider
import com.clefrun.app.coach.ExercisePlanRequest
import com.clefrun.app.coach.ExercisePlanSource
import com.clefrun.core.Difficulty
import com.clefrun.core.ExerciseFocus
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

            assertEquals("xml-1-EASY-READ_AHEAD", viewModel.currentMusicXml)
            val exercisePlan = checkNotNull(viewModel.currentExercisePlan)
            assertEquals("plan-1-EASY", exercisePlan.id)
            assertEquals("Focus 1", exercisePlan.coach.focusLabel)
            assertEquals(Difficulty.EASY, exercisePlan.difficulty)
        }

    @Test
    fun `initial state requests exercise plan once for easy seed one`() =
        runTest(mainDispatcherRule.dispatcher) {
            val providerRequests = mutableListOf<ExercisePlanRequest>()

            createViewModel(
                exercisePlanProvider = ExercisePlanProvider { request ->
                    providerRequests += request
                    createExercisePlan(seed = request.seed, difficulty = request.difficulty)
                }
            )

            assertEquals(
                listOf(
                    ExercisePlanRequest(
                        seed = 1L,
                        difficulty = Difficulty.EASY,
                        targetedPracticeText = null
                    )
                ),
                providerRequests
            )
        }

    @Test
    fun `new exercise updates exercise plan and music xml`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onNewExercise()
            advanceUntilIdle()

            assertEquals("xml-2-EASY-READ_AHEAD", viewModel.currentMusicXml)
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

            assertEquals("xml-2-HARD-READ_AHEAD", viewModel.currentMusicXml)
            val exercisePlan = checkNotNull(viewModel.currentExercisePlan)
            assertEquals("plan-2-HARD", exercisePlan.id)
            assertEquals(Difficulty.HARD, exercisePlan.difficulty)
        }

    @Test
    fun `targeted practice text is passed to next new exercise request`() =
        runTest(mainDispatcherRule.dispatcher) {
            val providerRequests = mutableListOf<ExercisePlanRequest>()
            val viewModel = createViewModel(
                exercisePlanProvider = ExercisePlanProvider { request ->
                    providerRequests += request
                    createExercisePlan(
                        seed = request.seed,
                        difficulty = request.difficulty,
                        generatorFocus = if (request.targetedPracticeText == "left hand") {
                            ExerciseFocus.LEFT_HAND_STABILITY
                        } else {
                            ExerciseFocus.READ_AHEAD
                        }
                    )
                }
            )
            advanceUntilIdle()

            viewModel.onTargetedPracticeTextChange("left hand")

            assertEquals("xml-1-EASY-READ_AHEAD", viewModel.currentMusicXml)

            viewModel.onNewExercise()
            advanceUntilIdle()

            assertEquals("left hand", providerRequests.last().targetedPracticeText)
            assertEquals("xml-2-EASY-LEFT_HAND_STABILITY", viewModel.currentMusicXml)
        }

    @Test
    fun `targeted practice text is clamped to max length`() {
        val viewModel = createViewModel()

        viewModel.onTargetedPracticeTextChange("a".repeat(300))

        assertEquals(255, viewModel.targetedPracticeText.length)
    }

    private fun createViewModel(
        exercisePlanProvider: ExercisePlanProvider = ExercisePlanProvider { request ->
            createExercisePlan(seed = request.seed, difficulty = request.difficulty)
        },
    ): ScoreViewModel {
        return ScoreViewModel(
            exercisePlanProvider = exercisePlanProvider,
            generateXml = { plan -> "xml-${plan.seed}-${plan.difficulty}-${plan.generatorFocus}" },
            generationDispatcher = mainDispatcherRule.dispatcher,
        )
    }

    private fun createExercisePlan(
        seed: Long,
        difficulty: Difficulty,
        generatorFocus: ExerciseFocus = ExerciseFocus.READ_AHEAD
    ): ExercisePlan {
        return ExercisePlan(
            id = "plan-$seed-$difficulty",
            seed = seed,
            source = ExercisePlanSource.LOCAL,
            mode = ExercisePlanMode.SIGHT_READING,
            difficulty = difficulty,
            generatorFocus = generatorFocus,
            focus = "Focus $seed",
            coach = CoachContent(
                title = "Coach tip",
                focusLabel = "Focus $seed",
                body = "Body $seed",
            )
        )
    }
}
