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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.clefrun.app.ui.theme.ClefrunTheme
import com.clefrun.core.MusicXmlWriter
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClefrunTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScoreRenderScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreRenderScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "WebView OSMD Render Test")
        ScoreWebView(
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ScoreWebView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var pageLoaded by remember { mutableStateOf(false) }
    var pendingRender by remember { mutableStateOf(false) }
    var renderRequests by remember { mutableIntStateOf(0) }

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
                    if (pendingRender) {
                        pendingRender = false
                        renderMusicXml(this@apply)
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

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {
                renderRequests += 1
                if (pageLoaded) {
                    renderMusicXml(webView)
                } else {
                    pendingRender = true
                }
            }
        ) {
            Text("Render test score")
        }

        AndroidWebView(
            webView = webView,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        if (renderRequests == 0) {
            Text(text = "Tap the button to render the hardcoded MusicXML grand staff example.")
        }
    }
}

@Composable
private fun AndroidWebView(webView: WebView, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

private fun renderMusicXml(webView: WebView) {
    val javascript = "window.renderMusicXml(${JSONObject.quote(MusicXmlWriter.writeTestExercise())});"
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

@Preview(showBackground = true)
@Composable
fun ScoreRenderScreenPreview() {
    ClefrunTheme {
        ScoreRenderScreen(
            modifier = Modifier.height(640.dp)
        )
    }
}
