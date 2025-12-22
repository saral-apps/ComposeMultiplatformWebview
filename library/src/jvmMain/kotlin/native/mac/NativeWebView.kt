package com.saralapps.composemultiplatformwebview.native.mac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import com.sun.jna.Pointer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.awt.KeyboardFocusManager
import java.awt.Window

/**
 * State holder for the native WebView.
 * This class manages the WebView lifecycle and provides reactive state.
 *
 * @param initialUrl The initial URL to load (optional).
 * @param javaScriptEnabled Whether JavaScript should be enabled (default: true).
 * @param allowsFileAccess Whether file:// URLs can access other files (default: true).
 */
class NativeWebViewState(
    initialUrl: String? = null,
    val javaScriptEnabled: Boolean = true,
    val allowsFileAccess: Boolean = true
) {
    /**
     * The internal ID of the native WebView.
     * 0 means the WebView hasn't been created yet.
     */
    internal var webViewId: Long by mutableStateOf(0L)
        private set

    /**
     * Whether the WebView has been created (but not necessarily attached yet).
     */
    val isCreated: Boolean
        get() = webViewId != 0L

    /**
     * Whether the WebView has been attached to a window.
     */
    var isAttached: Boolean by mutableStateOf(false)
        private set

    /**
     * The current URL being displayed (updated when navigation completes).
     */
    var currentUrl: String? by mutableStateOf(initialUrl)
        private set

    /**
     * The URL that is currently being navigated to (updated immediately when navigation starts).
     * Watch this to detect navigation changes.
     */
    var navigatingUrl: String? by mutableStateOf(null)
        internal set

    /**
     * Whether the WebView is currently loading a page.
     */
    var isLoading: Boolean by mutableStateOf(false)
        private set

    /**
     * The loading progress (0.0 to 1.0).
     */
    var loadingProgress: Float by mutableStateOf(0f)
        private set

    /**
     * Whether back navigation is available.
     */
    var canGoBack: Boolean by mutableStateOf(false)
        private set

    /**
     * Whether forward navigation is available.
     */
    var canGoForward: Boolean by mutableStateOf(false)
        private set

    /**
     * The title of the current page.
     */
    var pageTitle: String? by mutableStateOf(null)
        private set

    /**
     * The URL that was requested to load initially.
     */
    internal val pendingUrl: String? = initialUrl

    /**
     * Navigation callback - holds reference to prevent garbage collection
     */
    private var navigationCallback: NavigationCallback? = null

    /**
     * Pending interceptor to be set before first load
     */
    private var pendingInterceptor: ((url: String) -> Boolean)? = null

    /**
     * Creates the native WebView. Called internally by the composable.
     */
    internal fun create(): Boolean {
        if (webViewId != 0L) return true // Already created

        if (!MacWebViewNative.isMacOS) {
            println("[NativeWebView] Error: Not running on macOS")
            return false
        }

        return try {
            webViewId = MacWebViewNative.INSTANCE.createWebViewWithSettings(
                javaScriptEnabled,
                allowsFileAccess
            )

            // Apply pending interceptor if any
            pendingInterceptor?.let { interceptor ->
                setNavigationInterceptor(interceptor)
                pendingInterceptor = null
            }

            webViewId != 0L
        } catch (e: Throwable) {
            println("[NativeWebView] Error creating WebView: ${e.message}")
            e.printStackTrace()
            false
        }
    }

        /**
     * Attaches the WebView to a window. Called internally by the composable.
     */
    internal fun attachToWindow(windowPointer: Pointer): Boolean {
        if (webViewId == 0L) return false
        if (isAttached) return true

        return try {
            MacWebViewNative.INSTANCE.attachWebViewToWindow(webViewId, windowPointer)
            isAttached = true

            MacWebViewNative.INSTANCE.forceWebViewDisplay(webViewId)

            pendingUrl?.let { loadUrl(it) }

            true
        } catch (e: Throwable) {
            println("[NativeWebView] Error attaching to window: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    /**
     * Updates the frame of the WebView. Called internally by the composable.
     * Uses flipped coordinates (top-left origin like Compose).
     */
    internal fun updateFrame(x: Float, y: Float, width: Float, height: Float, parentHeight: Float) {
        if (webViewId == 0L || !isAttached) return

        try {
            MacWebViewNative.INSTANCE.setWebViewFrameFlipped(
                webViewId,
                x.toDouble(),
                y.toDouble(),
                width.toDouble(),
                height.toDouble(),
                parentHeight.toDouble()
            )
        } catch (e: Throwable) {
            println("[NativeWebView] Error updating frame: ${e.message}")
        }
    }

    /**
     * Destroys the WebView. Called internally when the composable is disposed.
     */
    internal fun destroy() {
        if (webViewId == 0L) return

        try {
            MacWebViewNative.INSTANCE.destroyWebView(webViewId)
        } catch (e: Throwable) {
            println("[NativeWebView] Error destroying WebView: ${e.message}")
        } finally {
            webViewId = 0L
            isAttached = false
        }
    }

    /**
     * Refreshes the state from the native WebView.
     */
    internal fun refreshState() {
        if (webViewId == 0L) return

        try {
            isLoading = MacWebViewNative.INSTANCE.webViewIsLoading(webViewId)
            loadingProgress = MacWebViewNative.INSTANCE.webViewGetProgress(webViewId).toFloat()
            canGoBack = MacWebViewNative.INSTANCE.webViewCanGoBack(webViewId)
            canGoForward = MacWebViewNative.INSTANCE.webViewCanGoForward(webViewId)

            val newUrl = MacWebViewHelper.getCurrentURL(webViewId)
            if (newUrl != currentUrl && newUrl != null) {
                // URL changed - update navigatingUrl for observers
                navigatingUrl = newUrl
            }
            currentUrl = newUrl
            pageTitle = MacWebViewHelper.getTitle(webViewId)
        } catch (e: Throwable) {
            // Silently ignore refresh errors
        }
    }

    // ========== Public API ==========

    /**
     * Loads a URL in the WebView.
     */
    fun loadUrl(url: String) {
        if (webViewId == 0L) {
            println("[NativeWebView] Cannot load URL: WebView not created")
            return
        }

        try {
            navigatingUrl = url  // Set immediately
            MacWebViewNative.INSTANCE.loadURL(webViewId, url)
            currentUrl = url
            isLoading = true
        } catch (e: Throwable) {
            println("[NativeWebView] Error loading URL: ${e.message}")
        }
    }

    /**
     * Loads HTML content directly.
     */
    fun loadHtml(html: String, baseUrl: String? = null) {
        if (webViewId == 0L) return

        try {
            MacWebViewNative.INSTANCE.loadHTMLString(webViewId, html, baseUrl)
            isLoading = true
        } catch (e: Throwable) {
            println("[NativeWebView] Error loading HTML: ${e.message}")
        }
    }

    /**
     * Sets a navigation interceptor that is called before each URL load.
     * The interceptor receives the URL and should return true to allow or false to cancel.
     *
     * Can be called before the WebView is created - it will be applied when ready.
     *
     * Example:
     * ```
     * state.setNavigationInterceptor { url ->
     *     if (url.contains("student")) {
     *         true // Allow
     *     } else {
     *         // Handle blocked navigation
     *         goBack()
     *         false // Cancel
     *     }
     * }
     * ```
     *
     * @param interceptor The interceptor function, or null to remove.
     */
    fun setNavigationInterceptor(interceptor: ((url: String) -> Boolean)?) {
        // If WebView not created yet, queue the interceptor
        if (webViewId == 0L) {
            println("[NativeWebView-Kotlin] WebView not created yet, queuing interceptor")
            pendingInterceptor = interceptor
            return
        }

        try {
            if (interceptor == null) {
                println("[NativeWebView-Kotlin] Removing navigation interceptor for WebView $webViewId")
                navigationCallback = null
                MacWebViewNative.INSTANCE.setNavigationCallback(webViewId, null)
            } else {
                println("[NativeWebView-Kotlin] Setting navigation interceptor for WebView $webViewId")
                // Create a JNA callback that wraps the Kotlin lambda
                val callback = object : NavigationCallback {
                    override fun invoke(webViewId: Long, url: String): Boolean {
                        println("[NativeWebView-Kotlin] Interceptor called with URL: $url")
                        val result = interceptor(url)
                        println("[NativeWebView-Kotlin] Interceptor returning: $result")
                        return result
                    }
                }
                // Store reference to prevent garbage collection
                navigationCallback = callback
                MacWebViewNative.INSTANCE.setNavigationCallback(webViewId, callback)
                println("[NativeWebView-Kotlin] Navigation interceptor set successfully")
            }
        } catch (e: Throwable) {
            println("[NativeWebView-Kotlin] Error setting navigation interceptor: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Navigates back in history.
     */
    fun goBack() {
        if (webViewId == 0L || !canGoBack) return
        MacWebViewNative.INSTANCE.webViewGoBack(webViewId)
    }

    /**
     * Navigates forward in history.
     */
    fun goForward() {
        if (webViewId == 0L || !canGoForward) return
        MacWebViewNative.INSTANCE.webViewGoForward(webViewId)
    }

    /**
     * Reloads the current page.
     */
    fun reload() {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.webViewReload(webViewId)
    }

    /**
     * Stops loading the current page.
     */
    fun stopLoading() {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.webViewStopLoading(webViewId)
        isLoading = false
    }

    /**
     * Evaluates JavaScript in the WebView.
     */
    fun evaluateJavaScript(code: String) {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.evaluateJavaScript(webViewId, code)
    }

    /**
     * Sets a custom user agent.
     */
    fun setUserAgent(userAgent: String) {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.setCustomUserAgent(webViewId, userAgent)
    }

    /**
     * Sets the visibility of the WebView.
     */
    fun setVisible(visible: Boolean) {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.setWebViewVisible(webViewId, visible)
    }

    /**
     * Forces the WebView to refresh its display.
     * Call this if the WebView appears blank.
     */
    fun forceDisplay() {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.forceWebViewDisplay(webViewId)
    }

    /**
     * Sets the opacity of the WebView.
     */
    fun setAlpha(alpha: Float) {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.setWebViewAlpha(webViewId, alpha.toDouble())
    }

    /**
     * Brings the WebView to the front.
     */
    fun bringToFront() {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.bringWebViewToFront(webViewId)
    }

    /**
     * Sends the WebView to the back.
     */
    fun sendToBack() {
        if (webViewId == 0L) return
        MacWebViewNative.INSTANCE.sendWebViewToBack(webViewId)
    }
}

/**
 * Creates and remembers a NativeWebViewState.
 *
 * @param url The initial URL to load (optional). If null, call state.loadUrl() manually.
 * @param javaScriptEnabled Whether JavaScript should be enabled.
 * @param allowsFileAccess Whether file:// URLs can access other files.
 * @param onNavigating Optional callback called before each navigation. Return true to allow, false to block.
 */
@Composable
internal fun rememberNativeWebViewState(
    url: String? = null,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    onNavigating: ((url: String) -> Boolean)? = null
): NativeWebViewState {
    val state = remember(javaScriptEnabled, allowsFileAccess) {
        NativeWebViewState(
            initialUrl = url,
            javaScriptEnabled = javaScriptEnabled,
            allowsFileAccess = allowsFileAccess
        )
    }

    LaunchedEffect(onNavigating) {
        if (onNavigating != null) {
            state.setNavigationInterceptor(onNavigating)
        }
    }

    return state
}

/**
 * A native macOS WebView composable that automatically sizes and positions itself.
 *
 * The WebView will fill the bounds of this composable and automatically resize
 * when the window or parent changes size.
 *
 * @param state The state object controlling this WebView (use [rememberNativeWebViewState]).
 * @param modifier Modifier to apply to the placeholder box.
 * @param placeholderColor Background color for the placeholder (shown briefly before WebView loads).
 * @param onUrlChanged Callback when the URL changes. Receives the new URL.
 * @param onCreated Callback when the WebView is created.
 * @param onDisposed Callback when the WebView is disposed.
 */
@Composable
internal fun NativeWebView(
    state: NativeWebViewState,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((url: String) -> Unit)? = null,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Track the current compose window
    var composeWindow by remember { mutableStateOf<ComposeWindow?>(null) }

    // Track if we've initialized
    var isInitialized by remember { mutableStateOf(false) }

    // Cache window height - updated periodically, not on every frame
    var cachedWindowHeight by remember { mutableStateOf(800f) }

    // Pending frame update job for debouncing
    var pendingFrameJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    // Last known bounds for debounced update
    var pendingBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }

    // Track last URL to detect changes
    var lastReportedUrl by remember { mutableStateOf<String?>(null) }

    // Find the ComposeWindow
    LaunchedEffect(Unit) {
        composeWindow = findComposeWindow()
    }

    // Create and attach WebView when window is available
    LaunchedEffect(composeWindow) {
        val window = composeWindow ?: return@LaunchedEffect

        if (!isInitialized && state.create()) {
            val windowPointer = Pointer(window.windowHandle)
            if (state.attachToWindow(windowPointer)) {
                isInitialized = true
                // Get initial window height on background thread
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    cachedWindowHeight = MacWebViewNative.INSTANCE.getWindowContentHeight(windowPointer).toFloat()
                }
                onCreated?.invoke()
                kotlinx.coroutines.delay(100)
                state.forceDisplay()
            }
        }
    }

    // Watch for URL changes and call callback
    LaunchedEffect(state.currentUrl, state.navigatingUrl) {
        val url = state.currentUrl ?: state.navigatingUrl
        if (url != null && url != lastReportedUrl) {
            lastReportedUrl = url
            onUrlChanged?.invoke(url)
        }
    }

    // Cleanup on disposal
    DisposableEffect(state) {
        onDispose {
            pendingFrameJob?.cancel()
            state.destroy()
            onDisposed?.invoke()
        }
    }

    // Periodically refresh state and window height (not on UI thread)
    LaunchedEffect(state, isInitialized) {
        if (!isInitialized) return@LaunchedEffect

        while (true) {
            kotlinx.coroutines.delay(200)  // Faster refresh for URL change detection

            // Refresh state
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                state.refreshState()
            }

            // Update cached window height less frequently
            composeWindow?.let { window ->
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val windowPointer = Pointer(window.windowHandle)
                    cachedWindowHeight = MacWebViewNative.INSTANCE.getWindowContentHeight(windowPointer).toFloat()
                }
            }
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
                pendingBounds = bounds

                // Cancel any pending update
                pendingFrameJob?.cancel()

                // Debounce: wait 32ms before actually updating (roughly 30fps max)
                pendingFrameJob = scope.launch {
                    kotlinx.coroutines.delay(32)

                    val currentBounds = pendingBounds ?: return@launch

                    // Convert from density-independent pixels to actual pixels
                    val x = currentBounds.left / density.density
                    val y = currentBounds.top / density.density
                    val width = currentBounds.width / density.density
                    val height = currentBounds.height / density.density

                    // Use cached window height - never call native here
                    val parentHeight = cachedWindowHeight

                    // Update WebView frame on IO dispatcher to not block UI
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        state.updateFrame(x, y, width, height, parentHeight)
                    }
                }
            }
    )
}

/**
 * Convenience composable that creates its own state.
 *
 * @param url The URL to load.
 * @param modifier Modifier to apply.
 * @param javaScriptEnabled Whether JavaScript is enabled.
 * @param allowsFileAccess Whether file access is allowed.
 * @param placeholderColor Background color for the placeholder.
 */
@Composable
internal fun NativeWebView(
    url: String,
    modifier: Modifier = Modifier,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    placeholderColor: Color = Color.White
) {
    val state = rememberNativeWebViewState(
        url = url,
        javaScriptEnabled = javaScriptEnabled,
        allowsFileAccess = allowsFileAccess
    )

    NativeWebView(
        state = state,
        modifier = modifier,
        placeholderColor = placeholderColor
    )
}

/**
 * Finds the ComposeWindow from the current AWT context.
 */
private fun findComposeWindow(): ComposeWindow? {
    // Try to get the focused window first
    val focusedWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().focusedWindow
    if (focusedWindow is ComposeWindow) {
        return focusedWindow
    }

    // Try active window
    val activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
    if (activeWindow is ComposeWindow) {
        return activeWindow
    }

    // Search all windows
    return Window.getWindows().filterIsInstance<ComposeWindow>().firstOrNull()
}