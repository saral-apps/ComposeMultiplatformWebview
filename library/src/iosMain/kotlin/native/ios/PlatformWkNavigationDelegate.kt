package com.saralapps.composemultiplatformwebview.native.ios

import com.saralapps.composemultiplatformwebview.data.PlatformWebViewState
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

/**
 * Navigation delegate for WKWebView.
 * Handles page load events and navigation interception.
 */
@OptIn(ExperimentalForeignApi::class)
@Suppress("CONFLICTING_OVERLOADS")
class PlatformWKNavigationDelegate(
    private val state: PlatformWebViewState
) : NSObject(), WKNavigationDelegateProtocol {

    /**
     * Called when the web view begins to receive web content.
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?
    ) {
        state.setIsLoading(true)
        webView.URL?.absoluteString?.let { url ->
            state.setCurrentUrl(url)
        }
        state.updateNavigationState()
    }

    /**
     * Called when the web view commits a navigation.
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didCommitNavigation: WKNavigation?
    ) {
        // Navigation committed, content is being loaded
    }

    /**
     * Called when the web view finishes loading.
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?
    ) {
        state.setIsLoading(false)
        state.setNavigatingUrl(null)
        state.setPageTitle(webView.title)
        webView.URL?.absoluteString?.let { url ->
            state.setCurrentUrl(url)
        }
        state.updateNavigationState()
    }

    /**
     * Called when the web view fails to load content.
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError
    ) {
        state.setIsLoading(false)
        println("WebView failed to load: ${withError.localizedDescription}")
    }

    /**
     * Called when navigation fails after content started loading.
     */
    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailNavigation: WKNavigation?,
        withError: NSError
    ) {
        state.setIsLoading(false)
        println("WebView navigation failed: ${withError.localizedDescription}")
    }

    /**
     * Decides whether to allow or cancel a navigation action.
     */
    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit
    ) {
        val url = decidePolicyForNavigationAction.request.URL?.absoluteString

        if (url != null) {
            // Check navigation interceptor
            if (state.shouldAllowNavigation(url)) {
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
            } else {
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
            }
        } else {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }
}