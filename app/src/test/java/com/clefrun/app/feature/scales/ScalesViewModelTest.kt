package com.clefrun.app.feature.scales

import com.clefrun.app.MainDispatcherRule
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import com.clefrun.core.TechnicalPracticeDefaults
import com.clefrun.core.supportedTechnicalPracticeTonics
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScalesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state generates default scale`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel()

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(TechnicalPracticeDefaults.mode, state.selectedMode)
        assertEquals(TechnicalPracticeDefaults.tonic, state.selectedTonic)
        assertEquals(
            "xml-${TechnicalPracticeDefaults.mode}-${TechnicalPracticeDefaults.tonic}",
            state.currentMusicXml
        )
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `selecting tonic updates state and generates new xml`() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val newTonic = supportedTechnicalPracticeTonics(TechnicalPracticeDefaults.mode)
                .first { it != TechnicalPracticeDefaults.tonic }
            viewModel.onTonicSelected(newTonic)
            advanceUntilIdle()

            val state = viewModel.uiState.value

            assertEquals(newTonic, state.selectedTonic)
            assertEquals(
                "xml-${state.selectedMode}-${newTonic}",
                state.currentMusicXml
            )
            assertFalse(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun `generation failure updates error state`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = createViewModel(
            generateXml = { _, _ -> throw RuntimeException("Generation failed") }
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(ScalesError.GenerationFailed, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `generation backpressure cancels stale selection and uses latest selection`() =
        runTest(mainDispatcherRule.dispatcher) {
            val defaultTonic = TechnicalPracticeDefaults.tonic
            val intermediateTonic = supportedTechnicalPracticeTonics(TechnicalPracticeDefaults.mode)
                .first { it != defaultTonic }

            val intermediateStarted = CompletableDeferred<Unit>()
            val allowIntermediateToFinish = CompletableDeferred<Unit>()

            val viewModel = createViewModel(
                generateXml = { mode, tonic ->
                    if (tonic == intermediateTonic) {
                        intermediateStarted.complete(Unit)
                        allowIntermediateToFinish.await()
                    }

                    "xml-$mode-$tonic"
                }
            )

            advanceUntilIdle()

            viewModel.onTonicSelected(intermediateTonic)

            intermediateStarted.await()

            viewModel.onTonicSelected(defaultTonic)

            advanceUntilIdle()

            val state = viewModel.uiState.value

            assertEquals(defaultTonic, state.selectedTonic)
            assertEquals(
                "xml-${TechnicalPracticeDefaults.mode}-$defaultTonic",
                state.currentMusicXml
            )
        }

    @Test
    fun `loading is cleared after successful generation`() =
        runTest(mainDispatcherRule.dispatcher) {
            val started = CompletableDeferred<Unit>()
            val finish = CompletableDeferred<Unit>()

            val viewModel = createViewModel(
                generateXml = { mode, tonic ->
                    // Simulate a long-running generation
                    started.complete(Unit)
                    finish.await()
                    "xml-$mode-$tonic"
                }
            )
            started.await()
            assertTrue(viewModel.uiState.value.isLoading)

            finish.complete(Unit)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
        }

    private fun createViewModel(
        generateXml: suspend (PracticeMode, PracticeTonic) -> String =
            { mode, tonic -> "xml-$mode-$tonic" },
    ): ScalesViewModel {
        return ScalesViewModel(
            generateXml = generateXml,
            generationDispatcher = mainDispatcherRule.dispatcher,
            logger = { _, _ -> }
        )
    }
}