package com.saralapps.composemultiplatformwebview.data

import android.os.Build
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Android implementation of PlatformWebViewState.
 * Wraps an Android WebView and provides reactive state.
 */
actual class PlatformWebViewState(
    initialUrl: String? = null,
    val javaScriptEnabled: Boolean = true,
    val allowsFileAccess: Boolean = true,
    private var navigationInterceptor: ((String) -> Boolean)? = null
) {
    /**
     * Reference to the actual Android WebView.
     * Set internally when the WebView is created.
     */
    internal var webView: WebView? = null

    /**
     * The URL that was requested to load initially.
     */
    internal val pendingUrl: String? = initialUrl

    // Backing fields for reactive state
    private var _currentUrl: String? by mutableStateOf(initialUrl)
    private var _navigatingUrl: String? by mutableStateOf(null)
    private var _isLoading: Boolean by mutableStateOf(false)
    private var _canGoBack: Boolean by mutableStateOf(false)
    private var _canGoForward: Boolean by mutableStateOf(false)
    private var _pageTitle: String? by mutableStateOf(null)

    /**
     * The current URL being displayed.
     */
    actual val currentUrl: String?
        get() = _currentUrl

    /**
     * The URL currently being navigated to.
     */
    actual val navigatingUrl: String?
        get() = _navigatingUrl

    /**
     * Whether the WebView is currently loading a page.
     */
    actual val isLoading: Boolean
        get() = _isLoading

    /**
     * Whether back navigation is available.
     */
    actual val canGoBack: Boolean
        get() = _canGoBack

    /**
     * Whether forward navigation is available.
     */
    actual val canGoForward: Boolean
        get() = _canGoForward

    /**
     * The title of the current page.
     */
    actual val pageTitle: String?
        get() = _pageTitle

    /**
     * Loads a URL in the WebView.
     */
    actual fun loadUrl(url: String) {
        _currentUrl = url
        webView?.loadUrl(url)
    }

    /**
     * Loads HTML content directly.
     */
    actual fun loadHtml(html: String, baseUrl: String?) {
        webView?.loadDataWithBaseURL(
            baseUrl,
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    /**
     * Navigates back in history.
     */
    actual fun goBack() {
        if (_canGoBack) {
            webView?.goBack()
        }
    }

    /**
     * Navigates forward in history.
     */
    actual fun goForward() {
        if (_canGoForward) {
            webView?.goForward()
        }
    }

    /**
     * Reloads the current page.
     */
    actual fun reload() {
        webView?.reload()
    }

    /**
     * Stops loading the current page.
     */
    actual fun stopLoading() {
        webView?.stopLoading()
        _isLoading = false
    }

    /**
     * Evaluates JavaScript in the WebView.
     */
    actual fun evaluateJavaScript(code: String) {
        webView?.evaluateJavascript(code, null)
    }

    /**
     * Sets a navigation interceptor.
     * Return true to allow navigation, false to block it.
     */
    actual fun setNavigationInterceptor(interceptor: ((String) -> Boolean)?) {
        navigationInterceptor = interceptor
    }

    /**
     * Called internally to check if navigation should be allowed.
     */
    internal fun shouldAllowNavigation(url: String): Boolean {
        _navigatingUrl = url
        return navigationInterceptor?.invoke(url) ?: true
    }

    /**
     * Updates navigation state from the WebView.
     */
    internal fun updateNavigationState() {
        webView?.let { wv ->
            _canGoBack = wv.canGoBack()
            _canGoForward = wv.canGoForward()
        }
    }

    // Internal setters for WebViewClient callbacks
    internal fun setCurrentUrl(url: String?) {
        _currentUrl = url
    }

    internal fun setIsLoading(loading: Boolean) {
        _isLoading = loading
    }

    internal fun setNavigatingUrl(url: String?) {
        _navigatingUrl = url
    }

    internal fun setPageTitle(title: String?) {
        _pageTitle = title
    }
}
