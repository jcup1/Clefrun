package com.clefrun.app

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.clefrun.core.Difficulty
import com.clefrun.core.MusicXmlWriter
import com.clefrun.core.RuleBasedGenerator
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScoreWebView(
    musicXml: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var pageLoaded by remember { mutableStateOf(false) }
    var pendingXml by remember { mutableStateOf<String?>(musicXml) }

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

    LaunchedEffect(musicXml, pageLoaded) {
        if (pageLoaded) {
            pendingXml = null
            renderMusicXml(webView, musicXml)
        } else {
            pendingXml = musicXml
        }
    }

    Box(
        modifier = modifier
            .background(Color.White)
            .fillMaxSize()
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxSize()
        )
    }
}

internal fun generateExerciseXml(seed: Long, difficulty: Difficulty): String {
    val exercise = RuleBasedGenerator.generate(seed, difficulty)
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
