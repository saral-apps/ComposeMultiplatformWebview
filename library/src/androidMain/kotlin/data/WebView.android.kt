package com.saralapps.composemultiplatformwebview.data

actual fun checkWebViewAvailability(): WebViewAvailability {
    return WebViewAvailability(
        isAvailable = true,
        platform = "Android",
        version = "WebView (Built-in)"
    )
}