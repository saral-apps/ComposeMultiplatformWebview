package com.saralapps.composemultiplatformwebview.data

actual fun checkWebViewAvailability(): WebViewAvailability {
    return WebViewAvailability(
        isAvailable = false,
        platform = "iOS",
        errorMessage = "This package does not support iOS yet. Consider Compose-Webview-Multiplatform."
    )
}