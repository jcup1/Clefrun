package com.clefrun.app.feature.sightreading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Divider
import com.clefrun.app.ui.theme.Panel
import com.clefrun.app.ui.theme.Paper
import com.clefrun.app.ui.theme.SelectedFill
import com.clefrun.app.ui.theme.Stroke
import com.clefrun.app.ui.theme.TextPrimary
import com.clefrun.app.ui.theme.TextSecondary
import com.clefrun.app.ui.theme.WarmAccent
import com.clefrun.core.Difficulty

@Composable
internal fun OptionsSheetContent(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    tempo: Float,
    onTempoChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Exercise settings",
            color = Charcoal,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Panel
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Difficulty",
                    color = Charcoal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                DifficultySelector(
                    selected = selectedDifficulty,
                    onSelected = onDifficultySelected,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = Divider)
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Tempo",
                    color = Charcoal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Slider(
                    value = tempo,
                    onValueChange = onTempoChange,
                    colors = SliderDefaults.colors(
                        thumbColor = WarmAccent,
                        activeTrackColor = WarmAccent,
                        inactiveTrackColor = Divider
                    )
                )

                Text(
                    text = "Tempo is preview-only for now.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DifficultySelector(
    selected: Difficulty,
    onSelected: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = Difficulty.entries

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelected(option) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = SelectedFill,
                    activeContentColor = TextPrimary,
                    activeBorderColor = Stroke,
                    inactiveContainerColor = Paper,
                    inactiveContentColor = TextSecondary,
                    inactiveBorderColor = Stroke
                ),
                label = {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

private val Difficulty.label: String
    get() = when (this) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HARD -> "Hard"
    }
