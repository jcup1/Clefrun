package com.clefrun.app.feature.practicehub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clefrun.app.ui.components.ClefRunLogo
import com.clefrun.app.ui.theme.AppBackground
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.TextSecondary

@Composable
internal fun PracticeHubScreen(
    onSightReadingClick: () -> Unit,
    onScalesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        ClefRunLogo(fontSize = 30.sp)

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Choose practice mode",
            color = TextSecondary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        PracticeModeCard(
            title = "Sight Reading",
            subtitle = "Generated reading exercises",
            motif = PracticeModeMotif.SIGHT_READING,
            onClick = onSightReadingClick,
            modifier = Modifier.fillMaxWidth()
        )

        PracticeModeCard(
            title = "Scales, Arpeggios & Cadences",
            subtitle = "Structured technical practice",
            motif = PracticeModeMotif.SCALES,
            onClick = onScalesClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f, fill = false))
    }
}
