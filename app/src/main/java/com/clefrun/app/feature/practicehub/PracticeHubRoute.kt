package com.clefrun.app.feature.practicehub

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PracticeHubRoute(
    onSightReadingClick: () -> Unit,
    onScalesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PracticeHubScreen(
        onSightReadingClick = onSightReadingClick,
        onScalesClick = onScalesClick,
        modifier = modifier
    )
}
