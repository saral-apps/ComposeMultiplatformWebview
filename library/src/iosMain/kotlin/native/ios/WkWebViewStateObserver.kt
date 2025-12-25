package com.saralapps.composemultiplatformwebview.native.ios

import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.WebKit.WKWebView

/**
 * Updates state from WKWebView.
 * Called periodically or after navigation events.
 */
@OptIn(ExperimentalForeignApi::class)
fun updateStateFromWebView(
    webView: WKWebView,
    state: PlatformWebViewState,
    onUrlChanged: ((String) -> Unit)?
) {
    // Update URL
    val currentUrl = webView.URL?.absoluteString
    if (currentUrl != null && currentUrl != state.currentUrl) {
        state.setCurrentUrl(currentUrl)
        onUrlChanged?.invoke(currentUrl)
    }

    // Update title
    webView.title?.let { title ->
        if (title != state.pageTitle) {
            state.setPageTitle(title)
        }
    }

    // Update navigation state
    state.setCanGoBack(webView.canGoBack)
    state.setCanGoForward(webView.canGoForward)

    // Update loading state
    state.setIsLoading(webView.loading)
}