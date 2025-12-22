package com.saralapps.composemultiplatformwebview.data

/**
 * Cross-platform WebView availability check result.
 */
data class WebViewAvailability(
    val isAvailable: Boolean,
    val platform: String,
    val version: String? = null,
    val errorMessage: String? = null,
    val downloadUrl: String? = null
)

/**
 * Checks WebView availability on the current platform.
 */
expect fun checkWebViewAvailability(): WebViewAvailability


