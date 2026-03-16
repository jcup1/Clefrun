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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.clefrun.app.ui.theme.ClefrunTheme
import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.RuleBasedGenerator
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClefrunTheme {
                ScoreRenderScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun ScoreRenderScreen(modifier: Modifier = Modifier) {
    ScoreWebView(modifier = modifier)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ScoreWebView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pageLoaded by remember { mutableStateOf(false) }
    var seedCounter by remember { mutableLongStateOf(1L) }
    var pendingXml by remember {
        mutableStateOf<String?>(generateExerciseXml(seedCounter).also { seedCounter += 1 })
    }

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

    Box(
        modifier = modifier
            .background(ReaderStone)
            .fillMaxSize()
    ) {
        AndroidWebView(
            webView = webView,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 12.dp)
                .offset(y = 2.dp)
                .clickable {
                    val xml = generateExerciseXml(seedCounter)
                    seedCounter += 1
                    if (pageLoaded) {
                        renderMusicXml(webView, xml)
                    } else {
                        pendingXml = xml
                    }
                },
            shape = RoundedCornerShape(100.dp),
            color = PillBackground,
            shadowElevation = 8.dp,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(7.dp)
                        .background(PillAccent, CircleShape)
                )
                Text(
                    text = "New",
                    color = CharcoalInk,
                    modifier = Modifier.padding(start = 14.dp)
                )
            }
        }
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

private val ReaderStone = Color(0xFFF1EEE7)
private val CharcoalInk = Color(0xFF2F2A24)
private val PillAccent = Color(0xFF28566A)
private val PillBackground = Color(0xEBFFF9F0)

@Preview(showBackground = true)
@Composable
fun ScoreRenderScreenPreview() {
    ClefrunTheme {
        ScoreRenderScreen(
            modifier = Modifier.height(640.dp)
        )
    }
}

@Composable
private fun AndroidWebView(webView: WebView, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { webView },
        modifier = modifier
    )
}
