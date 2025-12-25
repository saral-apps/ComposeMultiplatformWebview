package com.saralapps.composemultiplatformwebview.data

import platform.UIKit.UIDevice

actual fun checkWebViewAvailability(): WebViewAvailability {
    val device = UIDevice.currentDevice
    return WebViewAvailability(
        isAvailable = true,
        platform = "iOS ${device.systemVersion}",
        errorMessage = null,
        downloadUrl = null
    )
}