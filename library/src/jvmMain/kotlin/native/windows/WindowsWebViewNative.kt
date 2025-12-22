package com.saralapps.composemultiplatformwebview.native.windows

import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * Callback interface for navigation interception on Windows.
 * Called before each navigation with the URL.
 * Return true to allow navigation, false to cancel.
 */
interface NavigationCallback : Callback {
    /**
     * Called when a navigation is about to occur.
     * @param webViewId The WebView ID
     * @param url The URL being navigated to
     * @return true to allow navigation, false to cancel
     */
    fun invoke(webViewId: Long, url: String): Boolean
}

/**
 * JNA interface for the native Windows WebView2 library.
 * This interface maps to the C-callable functions exported from NativeWebView.dll.
 *
 * Usage:
 *   // Check if WebView2 is available
 *   if (WindowsWebViewNative.INSTANCE.isWebView2Available()) {
 *       WindowsWebViewNative.INSTANCE.initializeEnvironment()
 *       // Wait for environment to be ready
 *       while (!WindowsWebViewNative.INSTANCE.isEnvironmentReady()) {
 *           Thread.sleep(10)
 *       }
 *       val webViewId = WindowsWebViewNative.INSTANCE.createWebView(hwnd)
 *       WindowsWebViewNative.INSTANCE.loadURL(webViewId, "https://example.com")
 *   }
 */
interface WindowsWebViewNative : Library {

    // ========== WebView2 Runtime Check ==========

    /**
     * Checks if WebView2 Runtime is installed on the system.
     * @return true if WebView2 is available, false otherwise.
     */
    fun isWebView2Available(): Boolean

    /**
     * Gets the installed WebView2 Runtime version.
     * @return Pointer to version string (caller must free with freeString), or null if not installed.
     */
    fun getWebView2Version(): Pointer?

    /**
     * Gets the URL to download WebView2 Runtime if not installed.
     * @return URL string to Microsoft's WebView2 download page.
     */
    fun getWebView2DownloadUrl(): String

    // ========== Environment ==========

    /**
     * Initializes the WebView2 environment. Must be called before creating WebViews.
     * This is an async operation - use isEnvironmentReady() to check status.
     * @return true if initialization started successfully.
     */
    fun initializeEnvironment(): Boolean

    /**
     * Checks if the WebView2 environment is ready.
     * @return true if environment is initialized and ready.
     */
    fun isEnvironmentReady(): Boolean

    /**
     * Shuts down the WebView2 environment. Call when application exits.
     */
    fun shutdownEnvironment()

    // ========== Lifecycle ==========

    /**
     * Creates a new WebView instance.
     * @param hwnd The parent window handle (HWND).
     * @return A unique ID for the WebView, or 0 if creation failed.
     */
    fun createWebView(hwnd: Pointer): Long

    /**
     * Creates a new WebView with custom settings.
     * @param hwnd The parent window handle.
     * @param javaScriptEnabled Whether JavaScript is enabled.
     * @param allowFileAccess Whether file:// URLs can access local files.
     * @return A unique ID for the WebView, or 0 if creation failed.
     */
    fun createWebViewWithSettings(
        hwnd: Pointer,
        javaScriptEnabled: Boolean,
        allowFileAccess: Boolean
    ): Long

    /**
     * Checks if a WebView is ready (fully created and initialized).
     * WebView2 creation is async, so use this to wait until ready.
     * @param webViewId The ID of the WebView.
     * @return true if the WebView is ready to use.
     */
    fun isWebViewReady(webViewId: Long): Boolean

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

    // ========== Frame/Position ==========

    /**
     * Sets the position and size of the WebView.
     * @param webViewId The ID of the WebView.
     * @param x X position in pixels.
     * @param y Y position in pixels.
     * @param width Width in pixels.
     * @param height Height in pixels.
     */
    fun setWebViewBounds(webViewId: Long, x: Int, y: Int, width: Int, height: Int)

    /**
     * Sets the visibility of the WebView.
     * @param webViewId The ID of the WebView.
     * @param visible Whether the WebView should be visible.
     */
    fun setWebViewVisible(webViewId: Long, visible: Boolean)

    // ========== Loading Content ==========

    /**
     * Loads a URL in the WebView.
     * @param webViewId The ID of the WebView.
     * @param url The URL to load.
     * @return true if the URL was accepted.
     */
    fun loadURL(webViewId: Long, url: String): Boolean

    /**
     * Loads HTML content directly.
     * @param webViewId The ID of the WebView.
     * @param html The HTML content.
     */
    fun loadHTMLString(webViewId: Long, html: String)

    // ========== Navigation Controls ==========

    /**
     * Navigates back in history.
     * @param webViewId The ID of the WebView.
     */
    fun webViewGoBack(webViewId: Long)

    /**
     * Navigates forward in history.
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
     * @return true if loading.
     */
    fun webViewIsLoading(webViewId: Long): Boolean

    /**
     * Gets the current URL.
     * @param webViewId The ID of the WebView.
     * @return Pointer to URL string (caller must free with freeString), or null.
     */
    fun webViewGetCurrentURL(webViewId: Long): Pointer?

    /**
     * Gets the page title.
     * @param webViewId The ID of the WebView.
     * @return Pointer to title string (caller must free with freeString), or null.
     */
    fun webViewGetTitle(webViewId: Long): Pointer?

    // ========== JavaScript ==========

    /**
     * Evaluates JavaScript code in the WebView.
     * @param webViewId The ID of the WebView.
     * @param jsCode The JavaScript code to execute.
     */
    fun evaluateJavaScript(webViewId: Long, jsCode: String)

    // ========== User Agent ==========

    /**
     * Sets a custom user agent string.
     * @param webViewId The ID of the WebView.
     * @param userAgent The custom user agent.
     */
    fun setCustomUserAgent(webViewId: Long, userAgent: String)

    // ========== Utility ==========

    /**
     * Frees a string returned by this library.
     * @param str The pointer to free.
     */
    fun freeString(str: Pointer?)

    /**
     * Processes pending WebView2 messages. Call periodically from message loop.
     */
    fun processMessages()

    companion object {
        /**
         * Lazy-loaded instance of the native library.
         * The library must be named "NativeWebView.dll" and be in the library path.
         * DLLs are auto-extracted from resources on first access.
         */
        val INSTANCE: WindowsWebViewNative by lazy {
            // Ensure DLLs are extracted before loading
            WindowsDllLoader.ensureLoaded()
            Native.load("NativeWebView", WindowsWebViewNative::class.java)
        }

        /**
         * Checks if we're running on Windows.
         */
        val isWindows: Boolean by lazy {
            System.getProperty("os.name").lowercase().contains("win")
        }

        /**
         * Checks if the library is available for loading.
         */
        fun isLibraryAvailable(): Boolean {
            if (!isWindows) return false

            return try {
                // This will trigger DLL extraction and loading
                INSTANCE.isWebView2Available()
                true
            } catch (e: UnsatisfiedLinkError) {
                println("[WindowsWebViewNative] Library not available: ${e.message}")
                false
            } catch (e: Throwable) {
                println("[WindowsWebViewNative] Error loading library: ${e.message}")
                false
            }
        }
    }
}

/**
 * Helper functions for safer string handling.
 */
object WindowsWebViewHelper {

    /**
     * Safely gets the WebView2 version as a Kotlin String.
     */
    fun getVersion(): String? {
        val ptr = WindowsWebViewNative.INSTANCE.getWebView2Version() ?: return null
        return try {
            ptr.getString(0)
        } finally {
            WindowsWebViewNative.INSTANCE.freeString(ptr)
        }
    }

    /**
     * Safely gets the current URL as a Kotlin String.
     */
    fun getCurrentURL(webViewId: Long): String? {
        val ptr = WindowsWebViewNative.INSTANCE.webViewGetCurrentURL(webViewId) ?: return null
        return try {
            ptr.getString(0)
        } finally {
            WindowsWebViewNative.INSTANCE.freeString(ptr)
        }
    }

    /**
     * Safely gets the page title as a Kotlin String.
     */
    fun getTitle(webViewId: Long): String? {
        val ptr = WindowsWebViewNative.INSTANCE.webViewGetTitle(webViewId) ?: return null
        return try {
            ptr.getString(0)
        } finally {
            WindowsWebViewNative.INSTANCE.freeString(ptr)
        }
    }
}

/**
 * WebView2 availability checker with detailed status.
 */
object WebView2Checker {

    enum class Status {
        AVAILABLE,
        NOT_INSTALLED,
        LIBRARY_NOT_FOUND,
        NOT_WINDOWS
    }

    data class CheckResult(
        val status: Status,
        val version: String? = null,
        val downloadUrl: String? = null,
        val errorMessage: String? = null
    )

    /**
     * Checks WebView2 availability and returns detailed status.
     */
    fun check(): CheckResult {
        // Check if running on Windows
        if (!WindowsWebViewNative.isWindows) {
            return CheckResult(
                status = Status.NOT_WINDOWS,
                errorMessage = "WebView2 is only available on Windows"
            )
        }

        // Try to load the native library
        return try {
            val native = WindowsWebViewNative.INSTANCE

            if (native.isWebView2Available()) {
                val version = WindowsWebViewHelper.getVersion()
                CheckResult(
                    status = Status.AVAILABLE,
                    version = version
                )
            } else {
                CheckResult(
                    status = Status.NOT_INSTALLED,
                    downloadUrl = native.getWebView2DownloadUrl(),
                    errorMessage = "WebView2 Runtime is not installed"
                )
            }
        } catch (e: UnsatisfiedLinkError) {
            CheckResult(
                status = Status.LIBRARY_NOT_FOUND,
                errorMessage = "NativeWebView.dll not found: ${e.message}"
            )
        } catch (e: Throwable) {
            CheckResult(
                status = Status.LIBRARY_NOT_FOUND,
                errorMessage = "Error loading library: ${e.message}"
            )
        }
    }

    /**
     * Quick check if WebView2 is available and ready to use.
     */
    fun isAvailable(): Boolean {
        return check().status == Status.AVAILABLE
    }
}