package com.clefrun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.clefrun.app.feature.scales.ScalesViewModel
import com.clefrun.app.feature.sightreading.ScoreViewModel
import com.clefrun.app.navigation.ClefRunNavGraph
import com.clefrun.app.ui.theme.ClefrunTheme

class MainActivity : ComponentActivity() {
    private val scoreViewModel: ScoreViewModel by viewModels()
    private val scalesViewModel: ScalesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClefrunTheme {
                ClefRunNavGraph(
                    scoreViewModel = scoreViewModel,
                    scalesViewModel = scalesViewModel
                )
            }
        }
    }
}
