package com.saralapps.composemultiplatformwebview.data

/**
 * Cross-platform WebView state
 */
expect class PlatformWebViewState {
    val currentUrl: String?
    val navigatingUrl: String?
    val isLoading: Boolean
    val canGoBack: Boolean
    val canGoForward: Boolean
    val pageTitle: String?

    fun loadUrl(url: String)
    fun loadHtml(html: String, baseUrl: String? = null)
    fun goBack()
    fun goForward()
    fun reload()
    fun stopLoading()
    fun evaluateJavaScript(code: String)
    fun setNavigationInterceptor(interceptor: ((String) -> Boolean)?)
}