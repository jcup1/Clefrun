package com.clefrun.app.feature.sightreading

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun OptionsSheetContent(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    targetedPracticeText: String,
    onTargetedPracticeTextChange: (String) -> Unit,
    onTargetedPracticeFocused: suspend () -> Unit,
    tempo: Float,
    onTempoChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
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

                OutlinedTextField(
                    value = targetedPracticeText,
                    onValueChange = onTargetedPracticeTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .bringIntoViewRequester(bringIntoViewRequester)
                        .onFocusEvent { focusState ->
                            if (focusState.isFocused) {
                                scope.launch {
                                    onTargetedPracticeFocused()
                                    delay(TargetedPracticeBringIntoViewDelayMillis)
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                    label = { Text("Targeted practice") },
                    placeholder = { Text("e.g. left hand, accidentals, small jumps") },
                    supportingText = { Text("Used for the next exercise.") },
                    minLines = 3,
                    maxLines = 5,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Charcoal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Charcoal,
                        unfocusedTextColor = Charcoal,
                        focusedContainerColor = Paper,
                        unfocusedContainerColor = Paper,
                        focusedBorderColor = WarmAccent,
                        unfocusedBorderColor = Stroke,
                        focusedLabelColor = WarmAccent,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = WarmAccent,
                        focusedPlaceholderColor = TextSecondary,
                        unfocusedPlaceholderColor = TextSecondary,
                        focusedSupportingTextColor = TextSecondary,
                        unfocusedSupportingTextColor = TextSecondary
                    )
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

        Spacer(modifier = Modifier.height(36.dp))
    }
}

private const val TargetedPracticeBringIntoViewDelayMillis = 250L

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
