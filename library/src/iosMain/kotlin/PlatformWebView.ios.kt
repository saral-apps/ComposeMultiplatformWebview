package com.saralapps.composemultiplatformwebview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import com.saralapps.composemultiplatformwebview.data.WebViewAvailability
import com.saralapps.composemultiplatformwebview.native.ios.PlatformWKNavigationDelegate
import com.saralapps.composemultiplatformwebview.native.ios.updateStateFromWebView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.delay
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURL
import platform.Foundation.setValue
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled

/**
 * Creates and remembers a PlatformWebViewState for iOS.
 */
@Composable
actual fun rememberPlatformWebViewState(
    url: String?,
    javaScriptEnabled: Boolean,
    allowsFileAccess: Boolean,
    onNavigating: ((url: String) -> Boolean)?
): PlatformWebViewState {
    return remember(url, javaScriptEnabled, allowsFileAccess) {
        PlatformWebViewState(
            initialUrl = url,
            javaScriptEnabled = javaScriptEnabled,
            allowsFileAccess = allowsFileAccess,
            navigationInterceptor = onNavigating
        )
    }
}

/**
 * iOS WebView composable using WKWebView.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun PlatformWebView(
    state: PlatformWebViewState,
    modifier: Modifier,
    placeholderColor: Color,
    onUrlChanged: ((String) -> Unit)?,
    onCreated: (() -> Unit)?,
    onDisposed: (() -> Unit)?,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)?
) {
    // Create navigation delegate
    val navigationDelegate = remember(state) {
        PlatformWKNavigationDelegate(state)
    }

    // Periodically update state from WebView
    LaunchedEffect(state) {
        while (true) {
            state.webView?.let { webView ->
                updateStateFromWebView(webView, state, onUrlChanged)
            }
            delay(250) // Poll every 250ms
        }
    }

    UIKitView(
        factory = {
            // Create WKWebView configuration
            val configuration = WKWebViewConfiguration().apply {
                // Allow inline media playback
                allowsInlineMediaPlayback = true

                // No user gesture required for media
                mediaTypesRequiringUserActionForPlayback = WKAudiovisualMediaTypeNone

                // JavaScript settings
                defaultWebpagePreferences.allowsContentJavaScript = state.javaScriptEnabled

                preferences.apply {
                    javaScriptEnabled = state.javaScriptEnabled
                    setValue(state.allowsFileAccess, forKey = "allowFileAccessFromFileURLs")
                }

                setValue(state.allowsFileAccess, forKey = "allowUniversalAccessFromFileURLs")
            }

            // Create WKWebView
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = configuration
            ).apply {
                // Store reference in state
                state.webView = this

                // Enable back/forward gestures
                allowsBackForwardNavigationGestures = true

                // Set navigation delegate
                this.navigationDelegate = navigationDelegate

                // Configure scroll view
                scrollView.apply {
                    bounces = true
                    scrollEnabled = true
                    showsHorizontalScrollIndicator = true
                    showsVerticalScrollIndicator = true
                }

                // Set background color
                setOpaque(false)
                val uiColor = placeholderColor.toUIColor()
                setBackgroundColor(uiColor)
                scrollView.setBackgroundColor(uiColor)

                // Load initial URL if provided
                state.pendingUrl?.let { urlString ->
                    NSURL.URLWithString(urlString)?.let { nsUrl ->
                        val request = NSURLRequest.requestWithURL(nsUrl)
                        loadRequest(request)
                    }
                }

                // Notify creation
                onCreated?.invoke()
            }
        },
        modifier = modifier,
        onRelease = { webView ->
            // Clear navigation delegate
            webView.navigationDelegate = null

            // Clear state reference
            state.webView = null

            // Notify disposal
            onDisposed?.invoke()
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true
        )
    )
}

/**
 * Convenience composable that creates its own state.
 */
@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
    javaScriptEnabled: Boolean,
    allowsFileAccess: Boolean,
    placeholderColor: Color,
    onUrlChanged: ((String) -> Unit)?,
    onNavigating: ((String) -> Boolean)?,
    onCreated: (() -> Unit)?,
    onDisposed: (() -> Unit)?,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)?
) {
    val state = rememberPlatformWebViewState(
        url = url,
        javaScriptEnabled = javaScriptEnabled,
        allowsFileAccess = allowsFileAccess,
        onNavigating = onNavigating
    )

    PlatformWebView(
        state = state,
        modifier = modifier,
        placeholderColor = placeholderColor,
        onUrlChanged = onUrlChanged,
        onCreated = onCreated,
        onDisposed = onDisposed,
        onUnavailable = onUnavailable
    )
}

/**
 * Converts Compose Color to UIColor.
 */
@OptIn(ExperimentalForeignApi::class)
private fun Color.toUIColor(): platform.UIKit.UIColor {
    return platform.UIKit.UIColor(
        red = this.red.toDouble(),
        green = this.green.toDouble(),
        blue = this.blue.toDouble(),
        alpha = this.alpha.toDouble()
    )
}