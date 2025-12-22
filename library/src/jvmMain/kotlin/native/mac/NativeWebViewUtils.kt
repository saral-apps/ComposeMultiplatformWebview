package com.saralapps.composemultiplatformwebview.native.mac

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import java.awt.KeyboardFocusManager
import java.awt.Window

/**
 * CompositionLocal that provides the current ComposeWindow.
 *
 * Usage in your Window composable:
 * ```
 * Window(onCloseRequest = ::exitApplication) {
 *     CompositionLocalProvider(LocalComposeWindow provides window) {
 *         // Your content here
 *         NativeWebView(url = "https://google.com")
 *     }
 * }
 * ```
 *
 * If you're using Compose's Window, you can get the window from LaunchedEffect:
 * ```
 * var composeWindow by remember { mutableStateOf<ComposeWindow?>(null) }
 *
 * LaunchedEffect(Unit) {
 *     composeWindow = findComposeWindow()
 * }
 * ```
 */
val LocalComposeWindow = staticCompositionLocalOf<ComposeWindow?> { null }

/**
 * Utility functions for working with native WebViews.
 */
object NativeWebViewUtils {

    /**
     * Checks if the native WebView library is available.
     * Call this before using any WebView functionality.
     */
    fun isAvailable(): Boolean {
        if (!MacWebViewNative.isMacOS) {
            return false
        }

        return try {
            // Try to create and immediately destroy a test WebView
            val testId = MacWebViewNative.INSTANCE.createWebView()
            if (testId != 0L) {
                MacWebViewNative.INSTANCE.destroyWebView(testId)
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            println("[NativeWebView] Library not available: ${e.message}")
            false
        }
    }

    /**
     * Gets library load error message, if any.
     */
    fun getLoadError(): String? {
        if (!MacWebViewNative.isMacOS) {
            return "Not running on macOS. Native WebView is only available on macOS."
        }

        return try {
            MacWebViewNative.INSTANCE.createWebView()
            null
        } catch (e: UnsatisfiedLinkError) {
            "Native library not found. Make sure libNativeWebView.dylib is in your library path.\n" +
                    "Error: ${e.message}"
        } catch (e: Throwable) {
            "Unknown error loading native library: ${e.message}"
        }
    }

    /**
     * Finds a ComposeWindow from the AWT window hierarchy.
     * This is useful when you don't have direct access to the window reference.
     */
    fun findComposeWindow(): ComposeWindow? {
        // Try focused window
        KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow?.let {
            if (it is ComposeWindow) return it
        }

        // Try active window
        KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow?.let {
            if (it is ComposeWindow) return it
        }

        // Search all windows
        return Window.getWindows()
            .filterIsInstance<ComposeWindow>()
            .firstOrNull { it.isVisible }
    }
}

/**
 * Configuration for WebView appearance and behavior.
 */
data class WebViewSettings(
    val javaScriptEnabled: Boolean = true,
    val allowsFileAccess: Boolean = true,
    val userAgent: String? = null
)

/**
 * Settings builder DSL for creating WebView configurations.
 */
class WebViewSettingsBuilder {
    var javaScriptEnabled: Boolean = true
    var allowsFileAccess: Boolean = true
    var userAgent: String? = null

    fun build(): WebViewSettings = WebViewSettings(
        javaScriptEnabled = javaScriptEnabled,
        allowsFileAccess = allowsFileAccess,
        userAgent = userAgent
    )
}

/**
 * DSL function to create WebViewSettings.
 */
inline fun webViewSettings(block: WebViewSettingsBuilder.() -> Unit): WebViewSettings {
    return WebViewSettingsBuilder().apply(block).build()
}