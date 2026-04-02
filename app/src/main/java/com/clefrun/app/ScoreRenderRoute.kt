package com.clefrun.app

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.clefrun.app.ui.theme.AppBackground
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Divider
import com.clefrun.app.ui.theme.Paper
import com.clefrun.core.Difficulty
import kotlinx.coroutines.launch

@Composable
fun ScoreRenderScreen(
    scoreViewModel: ScoreViewModel,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeScoreScreen(
            musicXml = scoreViewModel.currentMusicXml,
            onNextExercise = scoreViewModel::onNewExercise,
            modifier = modifier
        )
    } else {
        PortraitScoreScreen(
            musicXml = scoreViewModel.currentMusicXml,
            selectedDifficulty = scoreViewModel.selectedDifficulty,
            onDifficultySelected = scoreViewModel::onDifficultySelected,
            onRegenerate = scoreViewModel::onNewExercise,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitScoreScreen(
    musicXml: String,
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tempo by remember { mutableFloatStateOf(0.55f) }

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
            OptionsSheetContent(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = onDifficultySelected,
                tempo = tempo,
                onTempoChange = { tempo = it },
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
            TopOverlayBar(
                onNewClick = onRegenerate,
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

            ScoreSurface(
                musicXml = musicXml,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun LandscapeScoreScreen(
    musicXml: String,
    onNextExercise: () -> Unit,
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
            musicXml = musicXml,
            modifier = Modifier.fillMaxSize()
        )

        LandscapeNextButton(
            onClick = onNextExercise,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
        )
    }
}

@Composable
private fun LandscapeNextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Paper,
        contentColor = Charcoal,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "Next exercise",
                tint = Charcoal
            )
        }
    }
}
