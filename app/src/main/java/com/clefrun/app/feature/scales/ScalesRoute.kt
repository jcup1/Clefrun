package com.clefrun.app.feature.scales

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ScalesRoute(
    modifier: Modifier = Modifier,
    viewModel: ScalesViewModel = hiltViewModel(),
) {
    ScalesScreen(
        viewModel = viewModel,
        modifier = modifier
    )
}
