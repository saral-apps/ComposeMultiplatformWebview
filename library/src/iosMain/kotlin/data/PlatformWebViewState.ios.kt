package com.saralapps.composemultiplatformwebview.data

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.WebKit.WKWebView

/**
 * iOS implementation of PlatformWebViewState.
 * Wraps a WKWebView and provides reactive state.
 */
@OptIn(ExperimentalForeignApi::class)
actual class PlatformWebViewState(
    initialUrl: String? = null,
    val javaScriptEnabled: Boolean = true,
    val allowsFileAccess: Boolean = true,
    private var navigationInterceptor: ((String) -> Boolean)? = null
) {
    /**
     * Reference to the actual WKWebView.
     * Set internally when the WebView is created.
     */
    internal var webView: WKWebView? = null

    /**
     * The URL that was requested to load initially.
     */
    internal val pendingUrl: String? = initialUrl

    // Internal state variables
    private var _currentUrl: String? = initialUrl
    private var _navigatingUrl: String? = null
    private var _isLoading: Boolean = false
    private var _canGoBack: Boolean = false
    private var _canGoForward: Boolean = false
    private var _pageTitle: String? = null

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
        webView?.let { wv ->
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl != null) {
                val request = platform.Foundation.NSURLRequest.requestWithURL(nsUrl)
                wv.loadRequest(request)
            }
        }
    }

    /**
     * Loads HTML content directly.
     */
    actual fun loadHtml(html: String, baseUrl: String?) {
        webView?.loadHTMLString(
            string = html,
            baseURL = baseUrl?.let { NSURL.URLWithString(it) }
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
        webView?.evaluateJavaScript(code) { _, _ -> }
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
            _canGoBack = wv.canGoBack
            _canGoForward = wv.canGoForward
        }
    }

    // Internal setters for observer callbacks
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

    internal fun setCanGoBack(canGoBack: Boolean) {
        _canGoBack = canGoBack
    }

    internal fun setCanGoForward(canGoForward: Boolean) {
        _canGoForward = canGoForward
    }
}