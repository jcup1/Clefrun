package com.clefrun.app.coach

import com.clefrun.core.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalExercisePlanProviderTest {

    @Test
    fun `cycles through predefined sight reading tips`() {
        val provider = LocalExercisePlanProvider()

        val first = provider.nextSightReadingPlan(seed = 1L, difficulty = Difficulty.EASY)
        val second = provider.nextSightReadingPlan(seed = 2L, difficulty = Difficulty.EASY)
        val third = provider.nextSightReadingPlan(seed = 3L, difficulty = Difficulty.EASY)
        val fourth = provider.nextSightReadingPlan(seed = 4L, difficulty = Difficulty.EASY)

        assertEquals("Read ahead", first.coach.focusLabel)
        assertEquals("Block chords together", second.coach.focusLabel)
        assertEquals("Steady rhythm", third.coach.focusLabel)
        assertEquals(first.coach, fourth.coach)
    }

    @Test
    fun `creates local sight reading plan for selected difficulty`() {
        val provider = LocalExercisePlanProvider()

        val plan = provider.nextSightReadingPlan(seed = 7L, difficulty = Difficulty.HARD)

        assertEquals("local-sight-reading-7", plan.id)
        assertEquals(ExercisePlanSource.LOCAL, plan.source)
        assertEquals(ExercisePlanMode.SIGHT_READING, plan.mode)
        assertEquals(Difficulty.HARD, plan.difficulty)
        assertEquals(plan.coach.focusLabel, plan.focus)
        assertEquals(emptyList<String>(), plan.constraints)
    }
}
