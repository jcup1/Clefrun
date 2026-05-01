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
            targetedPracticeText = scoreViewModel.targetedPracticeText,
            onDifficultySelected = scoreViewModel::onDifficultySelected,
            onTargetedPracticeTextChange = scoreViewModel::onTargetedPracticeTextChange,
            onNewExercise = scoreViewModel::onNewExercise,
            modifier = modifier
        )
    }
}
