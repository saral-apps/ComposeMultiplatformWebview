package com.saralapps.composemultiplatformwebview.util

/**
 * Multiplatform URL utilities for WebView.
 */
expect object WebViewUrlUtils {
    /**
     * Encodes URL query parameters while preserving the base URL structure.
     *
     * @param url The URL string to encode
     * @return The URL with encoded query parameters, or the original URL if encoding fails
     */
    fun encodeUrlParams(url: String?): String?
}

/**
 * Extension function to encode URL query parameters.
 */
fun String?.encodeUrlParams(): String? = WebViewUrlUtils.encodeUrlParams(this)


































