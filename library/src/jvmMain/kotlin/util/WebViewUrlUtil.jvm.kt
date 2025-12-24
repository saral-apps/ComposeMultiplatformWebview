package com.saralapps.composemultiplatformwebview.util

import java.net.URL
import java.net.URLEncoder

actual object WebViewUrlUtils {
    /**
     * Encodes URL query parameters while preserving the base URL structure.
     */
    actual fun encodeUrlParams(url: String?): String? {
        if (url == null) return null

        return try {
            val uri = URL(url)
            val baseUrl = buildString {
                append(uri.protocol)
                append("://")
                append(uri.host)
                if (uri.port != -1) {
                    append(":")
                    append(uri.port)
                }
                append(uri.path)
            }
            val query = uri.query

            if (query != null) {
                val encodedParams = query.split("&").joinToString("&") { param ->
                    val parts = param.split("=", limit = 2)
                    if (parts.size == 2) {
                        "${parts[0]}=${URLEncoder.encode(parts[1], "UTF-8")}"
                    } else {
                        param
                    }
                }
                "$baseUrl?$encodedParams"
            } else {
                url
            }
        } catch (e: Exception) {
            e.printStackTrace()
            url
        }
    }

}