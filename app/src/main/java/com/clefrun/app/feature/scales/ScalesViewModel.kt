package com.clefrun.app.feature.scales

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import com.clefrun.core.TechnicalPracticeDefaults
import com.clefrun.core.supportedTechnicalPracticeModes
import com.clefrun.core.supportedTechnicalPracticeTonics
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

@Immutable
data class ScalesUiState(
    val selectedMode: PracticeMode = TechnicalPracticeDefaults.mode,
    val selectedTonic: PracticeTonic = TechnicalPracticeDefaults.tonic,
    val supportedModes: ImmutableSet<PracticeMode> = supportedTechnicalPracticeModes().toImmutableSet(),
    val supportedTonics: ImmutableSet<PracticeTonic> = supportedTechnicalPracticeTonics(
        TechnicalPracticeDefaults.mode
    ).toImmutableSet(),
    val currentMusicXml: String = "",
    val isLoading: Boolean = false,
    val error: ScalesError? = null,
)

sealed interface ScalesError {
    data object GenerationFailed : ScalesError
}

class ScalesViewModel(
    private val generateXml: suspend (mode: PracticeMode, tonic: PracticeTonic) -> String = ::generateTechnicalPracticeXml,
    private val generationDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val logger: (String, Throwable?) -> Unit = { msg, t -> Log.e(TAG, msg, t) }
) : ViewModel() {

    private val selection = MutableStateFlow(
        ScaleSelection(
            mode = TechnicalPracticeDefaults.mode,
            tonic = TechnicalPracticeDefaults.tonic
        )
    )

    private val _uiState = MutableStateFlow(ScalesUiState())
    val uiState: StateFlow<ScalesUiState> = _uiState.asStateFlow()

    init {
        observeSelection()
    }

    fun onModeSelected(mode: PracticeMode) {
        val currentSelection = selection.value
        val state = uiState.value

        if (currentSelection.mode == mode || mode !in state.supportedModes) return

        val supportedTonics = supportedTechnicalPracticeTonics(mode).toImmutableSet()
        val tonic = if (currentSelection.tonic in supportedTonics) {
            currentSelection.tonic
        } else {
            TechnicalPracticeDefaults.tonic
        }

        selection.value = ScaleSelection(mode, tonic)
    }

    fun onTonicSelected(tonic: PracticeTonic) {
        val currentSelection = selection.value
        val state = uiState.value

        if (currentSelection.tonic == tonic) return
        if (tonic !in state.supportedTonics) return

        selection.value = currentSelection.copy(tonic = tonic)
    }

    fun onErrorDismissed() {
        clearError()
    }

    private fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSelection() {
        selection
            .onEach { selected ->
                val supportedTonics =
                    supportedTechnicalPracticeTonics(selected.mode).toImmutableSet()

                _uiState.update {
                    it.copy(
                        selectedMode = selected.mode,
                        selectedTonic = selected.tonic,
                        supportedTonics = supportedTonics,
                        isLoading = true,
                        error = null
                    )
                }
            }
            .flatMapLatest { selected ->
                flow {
                    val xml = withContext(generationDispatcher) {
                        generateXml(selected.mode, selected.tonic)
                    }
                    emit(xml)
                }
                    .map { Result.success(it) }
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        logger("Failed to generate music XML", throwable)
                        emit(Result.failure(throwable))
                    }
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { xml ->
                        _uiState.update {
                            it.copy(
                                currentMusicXml = xml,
                                isLoading = false,
                                error = null,
                            )
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = ScalesError.GenerationFailed
                            )
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val TAG = "ScalesViewModel"
    }
}
