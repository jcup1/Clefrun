package com.clefrun.app.domain.exerciseplan

import com.clefrun.core.Difficulty
import com.clefrun.core.ExerciseFocus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalExercisePlanProviderTest {

    @Test
    fun `empty targeted practice uses read ahead plan`() = runTest {
        val provider = LocalExercisePlanProvider()

        val plan = provider.nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 1L,
                difficulty = Difficulty.EASY,
                targetedPracticeText = null
            )
        )

        assertEquals(ExerciseFocus.READ_AHEAD, plan.generatorFocus)
        assertEquals("Read ahead", plan.coach.focusLabel)
    }

    @Test
    fun `creates local sight reading plan for selected difficulty`() = runTest {
        val provider = LocalExercisePlanProvider()

        val plan = provider.nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 7L,
                difficulty = Difficulty.HARD,
                targetedPracticeText = "left hand"
            )
        )

        assertEquals("local-sight-reading-7", plan.id)
        assertEquals(7L, plan.seed)
        assertEquals(ExercisePlanSource.LOCAL, plan.source)
        assertEquals(ExercisePlanMode.SIGHT_READING, plan.mode)
        assertEquals(Difficulty.HARD, plan.difficulty)
        assertEquals(ExerciseFocus.LEFT_HAND_STABILITY, plan.generatorFocus)
        assertEquals(plan.coach.focusLabel, plan.focus)
        assertEquals(
            ExercisePlanConstraints(
                leftHandTexture = LeftHandTexture.STEADY_BASS
            ),
            plan.constraints
        )
    }

    @Test
    fun `local plans map focused practice to typed constraints`() = runTest {
        val provider = LocalExercisePlanProvider()

        assertEquals(
            ExercisePlanConstraints(
                leftHandTexture = LeftHandTexture.STEADY_BASS
            ),
            provider.constraintsForText("left hand")
        )
        assertEquals(
            ExercisePlanConstraints(
                accidentalDensity = AccidentalDensity.MEDIUM
            ),
            provider.constraintsForText("accidentals")
        )
        assertEquals(
            ExercisePlanConstraints(
                rightHandMotion = RightHandMotion.STEPWISE_WITH_SMALL_LEAPS,
                maxLeap = MaxLeap.FOURTH
            ),
            provider.constraintsForText("small jumps")
        )
        assertEquals(ExercisePlanConstraints(), provider.constraintsForText("read ahead"))
    }

    @Test
    fun `maps targeted practice text to supported focus`() = runTest {
        val provider = LocalExercisePlanProvider()

        assertEquals(ExerciseFocus.LEFT_HAND_STABILITY, provider.focusForText("left hand"))
        assertEquals(ExerciseFocus.LEFT_HAND_STABILITY, provider.focusForText("bass clef"))
        assertEquals(ExerciseFocus.ACCIDENTALS, provider.focusForText("sharps and flats"))
        assertEquals(ExerciseFocus.ACCIDENTALS, provider.focusForText("accidentals"))
        assertEquals(ExerciseFocus.SMALL_LEAPS, provider.focusForText("small jumps"))
        assertEquals(ExerciseFocus.SMALL_LEAPS, provider.focusForText("leaps"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("chords"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("something else"))
    }

    @Test
    fun `does not match targeted practice substrings inside larger words`() = runTest {
        val provider = LocalExercisePlanProvider()

        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("cleft hand"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("flatware"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("jumper"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("jumping"))
    }

    @Test
    fun `tokenizes targeted practice on non letter separators`() = runTest {
        val provider = LocalExercisePlanProvider()

        assertEquals(ExerciseFocus.LEFT_HAND_STABILITY, provider.focusForText("LEFT-hand"))
        assertEquals(ExerciseFocus.ACCIDENTALS, provider.focusForText("sharp/flat"))
        assertEquals(ExerciseFocus.SMALL_LEAPS, provider.focusForText("small,jumps"))
    }

    private suspend fun LocalExercisePlanProvider.focusForText(text: String): ExerciseFocus {
        return nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 1L,
                difficulty = Difficulty.EASY,
                targetedPracticeText = text
            )
        ).generatorFocus
    }

    private suspend fun LocalExercisePlanProvider.constraintsForText(text: String): ExercisePlanConstraints {
        return nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 1L,
                difficulty = Difficulty.EASY,
                targetedPracticeText = text
            )
        ).constraints
    }
}
