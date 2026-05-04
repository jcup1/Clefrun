package com.clefrun.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clefrun.app.navigation.ClefRunNavGraph
import com.clefrun.app.ui.theme.ClefrunTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClefrunTheme {
                ClefRunNavGraph()
            }
        }
    }
}
