package com.clefrun.app.feature.sightreading

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SightReadingRoute(
    scoreViewModel: ScoreViewModel,
    modifier: Modifier = Modifier,
) {
    SightReadingScreen(
        musicXml = scoreViewModel.currentMusicXml,
        coach = scoreViewModel.currentExercisePlan.coach,
        selectedDifficulty = scoreViewModel.selectedDifficulty,
        onDifficultySelected = scoreViewModel::onDifficultySelected,
        onNewExercise = scoreViewModel::onNewExercise,
        modifier = modifier
    )
}
