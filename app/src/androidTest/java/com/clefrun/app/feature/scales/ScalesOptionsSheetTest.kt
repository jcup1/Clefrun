package com.clefrun.app.feature.scales

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.clefrun.core.PracticeMode
import com.clefrun.core.PracticeTonic
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Rule
import org.junit.Test

class ScalesOptionsSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selecting_tonic_updates_selected_chip() {
        composeRule.setContent {
            var selected by remember { mutableStateOf(PracticeTonic.F) }

            ScalesOptionsSheet(
                selectedMode = PracticeMode.MAJOR,
                selectedTonic = selected,
                supportedModes = persistentSetOf(PracticeMode.MAJOR),
                supportedTonics = persistentSetOf(
                    PracticeTonic.F,
                    PracticeTonic.C
                ),
                onModeSelected = {},
                onTonicSelected = { selected = it },
            )
        }

        composeRule
            .onNodeWithTag("tonic-chip-C")
            .performClick()

        composeRule
            .onNodeWithTag("tonic-chip-C")
            .assertIsSelected()
    }

}