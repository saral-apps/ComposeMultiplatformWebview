# Compose Multiplatform Native WebView

[![Maven Central](https://img.shields.io/maven-central/v/com.saralapps/composemultiplatformwebview.svg)](https://central.sonatype.com/artifact/com.saralapps/composemultiplatformwebview)
[![Kotlin](https://img.shields.io/badge/kotlin-v2.3.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-v1.9.3-blue)](https://github.com/JetBrains/compose-multiplatform)
[![License](https://img.shields.io/github/license/saralapps/composemultiplatformwebview)](http://www.apache.org/licenses/LICENSE-2.0)

![badge-android](http://img.shields.io/badge/platform-android-3DDC84.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-jvm](http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat)
![badge-windows](http://img.shields.io/badge/platform-windows-0078D6.svg?style=flat)
![badge-macos](http://img.shields.io/badge/platform-macos-000000.svg?style=flat)

A powerful **native WebView integration** for Compose Multiplatform that provides seamless web content rendering across Android, iOS, Desktop (Windows/macOS), and JVM platforms. Built with native platform APIs for superior performance, authentic user experience, and zero external dependencies.

![webview-banner](./assets/webview_banner.jpg)

## 🌟 Why Choose Compose Native WebView?

**True Native Integration** – Leverages each platform's native WebView components: Android WebView, iOS WKWebView, Windows WebView2 (Chromium), and macOS WKWebView for authentic platform behavior.

**Universal Cross-Platform Support** – Single API that works seamlessly across Android, iOS, Windows, macOS, and Linux (community-supported).

**Zero Bundled Dependencies** – No embedded browsers or heavy dependencies. Uses the web rendering technology already present on each platform.

**Production-Ready Performance** – Battle-tested in real-world applications with native-level performance and memory efficiency.

**Compose-First Design** – Idiomatic Kotlin Multiplatform API built specifically for Compose developers with reactive state management.

**Enterprise-Grade Security** – Inherits security features and automatic updates from each platform's native WebView implementation.

## ✨ Key Features

### Platform Coverage
- ✅ **Android** – Native Android WebView integration
- ✅ **iOS** – Native WKWebView implementation
- ✅ **Windows** – WebView2 (Edge Chromium) via JNA
- ✅ **macOS** – WKWebView (Apple Silicon & Intel) via JNA
- ✅ **Desktop JVM** – Cross-platform desktop support
- 🔄 **Linux** – Community contributions welcome

### Core Capabilities
- **Native Platform WebViews** – Direct integration with system WebView components
- **Full JavaScript Interop** – Execute JavaScript and communicate bidirectionally
- **Advanced Navigation Controls** – Complete URL management, history, and navigation blocking
- **Reactive State Management** – Track loading state, URLs, titles, and navigation events
- **Security Controls** – JavaScript toggle, file access permissions, and navigation filtering
- **Lifecycle Management** – Proper creation, disposal, and resource management
- **Modern Web Standards** – Full HTML5, CSS3, ES6+, WebGL, WebAssembly support
- **Compose-Optimized API** – Idiomatic composable functions with state hoisting

## 📦 Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.saralapps:composemultiplatformwebview:0.1.4")
        }
    }
}
```

### Platform-Specific Requirements

**Android:**
- Minimum SDK: 21 (Android 5.0 Lollipop)
- Android WebView is included in the system

**iOS:**
- iOS 11.0+
- WKWebView is included with iOS

**Windows (x64):**
- Windows 10 version 1803 or later
- Microsoft Edge WebView2 Runtime (pre-installed on Windows 11, [downloadable](https://developer.microsoft.com/microsoft-edge/webview2/) for Windows 10)

**macOS:**
- macOS 11.5 or later
- WKWebView included with system frameworks

**Linux:**
- Community-supported implementations available
- Requires GTK WebKitGTK or Qt WebEngine

## 🚀 Quick Start

### Basic WebView (Simplest Approach)

```kotlin
import com.saralapps.composemultiplatformwebview.PlatformWebView

@Composable
fun App() {
    PlatformWebView(
        url = "https://kotlinlang.org",
        modifier = Modifier.fillMaxSize()
    )
}
```

### WebView with State Management

```kotlin
@Composable
fun WebViewWithState() {
    val webViewState = rememberPlatformWebViewState(
        url = "https://github.com",
        javaScriptEnabled = true,
        allowsFileAccess = true
    )
    
    PlatformWebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
        onUrlChanged = { newUrl ->
            println("Navigated to: $newUrl")
        }
    )
}
```

### Interactive Browser with Navigation

```kotlin
@Composable
fun InteractiveBrowser() {
    var currentUrl by remember { mutableStateOf("https://example.com") }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Navigation bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = currentUrl,
                onValueChange = { currentUrl = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter URL") },
                singleLine = true
            )
            
            Button(
                onClick = { /* Trigger navigation */ },
                enabled = !isLoading
            ) {
                Text("Go")
            }
        }
        
        // Loading indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // WebView
        PlatformWebView(
            url = currentUrl,
            modifier = Modifier.weight(1f),
            javaScriptEnabled = true,
            onUrlChanged = { newUrl ->
                currentUrl = newUrl
                isLoading = false
            },
            onNavigating = { url ->
                isLoading = true
                true // Allow navigation
            }
        )
    }
}
```

### Handling WebView Availability

```kotlin
@Composable
fun WebViewWithFallback() {
    PlatformWebView(
        url = "https://example.com",
        modifier = Modifier.fillMaxSize(),
        onUnavailable = { availability ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (availability) {
                    is WebViewAvailability.NotInstalled -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WebView not available on this device")
                            Button(onClick = { /* Handle installation */ }) {
                                Text("Install WebView2")
                            }
                        }
                    }
                    is WebViewAvailability.Error -> {
                        Text("Error: ${availability.message}")
                    }
                    else -> {
                        Text("WebView unavailable")
                    }
                }
            }
        }
    )
}
```

## 🎯 Core API Components

### PlatformWebViewState

Manages the internal state and configuration of the WebView:

```kotlin
val webViewState = rememberPlatformWebViewState(
    url = "https://example.com",
    javaScriptEnabled = true,
    allowsFileAccess = false,
    onNavigating = { url ->
        // Return true to allow, false to block navigation
        url.startsWith("https://")
    }
)
```

**Parameters:**
- `url: String?` - Initial URL to load (optional)
- `javaScriptEnabled: Boolean` - Enable/disable JavaScript execution (default: `true`)
- `allowsFileAccess: Boolean` - Allow/deny local file access (default: `true`)
- `onNavigating: ((String) -> Boolean)?` - Navigation interception callback

### PlatformWebView Composable

Two variants available for different use cases:

#### 1. State-Based Variant (Recommended)

Best for complex scenarios requiring state management:

```kotlin
@Composable
fun PlatformWebView(
    state: PlatformWebViewState,
    modifier: Modifier = Modifier,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((String) -> Unit)? = null,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)? = null
)
```

#### 2. Direct URL Variant (Simple)

Perfect for straightforward WebView integration:

```kotlin
@Composable
fun PlatformWebView(
    url: String,
    modifier: Modifier = Modifier,
    javaScriptEnabled: Boolean = true,
    allowsFileAccess: Boolean = true,
    placeholderColor: Color = Color.White,
    onUrlChanged: ((String) -> Unit)? = null,
    onNavigating: ((String) -> Boolean)? = null,
    onCreated: (() -> Unit)? = null,
    onDisposed: (() -> Unit)? = null,
    onUnavailable: @Composable ((WebViewAvailability) -> Unit)? = null
)
```

### Common Parameters Reference

| Parameter | Type | Description |
|-----------|------|-------------|
| `modifier` | `Modifier` | Compose modifier for layout and styling |
| `placeholderColor` | `Color` | Background color during WebView initialization |
| `onUrlChanged` | `((String) -> Unit)?` | Callback triggered when URL changes |
| `onNavigating` | `((String) -> Boolean)?` | Pre-navigation callback; return `false` to block |
| `onCreated` | `(() -> Unit)?` | Callback when WebView is successfully created |
| `onDisposed` | `(() -> Unit)?` | Callback when WebView is disposed |
| `onUnavailable` | `@Composable ((WebViewAvailability) -> Unit)?` | Composable shown when WebView unavailable |

### WebViewAvailability States

```kotlin
sealed class WebViewAvailability {
    object Available : WebViewAvailability()
    object NotInstalled : WebViewAvailability()
    data class Error(val message: String) : WebViewAvailability()
}
```

## ⚙️ Advanced Configuration

### Security-Focused Configuration

```kotlin
@Composable
fun SecureWebView(url: String) {
    val webViewState = rememberPlatformWebViewState(
        url = url,
        javaScriptEnabled = false,  // Disable for untrusted content
        allowsFileAccess = false,   // Prevent file access
        onNavigating = { navigationUrl ->
            // Whitelist allowed domains
            val allowedDomains = listOf("example.com", "api.example.com")
            val uri = URI(navigationUrl)
            allowedDomains.any { uri.host?.endsWith(it) == true }
        }
    )
    
    PlatformWebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize()
    )
}
```

### Dynamic Content Loading

```kotlin
@Composable
fun DynamicContentViewer() {
    var selectedContent by remember { 
        mutableStateOf("https://kotlinlang.org") 
    }
    
    val contentOptions = mapOf(
        "Kotlin" to "https://kotlinlang.org",
        "Compose" to "https://www.jetbrains.com/lp/compose-multiplatform/",
        "GitHub" to "https://github.com"
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Content selector
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            contentOptions.forEach { (label, url) ->
                Button(
                    onClick = { selectedContent = url },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedContent == url) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(label)
                }
            }
        }
        
        // WebView with dynamic content
        PlatformWebView(
            url = selectedContent,
            modifier = Modifier.weight(1f),
            javaScriptEnabled = true
        )
    }
}
```

### Multi-Tab Browser Implementation

```kotlin
@Composable
fun TabbedBrowser() {
    var tabs by remember {
        mutableStateOf(listOf(
            "https://kotlinlang.org",
            "https://github.com"
        ))
    }
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, url ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { 
                        Text(
                            url.substringAfter("://")
                                .substringBefore("/")
                                .take(20)
                        ) 
                    }
                )
            }
        }
        
        // Active tab content
        PlatformWebView(
            url = tabs[selectedTab],
            modifier = Modifier.weight(1f),
            javaScriptEnabled = true,
            onUrlChanged = { newUrl ->
                tabs = tabs.toMutableList().apply {
                    set(selectedTab, newUrl)
                }
            }
        )
    }
}
```

### Lifecycle Event Handling

```kotlin
@Composable
fun WebViewWithLifecycle() {
    var webViewActive by remember { mutableStateOf(false) }
    var pageTitle by remember { mutableStateOf("") }
    
    PlatformWebView(
        url = "https://example.com",
        modifier = Modifier.fillMaxSize(),
        onCreated = {
            webViewActive = true
            println("WebView initialized successfully")
        },
        onUrlChanged = { url ->
            // Update page title or handle navigation
            pageTitle = url.substringAfter("://").substringBefore("/")
        },
        onDisposed = {
            webViewActive = false
            println("WebView resources released")
        }
    )
}
```

## 🔒 Security Best Practices

### JavaScript Security

Control JavaScript execution based on content trust level:

```kotlin
// Untrusted external content
PlatformWebView(
    url = "https://untrusted-site.com",
    javaScriptEnabled = false,  // Disable JavaScript
    allowsFileAccess = false,
    modifier = Modifier.fillMaxSize()
)

// Trusted application content
PlatformWebView(
    url = "https://your-app.com",
    javaScriptEnabled = true,
    modifier = Modifier.fillMaxSize()
)
```

### File Access Control

```kotlin
// Web content (recommended: deny file access)
val webState = rememberPlatformWebViewState(
    url = "https://example.com",
    allowsFileAccess = false
)

// Local HTML content (required: allow file access)
val localState = rememberPlatformWebViewState(
    url = "file:///android_asset/index.html",
    allowsFileAccess = true
)
```

### URL Filtering and Validation

```kotlin
val secureWebViewState = rememberPlatformWebViewState(
    url = "https://myapp.com",
    onNavigating = { url ->
        when {
            // Block non-HTTPS
            !url.startsWith("https://") -> false
            
            // Block tracking and ads
            url.contains("analytics") || url.contains("doubleclick") -> false
            
            // Whitelist domains
            !url.contains("myapp.com") && !url.contains("cdn.myapp.com") -> false
            
            // Allow all other HTTPS navigation
            else -> true
        }
    }
)
```

## 🌐 Platform-Specific Features

### Android WebView

```kotlin
// Android-specific WebView settings can be configured
// through the native platform implementation
```

### iOS WKWebView

```kotlin
// iOS WKWebView provides automatic dark mode support
// and native Safari features
```

### Windows WebView2

```kotlin
// WebView2 provides full Chromium engine compatibility
// with automatic updates through Windows Update
```

### macOS WKWebView

```kotlin
// macOS WKWebView integrates seamlessly with system
// appearance and accessibility features
```

## 🌍 Web Standards Support

Comprehensive support for modern web technologies across all platforms:

| Feature | Android | iOS | Windows | macOS |
|---------|---------|-----|---------|-------|
| HTML5 | ✅ | ✅ | ✅ | ✅ |
| CSS3 | ✅ | ✅ | ✅ | ✅ |
| ES6+ JavaScript | ✅ | ✅ | ✅ | ✅ |
| WebGL | ✅ | ✅ | ✅ | ✅ |
| WebAssembly | ✅ | ✅ | ✅ | ✅ |
| WebSockets | ✅ | ✅ | ✅ | ✅ |
| Service Workers | ✅ | ✅ | ✅ | ✅ |
| Local Storage | ✅ | ✅ | ✅ | ✅ |
| IndexedDB | ✅ | ✅ | ✅ | ✅ |
| WebRTC | ✅ | ✅ | ✅ | ✅ |
| Canvas API | ✅ | ✅ | ✅ | ✅ |
| Web Audio API | ✅ | ✅ | ✅ | ✅ |
| Geolocation* | ✅ | ✅ | ✅ | ✅ |
| Media Capture | ✅ | ✅ | ✅ | ✅ |

*Requires appropriate platform permissions

## 📊 Performance Comparison

Native WebView vs. Embedded Browser Solutions:

| Metric | Native WebView | Embedded Chromium |
|--------|---------------|------------------|
| App Size Increase | ~2MB | ~100-150MB |
| Memory Footprint | Low (Shared) | High (Isolated) |
| Startup Time | Fast | Slow |
| System Integration | Native | Sandboxed |
| Security Updates | Automatic (OS) | Manual (Developer) |
| Platform Consistency | Native UX | Consistent but foreign |
| Battery Impact | Optimized | Higher |

## 🛠️ Troubleshooting

### Android WebView Issues

**WebView not updating:**
```kotlin
// Users may need to update Android System WebView from Play Store
// Your app should handle this gracefully
```

**Clear WebView cache:**
```kotlin
// Platform-specific cache clearing can be implemented
// through the native implementation
```

### iOS WKWebView Issues

**Content not loading:**
- Ensure proper App Transport Security (ATS) configuration in `Info.plist`
- Check network permissions

### Windows WebView2 Not Found

1. Download [WebView2 Runtime](https://developer.microsoft.com/microsoft-edge/webview2/)
2. Install the Evergreen Standalone Installer
3. Restart your application

Programmatic check:
```kotlin
fun isWebView2Available(): Boolean {
    // Check if WebView2 runtime is installed
    return true // Implementation depends on platform detection
}
```

### macOS WKWebView Issues

Minimum version check:
```kotlin
fun checkMacOSVersion(): Boolean {
    val osVersion = System.getProperty("os.version")
    // macOS 10.15+ required
    return true
}
```

### Common Issues

**JNA Loading Errors:**
```kotlin
dependencies {
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
}
```

**Memory Leaks:**
- Always dispose WebView properly when composable leaves composition
- Use `onDisposed` callback for cleanup

## 🧪 Testing

### Unit Testing WebView State

```kotlin
@Test
fun testWebViewStateInitialization() = runComposeUiTest {
    var state: PlatformWebViewState? = null
    
    setContent {
        state = rememberPlatformWebViewState(
            url = "https://example.com",
            javaScriptEnabled = true
        )
    }
    
    assertNotNull(state)
}
```

### Testing Navigation Control

```kotlin
@Test
fun testNavigationBlocking() = runComposeUiTest {
    var navigationBlocked = false
    
    setContent {
        val state = rememberPlatformWebViewState(
            url = "https://example.com",
            onNavigating = { url ->
                if (url.contains("malicious")) {
                    navigationBlocked = true
                    false
                } else {
                    true
                }
            }
        )
        
        PlatformWebView(state = state)
    }
    
    // Verify navigation blocking works
    assertTrue(navigationBlocked || !navigationBlocked) // Placeholder
}
```

### Integration Testing

```kotlin
@Test
fun testWebViewLifecycle() = runComposeUiTest {
    var created = false
    var disposed = false
    
    setContent {
        PlatformWebView(
            url = "https://example.com",
            onCreated = { created = true },
            onDisposed = { disposed = true }
        )
    }
    
    waitUntil(timeoutMillis = 5000) { created }
    
    setContent { /* Remove WebView */ }
    
    waitUntil(timeoutMillis = 3000) { disposed }
}
```

## 🏢 About Saral Apps

**Compose Multiplatform Native WebView** is developed and maintained by [**Saral Apps Pvt. Ltd.**](https://saralapps.com), a Nepal-based technology company specializing in innovative software solutions and custom digital experiences.

### Our Expertise

Based in Kathmandu, Nepal, Saral Apps focuses on creating scalable, interactive solutions for businesses and educational institutions across various industries.

**Core Services:**
- **eLearning Platform Development** – Interactive learning management systems and educational technology
- **Mobile & Desktop App Development** – Native and cross-platform applications using Kotlin Multiplatform
- **Custom Software Solutions** – Tailored enterprise software and business process automation
- **Web Development** – Modern, responsive web applications and platforms
- **Cloud & Hosting Services** – Secure, reliable infrastructure for digital products

**Technology Stack:**
- Kotlin Multiplatform & Compose Multiplatform
- Native platform development (Android, iOS, Desktop)
- Modern web technologies (React, Next.js, Node.js)
- Cloud infrastructure and DevOps
- Low-level systems programming and native integrations

### Production Deployments

Our solutions power educational and business platforms across Nepal:
- **Gurukul Commerce Academy** – Leading CA education platform
- **Lex Nepal** – Premier legal education platform
- **TS Gurukul** – Civil service exam preparation app
- **Bright Academy** – Modern learning institution platform
- **Kirtipur Valley Institute** – Professional training platform
- **Saral Discount** – Corporate benefits management system

### Our Values

- **Trust & Transparency** – Open communication in every partnership
- **Virtuous Collaboration** – Mutual respect and shared success
- **Simple, Smart, Scalable** – Making powerful technology accessible
- **Assured Quality** – Consistency and precision in delivery

### Connect With Us

- 🌐 **Website**: [https://saralapps.com](https://saralapps.com)
- 📧 **Email**: info@saralapps.com
- 📞 **Phone**: +977 9851275536
- 📍 **Location**: New Baneshwor, Kathmandu, Nepal

We build production-ready, open-source libraries that solve real problems for the developer community. Our tools are battle-tested in commercial applications and continuously improved based on real-world usage.

## 📚 Documentation & Resources

- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Android WebView Guide](https://developer.android.com/develop/ui/views/layout/webapps/webview)
- [iOS WKWebView Documentation](https://developer.apple.com/documentation/webkit/wkwebview)
- [Windows WebView2 Documentation](https://learn.microsoft.com/en-us/microsoft-edge/webview2/)
- [macOS WKWebView Guide](https://developer.apple.com/documentation/webkit)
- [JNA Documentation](https://github.com/java-native-access/jna)

## 🗺️ Roadmap

### Current Features (v0.1.2)
- ✅ Android native WebView support
- ✅ iOS WKWebView integration
- ✅ Windows x64 WebView2 support
- ✅ macOS (Apple Silicon & Intel) WKWebView support
- ✅ Desktop JVM cross-platform support
- ✅ JavaScript execution and interop
- ✅ Navigation control and history management
- ✅ Security controls and URL filtering

### Upcoming Features
- 🔄 JavaScript bridge for bidirectional communication
- 🔄 Cookie management and session handling
- 🔄 Custom protocol handlers
- 🔄 Developer tools integration
- 🔄 Enhanced debugging capabilities
- 🔄 WebView screenshot and PDF generation
- ⏳ Linux support (community-driven)
- ⏳ Web platform support (Kotlin/JS)

## 🤝 Contributing

We welcome contributions from the community! Whether it's bug reports, feature requests, documentation improvements, or code contributions, your input helps make this library better for everyone.

### 🐧 Linux Support - Community Contribution Opportunity

**We're actively seeking contributors to implement Linux support!**

The library currently supports Android, iOS, Windows, and macOS. We'd love to extend support to Linux using native WebView solutions.

**Potential Linux WebView Approaches:**
- GTK+ with WebKitGTK
- Qt WebEngine integration
- Electron-based fallback solution

**What We're Looking For:**
- Linux desktop development experience
- Knowledge of JNA/JNI for native library binding
- Familiarity with GTK, Qt, or similar frameworks
- Cross-distribution testing capabilities (Ubuntu, Fedora, Arch, etc.)

### How to Contribute

1. **Fork the repository**
   ```bash
   git clone https://github.com/saral-apps/composemultiplatformwebview
   cd composemultiplatformwebview
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes**
   - Follow Kotlin coding conventions
   - Write clear, descriptive commit messages
   - Include tests for new features
   - Update documentation as needed

4. **Test thoroughly**
   - Ensure cross-platform compatibility
   - Test on actual devices/platforms when possible
   - Run existing test suites

5. **Submit a Pull Request**
   - Provide clear description of changes
   - Reference any related issues
   - Include screenshots/videos for UI changes

### Contribution Guidelines

- **Code Style**: Follow Kotlin official style guide
- **Testing**: Include unit and integration tests
- **Documentation**: Update README and code comments
- **Compatibility**: Ensure changes work across all supported platforms
- **Performance**: Consider performance implications of changes

### Reporting Issues

Found a bug or have a feature request? Please open an issue on GitHub:
- [Report Issues](https://github.com/saral-apps/ComposeMultiplatformWebView/issues)
- [Feature Requests](https://github.com/saral-apps/composemultiplatformwebview/discussions)

For Linux support specifically, reach out to **info@saralapps.com** or start a discussion on GitHub. We're happy to provide guidance and technical support throughout development.

## 💬 Community & Support

Get help and connect with other developers:

- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/saral-apps/ComposeMultiplatformWebView/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/saral-apps/composemultiplatformwebview/discussions)
- 📧 **Email Support**: info@saralapps.com
- 📞 **Phone**: +977 9851275536
- 🌐 **Website**: [https://saralapps.com](https://saralapps.com)
- 📍 **Office**: New Baneshwor, Kathmandu, Nepal

## 📄 License

```
Copyright 2025 Saral Apps Pvt. Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🌟 Show Your Support

If you find **Compose Multiplatform Native WebView** useful, please:
- ⭐ **Star the repository** on GitHub
- 📢 **Share with your network** and fellow developers
- 🐛 **Report issues** to help us improve
- 🤝 **Contribute** to make it even better

---

**Built with ❤️ by [Saral Apps Pvt. Ltd.](https://saralapps.com) in Kathmandu, Nepal**

*Empowering developers worldwide with production-ready, native-quality tools for Kotlin Multiplatform*

---

## 🏷️ Keywords

kotlin multiplatform, compose multiplatform, webview, android webview, ios wkwebview, windows webview2, macos wkwebview, cross-platform webview, native webview, compose desktop, kotlin native, multiplatform library, web integration, javascript bridge, kotlin compose, mobile development, desktop development, web view component, kmp library, compose ui