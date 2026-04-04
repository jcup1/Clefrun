package com.clefrun.app.feature.sightreading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.clefrun.app.ui.theme.Paper

@Composable
internal fun ScoreSurface(
    musicXml: String,
    showMeasureNumbers: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Paper)
    ) {
        ScoreWebView(
            musicXml = musicXml,
            showMeasureNumbers = showMeasureNumbers,
            modifier = Modifier.fillMaxSize()
        )
    }
}
