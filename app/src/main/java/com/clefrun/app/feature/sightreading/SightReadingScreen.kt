package com.clefrun.app.feature.sightreading

import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.clefrun.app.coach.CoachContent
import com.clefrun.app.ui.coach.CoachBubble
import com.clefrun.app.ui.coach.CoachTipPopup
import com.clefrun.app.ui.score.ScoreSurface
import com.clefrun.app.ui.theme.AppBackground
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Divider
import com.clefrun.app.ui.theme.Paper
import com.clefrun.core.Difficulty
import kotlinx.coroutines.launch

@Composable
internal fun SightReadingScreen(
    musicXml: String,
    coach: CoachContent,
    coachTipId: String,
    selectedDifficulty: Difficulty,
    targetedPracticeText: String,
    onDifficultySelected: (Difficulty) -> Unit,
    onTargetedPracticeTextChange: (String) -> Unit,
    onNewExercise: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeScoreScreen(
            musicXml = musicXml,
            onNextExercise = onNewExercise,
            modifier = modifier
        )
    } else {
        PortraitScoreScreen(
            musicXml = musicXml,
            coach = coach,
            coachTipId = coachTipId,
            selectedDifficulty = selectedDifficulty,
            targetedPracticeText = targetedPracticeText,
            onDifficultySelected = onDifficultySelected,
            onTargetedPracticeTextChange = onTargetedPracticeTextChange,
            onRegenerate = onNewExercise,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortraitScoreScreen(
    musicXml: String,
    coach: CoachContent,
    coachTipId: String,
    selectedDifficulty: Difficulty,
    targetedPracticeText: String,
    onDifficultySelected: (Difficulty) -> Unit,
    onTargetedPracticeTextChange: (String) -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tempo by remember { mutableFloatStateOf(0.55f) }
    var isCoachTipVisible by rememberSaveable { mutableStateOf(false) }
    var isCoachTipUnread by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(coachTipId) {
        isCoachTipUnread = !isCoachTipVisible
    }

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
        sheetPeekHeight = SightReadingBottomSheetPeekHeight,
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
                targetedPracticeText = targetedPracticeText,
                onTargetedPracticeTextChange = onTargetedPracticeTextChange,
                onTargetedPracticeFocused = {
                    scaffoldState.bottomSheetState.expand()
                },
                tempo = tempo,
                onTempoChange = { tempo = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                ScoreSurface(
                    musicXml = musicXml,
                    modifier = Modifier.fillMaxSize()
                )

                androidx.compose.animation.AnimatedVisibility(
                    visible = isCoachTipVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 200)) +
                        scaleIn(
                            initialScale = 0.96f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = tween(durationMillis = 200)
                        ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 160)) +
                        scaleOut(
                            targetScale = 0.96f,
                            transformOrigin = TransformOrigin(1f, 1f),
                            animationSpec = tween(durationMillis = 160)
                        ),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = CoachTipPopupEndPadding,
                            bottom = CoachTipPopupBottomPadding
                        )
                        .zIndex(1f)
                ) {
                    CoachTipPopup(
                        coach = coach,
                        onClose = { isCoachTipVisible = false }
                    )
                }

                CoachBubble(
                    showUnreadBadge = isCoachTipUnread,
                    onClick = {
                        val shouldOpen = !isCoachTipVisible
                        isCoachTipVisible = shouldOpen
                        if (shouldOpen) {
                            isCoachTipUnread = false
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(
                            end = CoachBubbleEndPadding,
                            bottom = CoachBubbleBottomPadding
                        )
                        .zIndex(2f)
                )
            }
        }
    }
}

private val SightReadingBottomSheetPeekHeight = 86.dp
private val CoachBubbleBottomPadding: Dp = SightReadingBottomSheetPeekHeight + 16.dp
private val CoachTipPopupBottomPadding: Dp = CoachBubbleBottomPadding + 76.dp
private val CoachTipPopupEndPadding = 8.dp
private val CoachBubbleEndPadding = 12.dp

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
