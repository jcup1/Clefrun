package com.clefrun.app.coach

import com.clefrun.core.Difficulty
import com.clefrun.core.ExerciseFocus
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalExercisePlanProviderTest {

    @Test
    fun `empty targeted practice uses read ahead plan`() {
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
    fun `creates local sight reading plan for selected difficulty`() {
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
        assertEquals(emptyList<String>(), plan.constraints)
    }

    @Test
    fun `maps targeted practice text to supported focus`() {
        val provider = LocalExercisePlanProvider()

        assertEquals(ExerciseFocus.LEFT_HAND_STABILITY, provider.focusForText("bass clef"))
        assertEquals(ExerciseFocus.ACCIDENTALS, provider.focusForText("sharps and flats"))
        assertEquals(ExerciseFocus.SMALL_LEAPS, provider.focusForText("small jumps"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("chords"))
        assertEquals(ExerciseFocus.READ_AHEAD, provider.focusForText("something else"))
    }

    private fun LocalExercisePlanProvider.focusForText(text: String): ExerciseFocus {
        return nextSightReadingPlan(
            ExercisePlanRequest(
                seed = 1L,
                difficulty = Difficulty.EASY,
                targetedPracticeText = text
            )
        ).generatorFocus
    }
}
