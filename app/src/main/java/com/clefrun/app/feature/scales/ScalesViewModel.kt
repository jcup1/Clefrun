package com.clefrun.app.feature.scales

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import com.clefrun.core.supportedTechnicalPracticeModes
import com.clefrun.core.supportedTechnicalPracticeTonics
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
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
    val selectedMode: PracticeMode = PracticeMode.MAJOR,
    val selectedTonic: PracticeTonic = PracticeTonic.F,
    val supportedModes: ImmutableSet<PracticeMode> = supportedTechnicalPracticeModes().toImmutableSet(),
    val supportedTonics: ImmutableSet<PracticeTonic> = supportedTechnicalPracticeTonics(PracticeMode.MAJOR).toImmutableSet(),
    val currentMusicXml: String = "",
    val isLoading: Boolean = false,
    val error: ScalesError? = null,
)

sealed interface ScalesError {
    data object GenerationFailed : ScalesError
}

class ScalesViewModel : ViewModel() {

    private val selection = MutableStateFlow(
        ScaleSelection(
            mode = PracticeMode.MAJOR,
            tonic = PracticeTonic.F
        )
    )

    private val _uiState = MutableStateFlow(ScalesUiState())
    val uiState: StateFlow<ScalesUiState> = _uiState.asStateFlow()

    init {
        observeSelection()
    }

    fun onModeSelected(mode: PracticeMode) {
        val state = uiState.value
        if (state.selectedMode == mode || mode !in state.supportedModes) return

        val supportedTonics = supportedTechnicalPracticeTonics(mode).toImmutableSet()
        val tonic = if (state.selectedTonic in supportedTonics) {
            state.selectedTonic
        } else {
            supportedTonics.first()
        }

        selection.value = ScaleSelection(mode, tonic)
    }

    fun onTonicSelected(tonic: PracticeTonic) {
        val state = uiState.value
        if (state.selectedTonic == tonic) return
        if (tonic !in state.supportedTonics) return

        selection.value = ScaleSelection(state.selectedMode, tonic)
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
                    val xml = withContext(Dispatchers.Default) {
                        generateTechnicalPracticeXml(selected.mode, selected.tonic)
                    }
                    emit(xml)
                }
                    .map { Result.success(it) }
                    .catch { throwable ->
                        if (throwable is CancellationException) throw throwable
                        Log.e(TAG, "Failed to generate music XML", throwable)
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
