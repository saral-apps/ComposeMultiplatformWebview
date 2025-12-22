package com.saralapps.composemultiplatformwebview.data

actual class PlatformWebViewState {
    actual val currentUrl: String?
        get() = TODO("Not yet implemented")
    actual val navigatingUrl: String?
        get() = TODO("Not yet implemented")
    actual val isLoading: Boolean
        get() = TODO("Not yet implemented")
    actual val canGoBack: Boolean
        get() = TODO("Not yet implemented")
    actual val canGoForward: Boolean
        get() = TODO("Not yet implemented")
    actual val pageTitle: String?
        get() = TODO("Not yet implemented")

    actual fun loadUrl(url: String) {
    }

    actual fun loadHtml(html: String, baseUrl: String?) {
    }

    actual fun goBack() {
    }

    actual fun goForward() {
    }

    actual fun reload() {
    }

    actual fun stopLoading() {
    }

    actual fun evaluateJavaScript(code: String) {
    }

    actual fun setNavigationInterceptor(interceptor: ((String) -> Boolean)?) {
    }
}