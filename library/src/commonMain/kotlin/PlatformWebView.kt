package com.saralapps.composemultiplatformwebview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import com.saralapps.composemultiplatformwebview.data.WebViewAvailability

@Composable
expect fun rememberPlatformWebViewState(
    url: String? = null,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    onNavigating: ((url: String) -> Boolean)? = null
): PlatformWebViewState


@Composable
expect fun PlatformWebView(
    state: PlatformWebViewState,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((String) -> Unit)? = null,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)? = null
)

@Composable
expect fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((String) -> Unit)? = null,
    onNavigating: ((String) -> Boolean)? = null,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)? = null
)
