package com.clefrun.app.feature.sightreading

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SightReadingRoute(
    scoreViewModel: ScoreViewModel,
    modifier: Modifier = Modifier,
) {
    val exercisePlan = scoreViewModel.currentExercisePlan
    if (exercisePlan != null) {
        SightReadingScreen(
            musicXml = scoreViewModel.currentMusicXml,
            coach = exercisePlan.coach,
            coachTipId = exercisePlan.id,
            selectedDifficulty = scoreViewModel.selectedDifficulty,
            onDifficultySelected = scoreViewModel::onDifficultySelected,
            onNewExercise = scoreViewModel::onNewExercise,
            modifier = modifier
        )
    }
}
