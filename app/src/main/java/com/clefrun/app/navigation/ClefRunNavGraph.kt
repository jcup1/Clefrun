package com.clefrun.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.clefrun.app.feature.practicehub.PracticeHubRoute
import com.clefrun.app.feature.scales.ScalesRoute
import com.clefrun.app.feature.scales.ScalesViewModel
import com.clefrun.app.feature.sightreading.ScoreViewModel
import com.clefrun.app.feature.sightreading.SightReadingRoute

@Composable
fun ClefRunNavGraph(
    scoreViewModel: ScoreViewModel,
    scalesViewModel: ScalesViewModel,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(ClefRunDestination.PracticeHub)

    NavDisplay(
        backStack = backStack,
        modifier = modifier.fillMaxSize(),
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = { key ->
            when (key) {
                ClefRunDestination.PracticeHub -> NavEntry(key) {
                    PracticeHubRoute(
                        onSightReadingClick = {
                            backStack.add(ClefRunDestination.SightReading)
                        },
                        onScalesClick = {
                            backStack.add(ClefRunDestination.ScalesArpeggios)
                        }
                    )
                }

                ClefRunDestination.SightReading -> NavEntry(key) {
                    SightReadingRoute(
                        scoreViewModel = scoreViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                ClefRunDestination.ScalesArpeggios -> NavEntry(key) {
                    ScalesRoute(
                        viewModel = scalesViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> error("Unknown destination: $key")
            }
        }
    )
}
