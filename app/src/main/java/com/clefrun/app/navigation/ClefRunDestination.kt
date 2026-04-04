package com.clefrun.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ClefRunDestination : NavKey {
    @Serializable
    data object PracticeHub : ClefRunDestination

    @Serializable
    data object SightReading : ClefRunDestination

    @Serializable
    data object ScalesArpeggios : ClefRunDestination
}
