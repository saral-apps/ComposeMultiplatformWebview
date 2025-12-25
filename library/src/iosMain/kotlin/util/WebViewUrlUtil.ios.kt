package com.saralapps.composemultiplatformwebview.util

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.create

/**
 * iOS implementation of URL utilities.
 */
actual object WebViewUrlUtils {

    /**
     * Encodes URL query parameters while preserving the base URL structure.
     */
    actual fun encodeUrlParams(url: String?): String? {
        if (url == null) return null

        return try {
            val nsUrl = NSURL.URLWithString(url) ?: return url
            val components = NSURLComponents(uRL = nsUrl, resolvingAgainstBaseURL = false)
                ?: return url

            // Get existing query items
            val queryItems = components.queryItems as? List<NSURLQueryItem> ?: return url

            if (queryItems.isEmpty()) {
                return url
            }

            // Re-encode each query item value
            val encodedItems = queryItems.map { item ->
                val name = item.name
                val value = item.value

                if (value != null) {
                    // Encode the value using NSString
                    val nsString = NSString.create(string = value)
                    val encodedValue = nsString.stringByAddingPercentEncodingWithAllowedCharacters(
                        NSCharacterSet.URLQueryAllowedCharacterSet
                    ) ?: value
                    NSURLQueryItem(name = name, value = encodedValue)
                } else {
                    item
                }
            }

            components.queryItems = encodedItems
            components.URL?.absoluteString ?: url
        } catch (e: Exception) {
            println("Error encoding URL params: ${e.message}")
            url
        }
    }
}