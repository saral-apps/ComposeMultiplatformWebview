package com.saralapps.composemultiplatformwebview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.saralapps.composemultiplatformwebview.data.PlatformUtils
import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import com.saralapps.composemultiplatformwebview.data.WebViewAvailability
import com.saralapps.composemultiplatformwebview.data.checkWebViewAvailability
import com.saralapps.composemultiplatformwebview.native.mac.NativeWebView
import com.saralapps.composemultiplatformwebview.native.mac.rememberNativeWebViewState
import com.saralapps.composemultiplatformwebview.native.windows.WindowsWebView
import com.saralapps.composemultiplatformwebview.native.windows.rememberWindowsWebViewState

/**
 * Creates and remembers a cross-platform WebView state.
 */
@Composable
actual fun rememberPlatformWebViewState(
    url: String?,
    javaScriptEnabled: Boolean,
    allowsFileAccess: Boolean,
    onNavigating: ((url: String) -> Boolean)?
): PlatformWebViewState {
    val macState = if (PlatformUtils.isMacOS) {
        rememberNativeWebViewState(
            url = url,
            javaScriptEnabled = javaScriptEnabled,
            allowsFileAccess = allowsFileAccess,
            onNavigating = onNavigating
        )
    } else null

    val windowsState = if (PlatformUtils.isWindows) {
        rememberWindowsWebViewState(
            url = url,
            javaScriptEnabled = javaScriptEnabled,
            allowsFileAccess = allowsFileAccess,
            onNavigating = onNavigating
        )
    } else null

    return remember(macState, windowsState) {
        PlatformWebViewState(macState, windowsState)
    }

}

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
    when {
        PlatformUtils.isMacOS && state.macState != null -> {
            NativeWebView(
                state = state.macState,
                modifier = modifier,
                placeholderColor = placeholderColor,
                onUrlChanged = onUrlChanged,
                onCreated = onCreated,
                onDisposed = onDisposed
            )
        }
        PlatformUtils.isWindows && state.windowsState != null -> {
            val availability = remember { checkWebViewAvailability() }

            if (availability.isAvailable) {
                WindowsWebView(
                    state = state.windowsState,
                    modifier = modifier,
                    placeholderColor = placeholderColor,
                    onUrlChanged = onUrlChanged,
                    onCreated = onCreated,
                    onDisposed = onDisposed
                )
            } else {
                if (onUnavailable != null) {
                    onUnavailable(availability)
                } else {
                    DefaultUnavailableContent(availability)
                }
            }
        }
        else -> {
            val availability = remember { checkWebViewAvailability() }
            if (onUnavailable != null) {
                onUnavailable(availability)
            } else {
                DefaultUnavailableContent(availability)
            }
        }
    }
}


/**
 * Default content shown when WebView is not available.
 */
@Composable
private fun DefaultUnavailableContent(availability: WebViewAvailability) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buildString {
                appendLine("WebView not available")
                appendLine()
                appendLine("Platform: ${availability.platform}")
                availability.errorMessage?.let {
                    appendLine()
                    appendLine(it)
                }
                availability.downloadUrl?.let {
                    appendLine()
                    appendLine("Download: $it")
                }
            },
            textAlign = TextAlign.Center,
            color = Color.DarkGray
        )
    }
}

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