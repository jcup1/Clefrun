package com.clefrun.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.WarmAccent

@Composable
fun ClefRunLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 26.sp,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = Charcoal,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("Clef")
            }
            withStyle(
                style = SpanStyle(
                    color = WarmAccent,
                    fontWeight = FontWeight.Medium
                )
            ) {
                append("Run")
            }
        },
        fontSize = fontSize,
        modifier = modifier
    )
}
