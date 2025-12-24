package com.saralapps.composemultiplatformwebview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import com.saralapps.composemultiplatformwebview.data.WebViewAvailability
import com.saralapps.composemultiplatformwebview.util.encodeUrlParams

/**
 * Creates and remembers a PlatformWebViewState for Android.
 */
@Composable
actual fun rememberPlatformWebViewState(
    url: String?,
    javaScriptEnabled: Boolean,
    allowsFileAccess: Boolean,
    onNavigating: ((url: String) -> Boolean)?
): PlatformWebViewState {
    val encodedUrl = url.encodeUrlParams()
    return remember(url, javaScriptEnabled, allowsFileAccess) {
        PlatformWebViewState(
            initialUrl = encodedUrl,
            javaScriptEnabled = javaScriptEnabled,
            allowsFileAccess = allowsFileAccess,
            navigationInterceptor = onNavigating
        )
    }
}

/**
 * Android WebView composable with state management.
 */
@Composable
actual fun PlatformWebView(
    state: PlatformWebViewState,
    modifier: Modifier,
    placeholderColor: Color,
    onUrlChanged: ((String) -> Unit)?,
    onCreated: (() -> Unit)?,
    onDisposed: (() -> Unit)?,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)?
) {
    var webViewKey by remember { mutableStateOf(0) }

    LaunchedEffect(state.currentUrl) {
        state.currentUrl?.let { url ->
            onUrlChanged?.invoke(url)
        }
    }

    BackHandler(enabled = state.canGoBack) {
        state.goBack()
    }

    BoxWithConstraints(modifier = modifier) {
        val width = if (constraints.hasFixedWidth) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val height = if (constraints.hasFixedHeight) {
            ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }

        AndroidView(
            modifier = Modifier.matchParentSize(),
            factory = { ctx ->
                createWebView(
                    context = ctx,
                    state = state,
                    backgroundColor = placeholderColor,
                    width = width,
                    height = height,
                    onCreated = onCreated,
                    onRendererCrash = {
                        webViewKey++
                    }
                )
            },
            update = { webView ->
                // Update if settings changed
                webView.settings.javaScriptEnabled = state.javaScriptEnabled
                webView.settings.allowFileAccess = state.allowsFileAccess
            },
            onReset = { },
            onRelease = { webView ->
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.stopLoading()
                webView.clearHistory()
                webView.clearCache(true)
                webView.loadUrl("about:blank")
                webView.onPause()
                webView.removeAllViews()
                webView.destroyDrawingCache()
                webView.destroy()
                state.webView = null
                onDisposed?.invoke()
            }
        )
    }
}

/**
 * Convenience composable that creates its own state.
 */
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
    javaScriptEnabled: Boolean,
    allowsFileAccess: Boolean,
    placeholderColor: Color,
    onUrlChanged: ((String) -> Unit)?,
    onNavigating: ((String) -> Boolean)?,
    onCreated: (() -> Unit)?,
    onDisposed: (() -> Unit)?,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)?
) {
    val encodedUrl = url.encodeUrlParams()

    val state = rememberPlatformWebViewState(
        url = encodedUrl,
        javaScriptEnabled = javaScriptEnabled,
        allowsFileAccess = allowsFileAccess,
        onNavigating = onNavigating
    )

    PlatformWebView(
        state = state,
        modifier = modifier,
        placeholderColor = placeholderColor,
        onUrlChanged = onUrlChanged,
        onCreated = onCreated,
        onDisposed = onDisposed,
        onUnavailable = onUnavailable
    )
}

/**
 * Creates and configures the Android WebView.
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createWebView(
    context: Context,
    state: PlatformWebViewState,
    backgroundColor: Color,
    width: Int,
    height: Int,
    onCreated: (() -> Unit)?,
    onRendererCrash: () -> Unit
): WebView {
    return WebView(context).apply {
        // Store reference in state
        state.webView = this

        // Set layout params
        layoutParams = FrameLayout.LayoutParams(width, height)

        // Set background color
        setBackgroundColor(backgroundColor.toArgb())

        // Use hardware acceleration
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // Configure settings
        settings.apply {
            // JavaScript
            javaScriptEnabled = state.javaScriptEnabled
            javaScriptCanOpenWindowsAutomatically = true

            // File access
            allowFileAccess = state.allowsFileAccess
            allowContentAccess = true

            // DOM Storage (important for modern web apps)
            domStorageEnabled = true
            databaseEnabled = true

            // Zoom
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false

            // Viewport
            loadWithOverviewMode = true
            useWideViewPort = true

            // Media
            mediaPlaybackRequiresUserGesture = false

            // Cache
            cacheMode = WebSettings.LOAD_DEFAULT

            // Mixed content
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            // Text
            textZoom = 100

            // Disable safe browsing (can cause issues)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }

            // User agent - use desktop user agent for better compatibility
            userAgentString = userAgentString.replace("Mobile", "").replace("Android", "Linux")
        }

        // Set WebViewClient
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false

                // Check navigation interceptor
                if (!state.shouldAllowNavigation(url)) {
                    return true // Block navigation
                }

                return false // Allow WebView to handle it
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                state.setIsLoading(true)
                url?.let { state.setCurrentUrl(it) }
                state.updateNavigationState()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                state.setIsLoading(false)
                state.setNavigatingUrl(null)
                url?.let { state.setCurrentUrl(it) }
                state.updateNavigationState()
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(view, url, isReload)
                state.updateNavigationState()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    android.util.Log.e("PlatformWebView", "WebView error: ${error?.description}")
                }
            }

            // Handle renderer crashes (API 26+)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                android.util.Log.e("PlatformWebView", "Renderer crashed! didCrash=${detail?.didCrash()}")

                // Clean up the crashed WebView
                view?.let { wv ->
                    (wv.parent as? ViewGroup)?.removeView(wv)
                    wv.destroy()
                }
                state.webView = null

                // Trigger recreation
                onRendererCrash()

                return true
            }
        }

        // Set WebChromeClient
        webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                state.setPageTitle(title)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                return super.onConsoleMessage(consoleMessage)
            }
        }

        state.pendingUrl?.let { url ->
            loadUrl(url)
        }

        onCreated?.invoke()
    }
}