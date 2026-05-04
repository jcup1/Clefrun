package com.clefrun.app.feature.sightreading

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun SightReadingRoute(
    modifier: Modifier = Modifier,
    scoreViewModel: ScoreViewModel = hiltViewModel(),
) {
    val exercisePlan = scoreViewModel.currentExercisePlan
    if (exercisePlan == null) {
        //TODO show loading screen
    } else {
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
