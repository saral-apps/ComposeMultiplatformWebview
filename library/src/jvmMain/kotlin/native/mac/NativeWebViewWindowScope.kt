package com.saralapps.composemultiplatformwebview.native.mac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.FrameWindowScope
import com.sun.jna.Pointer
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
 * A NativeWebView composable for use within a FrameWindowScope.
 *
 * This version receives the window directly from the WindowScope,
 * providing more reliable window access than the automatic detection.
 *
 * Usage:
 * ```
 * Window(onCloseRequest = ::exitApplication) {
 *     NativeWebViewInWindow(
 *         url = "https://www.google.com",
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * ```
 */
@Composable
fun FrameWindowScope.NativeWebViewInWindow(
    state: NativeWebViewState,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.White,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null
) {
    val density = LocalDensity.current

    // Get the window from the scope - this is reliable
    val composeWindow: ComposeWindow = window

    // Track if we've initialized
    var isInitialized by remember { mutableStateOf(false) }

    // Create and attach WebView
    LaunchedEffect(composeWindow) {
        if (!isInitialized && state.create()) {
            val windowPointer = Pointer(composeWindow.windowHandle)
            if (state.attachToWindow(windowPointer)) {
                isInitialized = true
                onCreated?.invoke()
            }
        }
    }

    // Cleanup on disposal
    DisposableEffect(state) {
        onDispose {
            state.destroy()
            onDisposed?.invoke()
        }
    }

    // Periodically refresh state
    LaunchedEffect(state, isInitialized) {
        if (!isInitialized) return@LaunchedEffect

        while (true) {
            state.refreshState()
            kotlinx.coroutines.delay(500)
        }
    }

    // Listen for window resize
    DisposableEffect(composeWindow) {
        val listener = object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                // Frame will be updated via onGloballyPositioned
            }
        }

        composeWindow.addComponentListener(listener)
        onDispose {
            composeWindow.removeComponentListener(listener)
        }
    }

    // The placeholder box that defines the WebView's bounds
    Box(
        modifier = modifier
            .background(placeholderColor)
            .onGloballyPositioned { coordinates ->
                if (!state.isAttached) return@onGloballyPositioned

                // Get bounds in window coordinates
                val bounds = coordinates.boundsInWindow()

                // Convert from density-independent pixels to actual pixels
                val x = bounds.left / density.density
                val y = bounds.top / density.density
                val width = bounds.width / density.density
                val height = bounds.height / density.density

                // Get window content height for coordinate conversion
                val windowPointer = Pointer(composeWindow.windowHandle)
                val parentHeight = MacWebViewNative.INSTANCE.getWindowContentHeight(windowPointer).toFloat()

                // Update WebView frame
                state.updateFrame(x, y, width, height, parentHeight)
            }
    )
}

/**
 * Convenience overload that creates its own state.
 */
@Composable
fun FrameWindowScope.NativeWebViewInWindow(
    url: String,
    modifier: Modifier = Modifier,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    placeholderColor: Color = Color.White,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null
) {
    val state = rememberNativeWebViewState(
        url = url,
        javaScriptEnabled = javaScriptEnabled,
        allowsFileAccess = allowsFileAccess
    )

    NativeWebViewInWindow(
        state = state,
        modifier = modifier,
        placeholderColor = placeholderColor,
        onCreated = onCreated,
        onDisposed = onDisposed
    )
}

// =============================================================================
// Alternative: Using a provided window reference
// =============================================================================

/**
 * A NativeWebView that accepts the window as a parameter.
 *
 * Use this when you have explicit access to the ComposeWindow reference.
 *
 * Usage:
 * ```
 * var window: ComposeWindow? = null
 *
 * Window(onCloseRequest = ::exitApplication) {
 *     LaunchedEffect(Unit) {
 *         window = this@Window.window
 *     }
 *
 *     window?.let { win ->
 *         NativeWebViewWithWindow(
 *             window = win,
 *             url = "https://www.google.com",
 *             modifier = Modifier.fillMaxSize()
 *         )
 *     }
 * }
 * ```
 */
@Composable
fun NativeWebViewWithWindow(
    window: ComposeWindow,
    state: NativeWebViewState,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.White,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null
) {
    val density = LocalDensity.current

    // Track if we've initialized
    var isInitialized by remember { mutableStateOf(false) }

    // Create and attach WebView
    LaunchedEffect(window) {
        if (!isInitialized && state.create()) {
            val windowPointer = Pointer(window.windowHandle)
            if (state.attachToWindow(windowPointer)) {
                isInitialized = true
                onCreated?.invoke()
            }
        }
    }

    // Cleanup on disposal
    DisposableEffect(state) {
        onDispose {
            state.destroy()
            onDisposed?.invoke()
        }
    }

    // Periodically refresh state
    LaunchedEffect(state, isInitialized) {
        if (!isInitialized) return@LaunchedEffect

        while (true) {
            state.refreshState()
            kotlinx.coroutines.delay(500)
        }
    }

    // Listen for window resize
    DisposableEffect(window) {
        val listener = object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                // Frame will be updated via onGloballyPositioned
            }
        }

        window.addComponentListener(listener)
        onDispose {
            window.removeComponentListener(listener)
        }
    }

    // The placeholder box that defines the WebView's bounds
    Box(
        modifier = modifier
            .background(placeholderColor)
            .onGloballyPositioned { coordinates ->
                if (!state.isAttached) return@onGloballyPositioned

                // Get bounds in window coordinates
                val bounds = coordinates.boundsInWindow()

                // Convert from density-independent pixels to actual pixels
                val x = bounds.left / density.density
                val y = bounds.top / density.density
                val width = bounds.width / density.density
                val height = bounds.height / density.density

                // Get window content height for coordinate conversion
                val windowPointer = Pointer(window.windowHandle)
                val parentHeight = MacWebViewNative.INSTANCE.getWindowContentHeight(windowPointer).toFloat()

                // Update WebView frame
                state.updateFrame(x, y, width, height, parentHeight)
            }
    )
}

/**
 * Convenience overload that creates its own state.
 */
@Composable
fun NativeWebViewWithWindow(
    window: ComposeWindow,
    url: String,
    modifier: Modifier = Modifier,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    placeholderColor: Color = Color.White,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null
) {
    val state = rememberNativeWebViewState(
        url = url,
        javaScriptEnabled = javaScriptEnabled,
        allowsFileAccess = allowsFileAccess
    )

    NativeWebViewWithWindow(
        window = window,
        state = state,
        modifier = modifier,
        placeholderColor = placeholderColor,
        onCreated = onCreated,
        onDisposed = onDisposed
    )
}