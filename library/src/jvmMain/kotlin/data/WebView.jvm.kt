package com.saralapps.composemultiplatformwebview.data

import com.saralapps.composemultiplatformwebview.native.windows.WebView2Checker


/**
 * Platform detection utilities.
 */
internal object PlatformUtils {
    val isMacOS: Boolean by lazy {
        System.getProperty("os.name").lowercase().contains("native/mac")
    }

    val isWindows: Boolean by lazy {
        System.getProperty("os.name").lowercase().contains("win")
    }

    val isLinux: Boolean by lazy {
        System.getProperty("os.name").lowercase().contains("linux")
    }

    val osName: String by lazy {
        System.getProperty("os.name")
    }
}

actual fun checkWebViewAvailability(): WebViewAvailability {
    return when {
        PlatformUtils.isMacOS -> {
            WebViewAvailability(
                isAvailable = true,
                platform = "macOS",
                version = "WKWebView (Built-in)"
            )
        }
        PlatformUtils.isWindows -> {
            val result = WebView2Checker.check()
            WebViewAvailability(
                isAvailable = result.status == WebView2Checker.Status.AVAILABLE,
                platform = "Windows",
                version = result.version,
                errorMessage = result.errorMessage,
                downloadUrl = result.downloadUrl
            )
        }
        PlatformUtils.isLinux -> {
            WebViewAvailability(
                isAvailable = false,
                platform = "Linux",
                errorMessage = "Native WebView not supported on Linux. Consider using JCEF."
            )
        }
        else -> {
            WebViewAvailability(
                isAvailable = false,
                platform = PlatformUtils.osName,
                errorMessage = "Unsupported platform"
            )
        }
    }
}