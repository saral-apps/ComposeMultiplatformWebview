package com.saralapps.composemultiplatformwebview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import com.saralapps.composemultiplatformwebview.data.WebViewAvailability

@Composable
actual fun rememberPlatformWebViewState(
    url: String?,
    javaScriptEnabled: Boolean,
    allowsFileAccess: Boolean,
    onNavigating: ((url: String) -> Boolean)?
): PlatformWebViewState {
    TODO("Not yet implemented")
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
}