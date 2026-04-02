package com.clefrun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.clefrun.app.ui.theme.ClefrunTheme

class MainActivity : ComponentActivity() {
    private val scoreViewModel: ScoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClefrunTheme {
                ScoreRenderScreen(
                    scoreViewModel = scoreViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
