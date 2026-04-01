package com.clefrun.app

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clefrun.app.ui.theme.AppBackground
import com.clefrun.app.ui.theme.Charcoal
import com.clefrun.app.ui.theme.ClefrunTheme
import com.clefrun.app.ui.theme.Divider
import com.clefrun.app.ui.theme.Panel
import com.clefrun.app.ui.theme.Paper
import com.clefrun.app.ui.theme.SelectedFill
import com.clefrun.app.ui.theme.Stroke
import com.clefrun.app.ui.theme.TextPrimary
import com.clefrun.app.ui.theme.TextSecondary
import com.clefrun.app.ui.theme.WarmAccent
import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.RuleBasedGenerator
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var regenerateSignal by remember { mutableLongStateOf(0L) }

            ClefrunTheme {
                ScoreRenderScreen(
                    regenerateSignal = regenerateSignal,
                    onRegenerate = { regenerateSignal++ }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ScoreWebView(
    regenerateSignal: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pageLoaded by remember { mutableStateOf(false) }
    var seedCounter by remember { mutableLongStateOf(2L) }
    var pendingXml by remember { mutableStateOf<String?>(generateExerciseXml(1L)) }

    val webView = remember(context) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return !isAllowedNavigation(request?.url)
                }

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return !isAllowedNavigation(url?.let(Uri::parse))
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    pageLoaded = true
                    pendingXml?.let { xml ->
                        pendingXml = null
                        renderMusicXml(this@apply, xml)
                    }
                }
            }
            loadUrl(SCORE_HTML_ASSET_URL)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }

    LaunchedEffect(regenerateSignal) {
        if (regenerateSignal == 0L) return@LaunchedEffect
        val xml = generateExerciseXml(seedCounter)
        seedCounter += 1
        if (pageLoaded) {
            renderMusicXml(webView, xml)
        } else {
            pendingXml = xml
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        AndroidWebView(
            webView = webView,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize()
        )
    }
}

private fun generateExerciseXml(seed: Long): String {
    val exercise = RuleBasedGenerator.generate(seed)
    return MusicXmlWriter.write(exercise)
}

private fun renderMusicXml(webView: WebView, xmlString: String) {
    val javascript = "window.renderMusicXml(${JSONObject.quote(xmlString)});"
    webView.evaluateJavascript(javascript, null)
}

private fun isAllowedNavigation(uri: Uri?): Boolean {
    if (uri == null) return false
    val isAsset = uri.toString() == SCORE_HTML_ASSET_URL
    val isAllowedCdn = uri.scheme == "https" && uri.host == ALLOWED_CDN_HOST
    val isAboutBlank = uri.toString() == "about:blank"
    return isAsset || isAllowedCdn || isAboutBlank
}

private const val SCORE_HTML_ASSET_URL = "file:///android_asset/score.html"
private const val ALLOWED_CDN_HOST = "cdn.jsdelivr.net"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreRenderScreen(
    regenerateSignal: Long,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDifficulty by remember { mutableStateOf(DifficultyUi.EASY) }
    var tempo by remember { mutableFloatStateOf(0.55f) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = false
        )
    )
    val scope = rememberCoroutineScope()

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 86.dp,
        sheetContainerColor = Paper,
        sheetContentColor = Charcoal,
        sheetShadowElevation = 12.dp,
        sheetDragHandle = {
            Surface(
                color = Divider,
                shape = RoundedCornerShape(999.dp),
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 8.dp)
                    .size(width = 56.dp, height = 6.dp)
            ) {}
        },
        sheetContent = {
            OptionsSheetContent(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = { selectedDifficulty = it },
                tempo = tempo,
                onTempoChange = { tempo = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            )
        },
        containerColor = AppBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(innerPadding)
        ) {
            TopOverlayBar(
                onNewClick = onRegenerate,
                onOptionsClick = {
                    scope.launch {
                        val sheetState = scaffoldState.bottomSheetState
                        if (sheetState.currentValue == SheetValue.Expanded) {
                            sheetState.partialExpand()
                        } else {
                            sheetState.expand()
                        }
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Paper)
            ) {
                ScoreWebView(
                    regenerateSignal = regenerateSignal,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun TopOverlayBar(
    onNewClick: () -> Unit,
    onOptionsClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClefRunLogo()

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NewExerciseButton(onClick = onNewClick)
            OptionsButton(onClick = onOptionsClick)
        }
    }
}

@Composable
private fun ClefRunLogo() {
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
        fontSize = 26.sp
    )
}

@Composable
private fun NewExerciseButton(
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Paper,
        contentColor = Charcoal,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = "New exercise",
                tint = Charcoal
            )
            Text(
                text = "New",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OptionsButton(
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Paper,
        contentColor = Charcoal,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier.size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = "Exercise options",
                tint = Charcoal
            )
        }
    }
}

@Composable
private fun OptionsSheetContent(
    selectedDifficulty: DifficultyUi,
    onDifficultySelected: (DifficultyUi) -> Unit,
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
                    enabled = false,
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
                    enabled = false,
                    colors = SliderDefaults.colors(
                        thumbColor = WarmAccent,
                        activeTrackColor = WarmAccent,
                        inactiveTrackColor = Divider
                    )
                )

                Text(
                    text = "Difficulty and tempo controls are preview-only for now.",
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
    selected: DifficultyUi,
    onSelected: (DifficultyUi) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val options = DifficultyUi.entries

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option,
                onClick = { onSelected(option) },
                enabled = enabled,
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

private enum class DifficultyUi(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Liszt"),
}

@Composable
private fun AndroidWebView(webView: WebView, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
