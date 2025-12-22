package com.saralapps.composemultiplatformwebview.data

import com.saralapps.composemultiplatformwebview.native.mac.NativeWebViewState
import com.saralapps.composemultiplatformwebview.native.windows.WindowsWebViewState

actual class PlatformWebViewState internal constructor(
    internal val macState: NativeWebViewState?,
    internal val windowsState: WindowsWebViewState?
) {
    actual val currentUrl: String?
        get() = macState?.currentUrl ?: windowsState?.currentUrl
    actual val navigatingUrl: String?
        get() = macState?.navigatingUrl ?: windowsState?.navigatingUrl
    actual val isLoading: Boolean
        get() = macState?.isLoading ?: windowsState?.isLoading ?: false
    actual val canGoBack: Boolean
        get() = macState?.canGoBack ?: windowsState?.canGoBack ?: false
    actual val canGoForward: Boolean
        get() = macState?.canGoForward ?: windowsState?.canGoForward ?: false
    actual val pageTitle: String?
        get() = macState?.pageTitle ?: windowsState?.pageTitle

    actual fun loadUrl(url: String) {
        macState?.loadUrl(url)
        windowsState?.loadUrl(url)
    }

    actual fun loadHtml(html: String, baseUrl: String?) {
        macState?.loadHtml(html, baseUrl)
        windowsState?.loadHtml(html)
    }

    actual fun goBack() {
        macState?.goBack()
        windowsState?.goBack()
    }

    actual fun goForward() {
        macState?.goForward()
        windowsState?.goForward()
    }

    actual fun reload() {
        macState?.reload()
        windowsState?.reload()
    }

    actual fun stopLoading() {
        macState?.stopLoading()
        windowsState?.stopLoading()
    }

    actual fun evaluateJavaScript(code: String) {
        macState?.evaluateJavaScript(code)
        windowsState?.evaluateJavaScript(code)
    }

    actual fun setNavigationInterceptor(interceptor: ((String) -> Boolean)?) {
        macState?.setNavigationInterceptor(interceptor)
        windowsState?.setNavigationInterceptor(interceptor)
    }
}