package com.clefrun.app.feature.practicehub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Divider
import com.clefrun.app.ui.theme.Paper
import com.clefrun.app.ui.theme.TextSecondary
import com.clefrun.app.ui.theme.WarmAccent

internal enum class PracticeModeMotif {
    SIGHT_READING,
    SCALES,
}

@Composable
internal fun PracticeModeCard(
    title: String,
    subtitle: String,
    motif: PracticeModeMotif,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        color = Paper,
        contentColor = Charcoal,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(172.dp)
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Charcoal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    )

                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                PracticeCardMotif(
                    motif = motif,
                    modifier = Modifier
                        .padding(start = 18.dp)
                        .size(width = 132.dp, height = 110.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = Paper,
                contentColor = Charcoal,
                tonalElevation = 0.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "Enter",
                        tint = Charcoal
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeCardMotif(
    motif: PracticeModeMotif,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val lineColor = WarmAccent.copy(alpha = 0.38f)
        val noteColor = WarmAccent.copy(alpha = 0.62f)
        val helperColor = Divider.copy(alpha = 0.8f)

        when (motif) {
            PracticeModeMotif.SIGHT_READING -> {
                val lineSpacing = size.height / 11f
                repeat(5) { index ->
                    val y = lineSpacing * (index + 2)
                    drawLine(
                        color = helperColor,
                        start = Offset(18f, y),
                        end = Offset(size.width - 8f, y),
                        strokeWidth = 2f
                    )
                }

                drawLine(
                    color = lineColor,
                    start = Offset(24f, lineSpacing * 1.4f),
                    end = Offset(24f, lineSpacing * 6.2f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = lineColor,
                    radius = 10f,
                    center = Offset(34f, lineSpacing * 4.2f),
                    style = Stroke(width = 3f)
                )

                val notes = listOf(
                    Offset(size.width * 0.50f, lineSpacing * 3.0f),
                    Offset(size.width * 0.64f, lineSpacing * 2.5f),
                    Offset(size.width * 0.79f, lineSpacing * 3.6f)
                )
                notes.forEach { center ->
                    drawCircle(color = noteColor, radius = 8f, center = center)
                    drawLine(
                        color = noteColor,
                        start = Offset(center.x + 8f, center.y),
                        end = Offset(center.x + 8f, center.y - 34f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }

            PracticeModeMotif.SCALES -> {
                val startX = 10f
                val step = size.width / 5.4f
                val noteCenters = List(5) { index ->
                    Offset(
                        x = startX + step * index,
                        y = size.height * (0.76f - index * 0.11f)
                    )
                }

                noteCenters.zipWithNext().forEach { (start, end) ->
                    drawLine(
                        color = helperColor,
                        start = Offset(start.x, start.y + 2f),
                        end = Offset(end.x, end.y + 2f),
                        strokeWidth = 2f
                    )
                }

                noteCenters.forEach { center ->
                    drawCircle(color = noteColor, radius = 8f, center = center)
                    drawLine(
                        color = noteColor,
                        start = Offset(center.x + 8f, center.y),
                        end = Offset(center.x + 8f, center.y - 30f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }

                val chipWidth = 24.dp.toPx()
                val chipHeight = 18.dp.toPx()
                val chipTop = size.height * 0.66f
                drawRoundRect(
                    color = WarmAccent.copy(alpha = 0.14f),
                    topLeft = Offset(size.width * 0.54f, chipTop),
                    size = androidx.compose.ui.geometry.Size(chipWidth, chipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
                drawRoundRect(
                    color = WarmAccent.copy(alpha = 0.20f),
                    topLeft = Offset(size.width * 0.72f, chipTop - 10f),
                    size = androidx.compose.ui.geometry.Size(chipWidth, chipHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
            }
        }
    }
}
