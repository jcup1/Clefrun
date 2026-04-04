package com.clefrun.app.feature.scales

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScalesRoute(
    viewModel: ScalesViewModel,
    modifier: Modifier = Modifier,
) {
    ScalesScreen(
        viewModel = viewModel,
        modifier = modifier
    )
}
