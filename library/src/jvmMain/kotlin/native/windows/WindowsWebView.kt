package com.saralapps.composemultiplatformwebview.native.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.sun.jna.Native
import com.sun.jna.Pointer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Canvas
import java.awt.KeyboardFocusManager
import java.awt.Window
import javax.swing.SwingUtilities

/**
 * State holder for Windows WebView2.
 */
class WindowsWebViewState(
    initialUrl: String? = null,
    val javaScriptEnabled: Boolean = true,
    val allowsFileAccess: Boolean = true
) {
    internal var webViewId: Long by mutableStateOf(0L)
        private set

    val isCreated: Boolean get() = webViewId != 0L

    var isReady: Boolean by mutableStateOf(false)
        private set

    var isAttached: Boolean by mutableStateOf(false)
        private set

    var currentUrl: String? by mutableStateOf(initialUrl)
        private set

    var navigatingUrl: String? by mutableStateOf(null)
        internal set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var canGoBack: Boolean by mutableStateOf(false)
        private set

    var canGoForward: Boolean by mutableStateOf(false)
        private set

    var pageTitle: String? by mutableStateOf(null)
        private set

    internal val pendingUrl: String? = initialUrl
    private var navigationCallback: NavigationCallback? = null
    private var pendingInterceptor: ((url: String) -> Boolean)? = null

    // Store the canvas for HWND
    private var nativeCanvas: Canvas? = null

    /**
     * Creates the WebView but doesn't attach it yet.
     */
    internal fun create(): Boolean {
        if (webViewId != 0L) return true

        try {
            val envResult = WindowsWebViewNative.INSTANCE.initializeEnvironment()
            if (!envResult) return false

            isReady = true
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Attaches the WebView to a window using a Canvas for the HWND.
     */
    internal fun attachToWindow(canvas: Canvas): Boolean {
        if (!isReady) return false
        if (isAttached) return true

        try {
            nativeCanvas = canvas

            val hwndPointer = Native.getComponentPointer(canvas)
            if (hwndPointer == null || Pointer.nativeValue(hwndPointer) == 0L) {
                return false
            }

            webViewId = WindowsWebViewNative.INSTANCE.createWebViewWithSettings(
                hwndPointer, javaScriptEnabled, allowsFileAccess
            )

            if (webViewId == 0L) return false

            isAttached = true

            pendingInterceptor?.let {
                setNavigationInterceptor(it)
                pendingInterceptor = null
            }

            return true

        } catch (e: Throwable) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Updates the WebView bounds.
     */
    internal fun updateBounds(x: Int, y: Int, width: Int, height: Int) {
        if (webViewId == 0L || !isAttached || width <= 0 || height <= 0) return
        try {
            WindowsWebViewNative.INSTANCE.setWebViewBounds(webViewId, x, y, width, height)
        } catch (e: Throwable) {
            // Silently ignore
        }
    }

    internal fun destroy() {
        if (webViewId == 0L) return
        try {
            WindowsWebViewNative.INSTANCE.destroyWebView(webViewId)
        } catch (e: Throwable) {
            // Silently ignore
        } finally {
            webViewId = 0L
            isAttached = false
            isReady = false
            nativeCanvas = null
        }
    }

    internal fun refreshState() {
        if (webViewId == 0L) return
        try {
            canGoBack = WindowsWebViewNative.INSTANCE.webViewCanGoBack(webViewId)
            canGoForward = WindowsWebViewNative.INSTANCE.webViewCanGoForward(webViewId)
            val newUrl = WindowsWebViewHelper.getCurrentURL(webViewId)
            if (newUrl != null && newUrl != currentUrl) {
                navigatingUrl = newUrl
            }
            currentUrl = newUrl
            pageTitle = WindowsWebViewHelper.getTitle(webViewId)
            isLoading = false
        } catch (e: Throwable) {}
    }

    fun loadUrl(url: String) {
        if (webViewId == 0L) return
        navigatingUrl = url
        isLoading = true
        WindowsWebViewNative.INSTANCE.loadURL(webViewId, url)
        currentUrl = url
    }

    fun loadHtml(html: String) {
        if (webViewId == 0L) return
        isLoading = true
        WindowsWebViewNative.INSTANCE.loadHTMLString(webViewId, html)
    }

    fun setNavigationInterceptor(interceptor: ((url: String) -> Boolean)?) {
        if (webViewId == 0L) {
            pendingInterceptor = interceptor
            return
        }
        if (interceptor == null) {
            navigationCallback = null
            WindowsWebViewNative.INSTANCE.setNavigationCallback(webViewId, null)
        } else {
            val callback = object : NavigationCallback {
                override fun invoke(webViewId: Long, url: String): Boolean {
                    return interceptor(url)
                }
            }
            navigationCallback = callback
            WindowsWebViewNative.INSTANCE.setNavigationCallback(webViewId, callback)
        }
    }

    fun goBack() { if (webViewId != 0L && canGoBack) WindowsWebViewNative.INSTANCE.webViewGoBack(webViewId) }
    fun goForward() { if (webViewId != 0L && canGoForward) WindowsWebViewNative.INSTANCE.webViewGoForward(webViewId) }
    fun reload() { if (webViewId != 0L) WindowsWebViewNative.INSTANCE.webViewReload(webViewId) }
    fun stopLoading() { if (webViewId != 0L) { WindowsWebViewNative.INSTANCE.webViewStopLoading(webViewId); isLoading = false } }
    fun evaluateJavaScript(code: String) { if (webViewId != 0L) WindowsWebViewNative.INSTANCE.evaluateJavaScript(webViewId, code) }
    fun setUserAgent(ua: String) { if (webViewId != 0L) WindowsWebViewNative.INSTANCE.setCustomUserAgent(webViewId, ua) }
    fun setVisible(visible: Boolean) { if (webViewId != 0L) WindowsWebViewNative.INSTANCE.setWebViewVisible(webViewId, visible) }

    /**
     * Forces the WebView to refresh its display.
     * This is the key fix for the white screen issue on Windows.
     */
    fun forceDisplay() {
        if (webViewId == 0L || !isAttached) return

        try {
            nativeCanvas?.let { c ->
                SwingUtilities.invokeLater {
                    // Get current size
                    val w = c.width
                    val h = c.height

                    if (w > 0 && h > 0) {
                        // Force bounds update multiple times
                        WindowsWebViewNative.INSTANCE.setWebViewBounds(webViewId, 0, 0, w, h)

                        // Toggle visibility to force refresh
                        WindowsWebViewNative.INSTANCE.setWebViewVisible(webViewId, false)
                        Thread.sleep(10) // Small delay
                        WindowsWebViewNative.INSTANCE.setWebViewVisible(webViewId, true)

                        // Update bounds again after visibility toggle
                        WindowsWebViewNative.INSTANCE.setWebViewBounds(webViewId, 0, 0, w, h)

                        // Invalidate canvas to trigger repaint
                        c.repaint()
                        c.parent?.repaint()
                        c.parent?.revalidate()
                    }
                }
            }
        } catch (e: Throwable) {
            // Silently ignore
        }
    }
}

@Composable
fun rememberWindowsWebViewState(
    url: String? = null,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    onNavigating: ((url: String) -> Boolean)? = null
): WindowsWebViewState {
    val state = remember(javaScriptEnabled, allowsFileAccess) {
        WindowsWebViewState(initialUrl = url, javaScriptEnabled = javaScriptEnabled, allowsFileAccess = allowsFileAccess)
    }

    LaunchedEffect(onNavigating) {
        onNavigating?.let { state.setNavigationInterceptor(it) }
    }

    return state
}

@Composable
fun WindowsWebView(
    state: WindowsWebViewState,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((String) -> Unit)? = null,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var composeWindow by remember { mutableStateOf<ComposeWindow?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    var lastReportedUrl by remember { mutableStateOf<String?>(null) }

    // The canvas that will host the WebView
    var canvas by remember { mutableStateOf<Canvas?>(null) }

    // Find ComposeWindow
    LaunchedEffect(Unit) {
        composeWindow = findComposeWindow()
    }

    // Create canvas and attach WebView when window is available
    LaunchedEffect(composeWindow) {
        val window = composeWindow ?: return@LaunchedEffect

        if (!isInitialized && state.create()) {
            // Wait for environment to be ready
            var attempts = 0
            while (!WindowsWebViewNative.INSTANCE.isEnvironmentReady() && attempts < 100) {
                delay(50)
                attempts++
            }

            if (!WindowsWebViewNative.INSTANCE.isEnvironmentReady()) {
                println("[WindowsWebView] Environment failed to initialize")
                return@LaunchedEffect
            }

            // Create a Canvas on the Swing thread
            val canvasCreated = kotlinx.coroutines.suspendCancellableCoroutine<Canvas?> { continuation ->
                SwingUtilities.invokeLater {
                    try {
                        val newCanvas = Canvas()
                        newCanvas.background = java.awt.Color.WHITE

                        // Add canvas to the window's layered pane so it overlays content
                        val layeredPane = window.layeredPane
                        layeredPane.add(newCanvas, javax.swing.JLayeredPane.POPUP_LAYER)

                        continuation.resume(newCanvas, null)
                    } catch (e: Exception) {
                        continuation.resume(null, null)
                    }
                }
            }

            canvas = canvasCreated

            // Wait for canvas to be displayable and have a size
            var canvasAttempts = 0
            while (canvasAttempts < 50) {
                delay(50)
                val c = canvas
                if (c != null && c.isDisplayable && c.width > 0 && c.height > 0) {
                    break
                }
                canvasAttempts++
            }

            // Attach WebView to canvas
            canvas?.let { c ->
                if (state.attachToWindow(c)) {
                    isInitialized = true

                    // Wait for WebView to be ready
                    var webViewAttempts = 0
                    while (!WindowsWebViewNative.INSTANCE.isWebViewReady(state.webViewId) && webViewAttempts < 100) {
                        delay(50)
                        webViewAttempts++
                    }

                    // Set initial bounds
                    if (c.width > 0 && c.height > 0) {
                        state.updateBounds(0, 0, c.width, c.height)
                    }

                    // Force display immediately after creation
                    delay(100)
                    state.forceDisplay()

                    // Load URL if provided
                    state.pendingUrl?.let { url ->
                        delay(200)
                        state.loadUrl(url)
                    }

                    onCreated?.invoke()
                }
            }
        }
    }

    // Aggressive initial display forcing - this is the KEY FIX
    LaunchedEffect(isInitialized) {
        if (!isInitialized) return@LaunchedEffect

        // Force display multiple times in the first few seconds
        // This ensures WebView2 renders properly without needing window resize
        val delays = listOf(100L, 200L, 300L, 500L, 700L, 1000L, 1500L, 2000L)

        for (delayMs in delays) {
            delay(delayMs)
            canvas?.let { c ->
                if (c.width > 0 && c.height > 0) {
                    // Update bounds first
                    state.updateBounds(0, 0, c.width, c.height)
                    // Then force display
                    state.forceDisplay()
                }
            }
        }
    }

    // Watch for URL changes
    LaunchedEffect(state.currentUrl, state.navigatingUrl) {
        val url = state.currentUrl ?: state.navigatingUrl
        if (url != null && url != lastReportedUrl) {
            lastReportedUrl = url
            onUrlChanged?.invoke(url)
        }
    }

    // Cleanup
    DisposableEffect(state) {
        onDispose {
            canvas?.let { c ->
                SwingUtilities.invokeLater {
                    c.parent?.remove(c)
                }
            }
            state.destroy()
            onDisposed?.invoke()
        }
    }

    // Periodic state refresh
    LaunchedEffect(state, isInitialized) {
        if (!isInitialized) return@LaunchedEffect
        while (true) {
            delay(200)
            withContext(Dispatchers.IO) {
                state.refreshState()
            }
        }
    }

    // The placeholder box - positions the WebView using onGloballyPositioned
    Box(
        modifier = modifier
            .background(placeholderColor)
            .onGloballyPositioned { coordinates ->
                if (!state.isAttached) return@onGloballyPositioned

                val bounds = coordinates.boundsInWindow()

                val x = bounds.left.toInt()
                val y = bounds.top.toInt()
                val width = bounds.width.toInt()
                val height = bounds.height.toInt()

                if (width <= 0 || height <= 0) return@onGloballyPositioned

                // Update canvas position and size
                canvas?.let { c ->
                    val needsUpdate = c.x != x || c.y != y || c.width != width || c.height != height

                    if (needsUpdate) {
                        SwingUtilities.invokeLater {
                            c.setBounds(x, y, width, height)
                            // Also update WebView bounds
                            state.updateBounds(0, 0, width, height)
                            // Force refresh after bounds change
                            scope.launch {
                                delay(50)
                                state.forceDisplay()
                            }
                        }
                    }
                }
            }
    )
}

@Composable
fun WindowsWebView(
    url: String,
    modifier: Modifier = Modifier,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((String) -> Unit)? = null
) {
    val state = rememberWindowsWebViewState(url = url, javaScriptEnabled = javaScriptEnabled, allowsFileAccess = allowsFileAccess)
    WindowsWebView(state = state, modifier = modifier, placeholderColor = placeholderColor, onUrlChanged = onUrlChanged)
}

private fun findComposeWindow(): ComposeWindow? {
    val focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow
    if (focusedWindow is ComposeWindow) return focusedWindow

    val activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
    if (activeWindow is ComposeWindow) return activeWindow

    return Window.getWindows().filterIsInstance<ComposeWindow>().firstOrNull()
}