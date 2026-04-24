package com.clefrun.app.feature.scales

import android.content.res.Configuration
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clefrun.app.feature.sightreading.ScoreSurface
import com.clefrun.app.ui.components.ClefRunLogo
import com.clefrun.app.ui.theme.AppBackground
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Divider
import com.clefrun.app.ui.theme.Paper
import com.clefrun.app.ui.theme.SelectedFill
import com.clefrun.app.ui.theme.Stroke
import com.clefrun.app.ui.theme.TextPrimary
import com.clefrun.app.ui.theme.TextSecondary
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ScalesScreen(
    viewModel: ScalesViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val onErrorDismissed = viewModel::onErrorDismissed

    if (isLandscape) {
        LandscapeScalesContent(
            uiState = uiState,
            onErrorDismissed = onErrorDismissed,
            modifier = modifier
        )
    } else {
        PortraitScalesContent(
            uiState = uiState,
            onModeSelected = viewModel::onModeSelected,
            onTonicSelected = viewModel::onTonicSelected,
            onErrorDismissed = onErrorDismissed,
            modifier = modifier
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            delay(3000)
            onErrorDismissed()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PortraitScalesContent(
    uiState: ScalesUiState,
    onModeSelected: (PracticeMode) -> Unit,
    onTonicSelected: (PracticeTonic) -> Unit,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 86.dp,
        sheetContainerColor = Paper,
        sheetContentColor = Charcoal,
        sheetShadowElevation = 12.dp,
        sheetDragHandle = {
            Surface(
                color = Divider,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .size(width = 56.dp, height = 6.dp)
            ) {}
        },
        sheetContent = {
            ScalesOptionsSheet(
                selectedMode = uiState.selectedMode,
                selectedTonic = uiState.selectedTonic,
                onModeSelected = onModeSelected,
                onTonicSelected = onTonicSelected,
                supportedModes = uiState.supportedModes,
                supportedTonics = uiState.supportedTonics,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            )
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            ScalesTopOverlayBar(
                onOptionsClick = {
                    scope.launch {
                        val sheetState = scaffoldState.bottomSheetState
                        if (sheetState.currentValue == SheetValue.Expanded) {
                            sheetState.partialExpand()
                        } else {
                            sheetState.expand()
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 28.dp),
            ) {

                ScoreSurface(
                    musicXml = uiState.currentMusicXml,
                    showMeasureNumbers = false,
                    modifier = Modifier.fillMaxSize()
                )

                ScalesErrorBanner(
                    error = uiState.error,
                    onDismissed = onErrorDismissed,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .zIndex(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeScalesContent(
    uiState: ScalesUiState,
    onErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        ScoreSurface(
            musicXml = uiState.currentMusicXml,
            showMeasureNumbers = false,
            modifier = Modifier.fillMaxSize()
        )

        ScalesErrorBanner(
            error = uiState.error,
            onDismissed = onErrorDismissed,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
        )

    }
}

@Composable
private fun ScalesTopOverlayBar(
    onOptionsClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClefRunLogo()

        Surface(
            onClick = onOptionsClick,
            shape = CircleShape,
            color = Paper,
            contentColor = Charcoal,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = "Practice options",
                    tint = Charcoal
                )
            }
        }
    }
}

@VisibleForTesting
@Composable
fun ScalesOptionsSheet(
    selectedMode: PracticeMode,
    selectedTonic: PracticeTonic,
    onModeSelected: (PracticeMode) -> Unit,
    onTonicSelected: (PracticeTonic) -> Unit,
    supportedModes: ImmutableSet<PracticeMode>,
    supportedTonics: ImmutableSet<PracticeTonic>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Mode",
            color = Charcoal,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PracticeMode.entries.forEach { mode ->
                val enabled = mode in supportedModes
                FilterChip(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                    enabled = enabled,
                    label = {
                        Text(mode.label)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SelectedFill,
                        selectedLabelColor = TextPrimary,
                        containerColor = Paper,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = enabled,
                        selected = mode == selectedMode,
                        borderColor = Stroke,
                        selectedBorderColor = Stroke
                    )
                )
            }
        }

        HorizontalDivider(
            color = Divider,
            modifier = Modifier.padding(vertical = 18.dp)
        )

        Text(
            text = "Key",
            color = Charcoal,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PracticeTonic.entries.forEach { tonic ->
                val enabled = tonic in supportedTonics
                FilterChip(
                    selected = tonic == selectedTonic,
                    onClick = { onTonicSelected(tonic) },
                    enabled = enabled,
                    label = {
                        Text(tonic.name)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SelectedFill,
                        selectedLabelColor = TextPrimary,
                        containerColor = Paper,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = enabled,
                        selected = tonic == selectedTonic,
                        borderColor = Stroke,
                        selectedBorderColor = Stroke
                    ),
                    modifier = Modifier.testTag("tonic-chip-${tonic.name}")
                )
            }
        }

        Text(
            text = "Updates instantly for supported curated patterns.",
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun ScalesErrorBanner(
    error: ScalesError?,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = error != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        error?.let {
            Surface(
                onClick = onDismissed,
                color = Paper,
                contentColor = Charcoal,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 6.dp,
                tonalElevation = 2.dp,
                modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = it.message(),
                    modifier = Modifier.padding(12.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

private val PracticeMode.label: String
    get() = when (this) {
        PracticeMode.MAJOR -> "Major"
        PracticeMode.NATURAL_MINOR -> "Natural minor"
        PracticeMode.HARMONIC_MINOR -> "Harmonic minor"
        PracticeMode.MELODIC_MINOR -> "Melodic minor"
    }

private fun ScalesError.message(): String = when (this) {
    ScalesError.GenerationFailed -> "Couldn’t generate the scale. Please try again."
}
