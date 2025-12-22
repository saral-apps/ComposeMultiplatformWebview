package com.saralapps.composemultiplatformwebview.native.mac
import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * Callback interface for navigation interception.
 * Called before each navigation with the URL.
 * Return true to allow navigation, false to cancel.
 */
interface NavigationCallback : Callback {
    /**
     * Called when a navigation is about to occur.
     * @param webViewId The ID of the WebView
     * @param url The URL being navigated to
     * @return true to allow navigation, false to cancel
     */
    fun invoke(webViewId: Long, url: String): Boolean
}

/**
 * JNA interface for the native macOS WebView library.
 * This interface maps to the C-callable functions exported from NativeWebView.swift.
 *
 * Usage:
 *   val webViewId = MacWebViewNative.INSTANCE.createWebView()
 *   MacWebViewNative.INSTANCE.attachWebViewToWindow(webViewId, windowPointer)
 *   MacWebViewNative.INSTANCE.loadURL(webViewId, "https://example.com")
 */
interface MacWebViewNative : Library {

    // ========== Lifecycle ==========

    /**
     * Creates a new WebView with default configuration.
     * @return A unique ID for the WebView, or 0 if creation failed.
     */
    fun createWebView(): Long

    /**
     * Creates a new WebView with custom settings.
     * @param javaScriptEnabled Whether JavaScript is enabled.
     * @param allowsFileAccess Whether file:// URLs can access other file:// URLs.
     * @return A unique ID for the WebView, or 0 if creation failed.
     */
    fun createWebViewWithSettings(javaScriptEnabled: Boolean, allowsFileAccess: Boolean): Long

    /**
     * Destroys a WebView and releases its resources.
     * @param webViewId The ID of the WebView to destroy.
     */
    fun destroyWebView(webViewId: Long)

    // ========== Navigation Callback ==========

    /**
     * Sets a callback to intercept navigation requests.
     * The callback is called before each navigation and can allow or cancel it.
     * @param webViewId The ID of the WebView.
     * @param callback The callback to set, or null to remove.
     */
    fun setNavigationCallback(webViewId: Long, callback: NavigationCallback?)

    // ========== Window Attachment ==========

    /**
     * Attaches the WebView to an NSWindow's content view.
     * @param webViewId The ID of the WebView.
     * @param nsWindowPtr Raw pointer to the NSWindow (from ComposeWindow.windowHandle).
     */
    fun attachWebViewToWindow(webViewId: Long, nsWindowPtr: Pointer)

    // ========== Frame/Position ==========

    /**
     * Sets the frame (position and size) of the WebView using macOS coordinates (bottom-left origin).
     * @param webViewId The ID of the WebView.
     * @param x X position from left edge.
     * @param y Y position from BOTTOM edge.
     * @param width Width of the WebView.
     * @param height Height of the WebView.
     */
    fun setWebViewFrame(webViewId: Long, x: Double, y: Double, width: Double, height: Double)

    /**
     * Sets the frame with flipped Y coordinate (top-left origin like Compose).
     * This is the preferred method when integrating with Compose.
     * @param webViewId The ID of the WebView.
     * @param x X position from left edge.
     * @param y Y position from TOP edge.
     * @param width Width of the WebView.
     * @param height Height of the WebView.
     * @param parentHeight Height of the parent view (needed for coordinate conversion).
     */
    fun setWebViewFrameFlipped(
        webViewId: Long,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        parentHeight: Double
    )

    // ========== Loading Content ==========

    /**
     * Loads a URL in the WebView.
     * @param webViewId The ID of the WebView.
     * @param urlString The URL string to load.
     * @return true if the URL was valid and loading started.
     */
    fun loadURL(webViewId: Long, urlString: String): Boolean

    /**
     * Loads HTML content directly into the WebView.
     * @param webViewId The ID of the WebView.
     * @param htmlString The HTML content to load.
     * @param baseURLString Optional base URL for resolving relative paths (can be null).
     */
    fun loadHTMLString(webViewId: Long, htmlString: String, baseURLString: String?)

    // ========== Navigation ==========

    /**
     * Navigates back in the WebView's history.
     * @param webViewId The ID of the WebView.
     */
    fun webViewGoBack(webViewId: Long)

    /**
     * Navigates forward in the WebView's history.
     * @param webViewId The ID of the WebView.
     */
    fun webViewGoForward(webViewId: Long)

    /**
     * Reloads the current page.
     * @param webViewId The ID of the WebView.
     */
    fun webViewReload(webViewId: Long)

    /**
     * Stops loading the current page.
     * @param webViewId The ID of the WebView.
     */
    fun webViewStopLoading(webViewId: Long)

    // ========== State Queries ==========

    /**
     * Checks if the WebView can navigate back.
     * @param webViewId The ID of the WebView.
     * @return true if back navigation is available.
     */
    fun webViewCanGoBack(webViewId: Long): Boolean

    /**
     * Checks if the WebView can navigate forward.
     * @param webViewId The ID of the WebView.
     * @return true if forward navigation is available.
     */
    fun webViewCanGoForward(webViewId: Long): Boolean

    /**
     * Checks if the WebView is currently loading.
     * @param webViewId The ID of the WebView.
     * @return true if a page is being loaded.
     */
    fun webViewIsLoading(webViewId: Long): Boolean

    /**
     * Gets the estimated loading progress.
     * @param webViewId The ID of the WebView.
     * @return Progress value from 0.0 to 1.0.
     */
    fun webViewGetProgress(webViewId: Long): Double

    // ========== JavaScript ==========

    /**
     * Evaluates JavaScript code in the WebView.
     * @param webViewId The ID of the WebView.
     * @param jsCode The JavaScript code to execute.
     */
    fun evaluateJavaScript(webViewId: Long, jsCode: String)

    // ========== Visibility ==========

    /**
     * Sets the visibility of the WebView.
     * @param webViewId The ID of the WebView.
     * @param visible Whether the WebView should be visible.
     */
    fun setWebViewVisible(webViewId: Long, visible: Boolean)

    /**
     * Sets the opacity of the WebView.
     * @param webViewId The ID of the WebView.
     * @param alpha Opacity value from 0.0 (transparent) to 1.0 (opaque).
     */
    fun setWebViewAlpha(webViewId: Long, alpha: Double)

    // ========== Z-Order ==========

    /**
     * Brings the WebView to the front of its siblings.
     * @param webViewId The ID of the WebView.
     */
    fun bringWebViewToFront(webViewId: Long)

    /**
     * Sends the WebView to the back of its siblings.
     * @param webViewId The ID of the WebView.
     */
    fun sendWebViewToBack(webViewId: Long)

    // ========== User Agent ==========

    /**
     * Sets a custom user agent string.
     * @param webViewId The ID of the WebView.
     * @param userAgent The custom user agent string.
     */
    fun setCustomUserAgent(webViewId: Long, userAgent: String)

    // ========== URL/Title ==========

    /**
     * Gets the current URL of the WebView.
     * Note: The returned pointer must be freed with freeString().
     * @param webViewId The ID of the WebView.
     * @return Pointer to a C string, or null if not available.
     */
    fun webViewGetCurrentURL(webViewId: Long): Pointer?

    /**
     * Gets the title of the current page.
     * Note: The returned pointer must be freed with freeString().
     * @param webViewId The ID of the WebView.
     * @return Pointer to a C string, or null if not available.
     */
    fun webViewGetTitle(webViewId: Long): Pointer?

    /**
     * Frees a string returned by webViewGetCurrentURL or webViewGetTitle.
     * @param str The pointer to free.
     */
    fun freeString(str: Pointer?)

    // ========== Window Helper ==========

    /**
     * Gets the height of an NSWindow's content view.
     * Useful for coordinate conversion.
     * @param nsWindowPtr Raw pointer to the NSWindow.
     * @return The height of the content view.
     */
    fun getWindowContentHeight(nsWindowPtr: Pointer): Double

    /**
     * Forces the WebView to display immediately.
     * Call this after attaching and setting frame to fix white screen issues.
     * @param webViewId The ID of the WebView.
     */
    fun forceWebViewDisplay(webViewId: Long)


    companion object {
        /**
         * Lazy-loaded instance of the native library.
         * The library must be named "libNativeUtils.dylib" and be in the library path.
         */
        val INSTANCE: MacWebViewNative by lazy {
            Native.load("NativeUtils", MacWebViewNative::class.java)
        }

        /**
         * Checks if we're running on macOS.
         */
        val isMacOS: Boolean by lazy {
            System.getProperty("os.name").lowercase().contains("mac")
        }
    }
}

/**
 * Helper extension functions for safer string handling.
 */
object MacWebViewHelper {

    /**
     * Safely gets the current URL as a Kotlin String.
     */
    fun getCurrentURL(webViewId: Long): String? {
        val ptr = MacWebViewNative.INSTANCE.webViewGetCurrentURL(webViewId) ?: return null
        return try {
            ptr.getString(0)
        } finally {
            MacWebViewNative.INSTANCE.freeString(ptr)
        }
    }

    /**
     * Safely gets the page title as a Kotlin String.
     */
    fun getTitle(webViewId: Long): String? {
        val ptr = MacWebViewNative.INSTANCE.webViewGetTitle(webViewId) ?: return null
        return try {
            ptr.getString(0)
        } finally {
            MacWebViewNative.INSTANCE.freeString(ptr)
        }
    }
}