package com.clefrun.app.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.Paper
import com.clefrun.app.ui.theme.WarmAccent

@Composable
internal fun CoachBubble(
    showUnreadBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(60.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = Paper,
            contentColor = Charcoal,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.size(54.dp)
        ) {
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Coach tip",
                    tint = WarmAccent
                )
            }
        }

        if (showUnreadBadge) {
            val pulse = rememberInfiniteTransition(label = "CoachUnreadBadgePulse")
            val badgeScale by pulse.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "CoachUnreadBadgeScale"
            )
            val badgeAlpha by pulse.animateFloat(
                initialValue = 1f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1100),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "CoachUnreadBadgeAlpha"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .scale(badgeScale)
                    .alpha(badgeAlpha)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Paper),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WarmAccent)
                )
            }
        }
    }
}
