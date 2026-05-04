package com.clefrun.app.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clefrun.app.domain.exerciseplan.CoachContent
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Paper
import com.clefrun.app.ui.theme.TextSecondary
import com.clefrun.app.ui.theme.WarmAccent

@Composable
internal fun CoachTipPopup(
    coach: CoachContent,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Paper,
        contentColor = Charcoal,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        modifier = modifier.widthIn(min = 252.dp, max = 320.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = WarmAccent,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = coach.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Charcoal,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close coach tip",
                        tint = Charcoal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            FocusRow(focus = coach.focusLabel)

            Text(
                text = coach.body,
                style = MaterialTheme.typography.bodyMedium,
                color = Charcoal
            )

            coach.watchOut?.let { watchOut ->
                Text(
                    text = watchOut,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FocusRow(
    focus: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Focus:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = WarmAccent
        )
        Text(
            text = focus,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = Charcoal
        )
    }
}
