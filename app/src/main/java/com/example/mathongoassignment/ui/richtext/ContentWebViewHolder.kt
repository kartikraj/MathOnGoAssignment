package com.example.mathongoassignment.ui.richtext

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

data class ContentTheme(
    val textColor: String,
    val mutedColor: String,
    val borderColor: String,
    val fontSizePx: Int,
    val lineHeight: Float,
) {
    fun toJson(): String = JSONObject()
        .put("text", textColor)
        .put("muted", mutedColor)
        .put("border", borderColor)
        .put("fontSize", "${fontSizePx}px")
        .put("lineHeight", lineHeight.toString())
        .toString()
}

class ContentWebViewHolder(private val context: Context) {

    private val _contentHeightCssPx = MutableStateFlow(0)

    /** Height of the rendered content in CSS pixels, which equals dp for this viewport. */
    val contentHeightCssPx: StateFlow<Int> = _contentHeightCssPx.asStateFlow()

    private var isPageLoaded = false
    private var pendingHtml: String? = null
    private var pendingTheme: ContentTheme? = null
    private var currentHtml: String? = null
    private var currentTheme: ContentTheme? = null
    private var consecutiveCrashes = 0

    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    private val _webView = MutableStateFlow(createWebView())

    val webView: StateFlow<WebView> = _webView.asStateFlow()

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView(): WebView = NonInteractiveWebView(context).apply {
        settings.javaScriptEnabled = true
        // MathJax keeps its font cache in localStorage.
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = false
        settings.useWideViewPort = false
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        // The page itself is served from assets over https; only images come from the network.
        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = WebView.OVER_SCROLL_NEVER
        setBackgroundColor(Color.TRANSPARENT)
        addJavascriptInterface(Bridge(), BRIDGE_NAME)
        webViewClient = object : WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest,
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

            override fun onPageFinished(view: WebView, url: String) {
                if (view !== _webView.value) return
                isPageLoaded = true
                pendingHtml?.let { html -> render(html, pendingTheme) }
                pendingHtml = null
            }

            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                onRendererGone(view, didCrash = detail.didCrash())
                return true
            }
        }
        loadUrl(CONTENT_URL)
    }

    /** Pushes new content into the page; a no-op when the same content is already shown. */
    fun setContent(html: String, theme: ContentTheme) {
        val themeChanged = theme != currentTheme
        if (html == currentHtml && !themeChanged) return
        if (html != currentHtml) {
            consecutiveCrashes = 0
            _contentHeightCssPx.value = 0
        }
        currentHtml = html
        currentTheme = theme
        if (isPageLoaded) {
            render(html, theme)
        } else {
            pendingHtml = html
            pendingTheme = theme
        }
    }

    private fun render(html: String, theme: ContentTheme?) {
        val themeJson = (theme ?: currentTheme)?.toJson() ?: "null"
        val script = "window.renderContent(${JSONObject.quote(html)}, $themeJson);"
        _webView.value.evaluateJavascript(script, null)
    }

    private fun onRendererGone(view: WebView, didCrash: Boolean) {
        if (view !== _webView.value) return
        Log.w(TAG, "WebView renderer gone (crashed=$didCrash), recreating")
        release(view)
        isPageLoaded = false
        if (didCrash) consecutiveCrashes++
        pendingHtml = currentHtml.takeIf { consecutiveCrashes <= MAX_CRASH_RETRIES }
        pendingTheme = currentTheme
        _webView.value = createWebView()
    }

    fun destroy() = release(_webView.value)

    private fun release(view: WebView) {
        view.removeJavascriptInterface(BRIDGE_NAME)
        (view.parent as? ViewGroup)?.removeView(view)
        view.destroy()
    }

    private inner class Bridge {
        @JavascriptInterface
        fun onHeightChanged(heightCssPx: Int) {
            _contentHeightCssPx.value = heightCssPx
        }
    }

    /**
     * Content is read-only, so the view forwards every touch to its parent. That keeps the page
     * scrollable and option cards clickable even where a WebView covers them.
     */
    private class NonInteractiveWebView(context: Context) : WebView(context) {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(event: MotionEvent): Boolean = false
    }

    private companion object {
        const val TAG = "ContentWebView"
        const val BRIDGE_NAME = "AndroidBridge"
        const val MAX_CRASH_RETRIES = 1
        const val CONTENT_URL = "https://appassets.androidplatform.net/assets/content/index.html"
    }
}
