package com.fei.firstapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OnlineSiteActivity : ComponentActivity() {
    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var siteUrl by remember { mutableStateOf<String?>(null) }

            if (siteUrl == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            webViewClient = WebViewClient()
                            webChromeClient = WebChromeClient()
                            loadUrl(siteUrl!!)
                            webView = this
                        }
                    }
                )
            }

            DisposableEffect(Unit) {
                lifecycleScope.launch {
                    try {
                        siteUrl = UserRepository(applicationContext).getVipSiteUrl()
                    } catch (error: Exception) {
                        Toast.makeText(
                            this@OnlineSiteActivity,
                            "VIP 页面获取失败：${error.message ?: "请求失败"}",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                }

                onDispose {
                    webView?.apply {
                        stopLoading()
                        loadUrl("about:blank")
                        destroy()
                    }
                    webView = null
                }
            }
        }
    }
}
