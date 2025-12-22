# Compose Multiplatform Native WebView

[![Maven Central](https://img.shields.io/maven-central/v/com.saralapps/composemultiplatformwebview.svg)](https://central.sonatype.com/artifact/com.saralapps/composemultiplatformwebview)
[![Kotlin](https://img.shields.io/badge/kotlin-v2.3.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-v1.9.3-blue)](https://github.com/JetBrains/compose-multiplatform)
[![License](https://img.shields.io/github/license/saralapps/composemultiplatformwebview)](http://www.apache.org/licenses/LICENSE-2.0)

![badge-jvm](http://img.shields.io/badge/platform-jvm-DB413D.svg?style=flat)
![badge-windows](http://img.shields.io/badge/platform-windows-0078D6.svg?style=flat)
![badge-macos](http://img.shields.io/badge/platform-macos-000000.svg?style=flat)

A powerful native WebView integration for Compose Multiplatform Desktop (JVM) that leverages system-native web rendering engines through JNA (Java Native Access). Unlike traditional embedded browsers, this library provides direct access to the operating system's native WebView components, delivering superior performance, native feel, and seamless integration with platform features.

![webview-banner](./assets/webview_banner.jpg)

## 🌟 Why Native WebView?

**True Native Performance** – Access Windows WebView2 (Chromium) and macOS WKWebView directly through JNA, eliminating the overhead of embedded browsers.

**Zero External Dependencies** – No need to bundle Chromium or other browser engines. Uses the web rendering technology already present on the user's system.

**Authentic Platform Experience** – WebView behaves exactly like native applications, respecting system settings, security policies, and user preferences.

**Lightweight Distribution** – Dramatically smaller application size compared to solutions that bundle entire browser engines.

**Enterprise-Grade Security** – Leverage the security features and updates provided by the operating system's native WebView.

## ✨ Features

**Cross-Platform Desktop Support** – Works seamlessly on Windows (x64) and macOS (Apple Silicon & Intel).

**JNA-Powered Native Integration** – Direct access to system WebView APIs through Java Native Access for maximum performance.

**WebView2 on Windows** – Utilizes Microsoft Edge WebView2 runtime (Chromium-based) for modern web standards support.

**WKWebView on macOS** – Leverages Apple's WKWebView for optimal performance and native macOS integration.

**Full JavaScript Interop** – Execute JavaScript code and communicate bidirectionally between Compose and web content.

**Navigation Controls** – Complete control over page navigation with back, forward, reload, and URL loading capabilities.

**State Management** – Track loading state, page title, URL changes, and navigation history.

**Compose-First API** – Idiomatic Kotlin API designed specifically for Compose Multiplatform developers.

## 🏢 About Saral Apps

This library is developed and maintained by **[Saral Apps Pvt. Ltd.](https://saralapps.com)**, a Nepal-based technology company specializing in custom digital experiences and innovative software solutions.

### What We Do

Saral Apps Pvt. Ltd. is a technology solutions provider with a strong focus on developing eLearning platforms and custom digital experiences. Based in Kathmandu, Nepal, we're dedicated to creating interactive, scalable, and engaging solutions tailored to educators, businesses, and learners across various industries.

**Our Core Services:**
- **eLearning Platform Development** – Interactive and scalable learning solutions for educational institutions and corporate training
- **Mobile App Development** – Native and cross-platform applications for iOS, Android, and Desktop
- **Website Design & Development** – User-friendly, conversion-focused web solutions
- **Custom Software Development** – Tailored software to streamline complex business processes
- **Web Hosting Services** – Reliable, secure hosting solutions for digital assets

**Our Expertise:**
- Cross-platform application development with modern frameworks
- Native system integration and low-level programming
- UI/UX design focused on user experience and engagement
- Educational technology and digital learning platforms
- Enterprise software solutions

**Our Values:**
- **Trust & Transparency** – Open communication and honest collaboration
- **Virtuous Partnership** – Building relationships based on respect and mutual success
- **Simple, Smart, Scalable** – Making technology accessible without compromising power
- **Assured Quality** – Delivering with consistency, care, and precision

### Our Portfolio

Saral Apps has successfully delivered digital solutions for various educational institutions and businesses in Nepal, including:
- **Gurukul Commerce Academy** – Premier CA education platform
- **Lex Nepal** – Nepal's leading legal education platform
- **TS Gurukul** – Loksewa exam preparation app
- **Bright Academy** – Modern educational institution platform
- **Kirtipur Valley Institute** – High-quality training platform
- **Saral Discount** – Employee benefits platform

We believe in empowering developers and businesses with tools that don't compromise on performance or user experience. Our libraries and solutions are production-tested, actively maintained, and designed with real-world use cases in mind.

**Connect with us:**
- 📍 Location: New Baneshwor, Kathmandu, Nepal
- 📞 Phone: +977 9851275536
- 📧 Email: info@saralapps.com
- 🌐 Website: [https://saralapps.com](https://saralapps.com)

## 📦 Installation

Add the dependency to your `build.gradle.kts` file:

```kotlin
kotlin {
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation("com.saralapps:composemultiplatformwebview:0.0.7")
            }
        }
    }
}
```

### Platform Requirements

**Windows (x64):**
- Windows 10 version 1803 or later
- Microsoft Edge WebView2 Runtime (automatically installed on Windows 11, downloadable for Windows 10)

**macOS (Apple Silicon):**
- WKWebView is included with macOS system frameworks

## 🚀 Quick Start

### Basic WebView (Simple Approach)

```kotlin
@Composable
fun App() {
    PlatformWebView(
        url = "https://example.com",
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
            println("URL changed to: $newUrl")
        }
    )
}
```

### WebView with Navigation Control

```kotlin
@Composable
fun WebViewWithNavigationControl() {
    var currentUrl by remember { mutableStateOf("https://example.com") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // URL input
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = currentUrl,
                onValueChange = { currentUrl = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter URL") }
            )
            
            Button(onClick = { /* Load new URL */ }) {
                Text("Go")
            }
        }
        
        // WebView
        PlatformWebView(
            url = currentUrl,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            javaScriptEnabled = true,
            onUrlChanged = { newUrl ->
                currentUrl = newUrl
            },
            onNavigating = { url ->
                println("Navigating to: $url")
                // Return true to allow navigation, false to block
                true
            }
        )
    }
}
```

### Handling WebView Availability

```kotlin
@Composable
fun WebViewWithAvailabilityCheck() {
    PlatformWebView(
        url = "https://example.com",
        modifier = Modifier.fillMaxSize(),
        onUnavailable = { availability ->
            // Handle WebView unavailability
            when (availability) {
                is WebViewAvailability.NotInstalled -> {
                    Text("WebView is not installed on this system")
                }
                is WebViewAvailability.Error -> {
                    Text("Error initializing WebView: ${availability.message}")
                }
                else -> {
                    Text("WebView unavailable")
                }
            }
        }
    )
}
```

## 🎯 Core Components

### PlatformWebViewState

The `PlatformWebViewState` manages the internal state of the WebView. You can create it using the `rememberPlatformWebViewState` composable:

```kotlin
val webViewState = rememberPlatformWebViewState(
    url = "https://example.com",
    javaScriptEnabled = true,
    allowsFileAccess = true,
    onNavigating = { url ->
        println("Navigating to: $url")
        // Return true to allow navigation, false to block
        true
    }
)
```

**Parameters:**
- `url: String?` - The initial URL to load (optional)
- `javaScriptEnabled: Boolean` - Enable or disable JavaScript execution (default: true)
- `allowsFileAccess: Boolean` - Allow or deny access to local files (default: true)
- `onNavigating: ((String) -> Boolean)?` - Callback invoked before navigation, return false to block

### PlatformWebView Composable

The library provides two variants of the `PlatformWebView` composable:

#### 1. State-based Variant (Recommended for complex scenarios)

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

**Usage Example:**
```kotlin
val state = rememberPlatformWebViewState(url = "https://example.com")

PlatformWebView(
    state = state,
    modifier = Modifier.fillMaxSize(),
    onUrlChanged = { newUrl -> println("URL: $newUrl") },
    onCreated = { println("WebView created") },
    onDisposed = { println("WebView disposed") }
)
```

#### 2. Direct URL Variant (Simple use cases)

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

**Usage Example:**
```kotlin
PlatformWebView(
    url = "https://github.com",
    modifier = Modifier.fillMaxSize(),
    javaScriptEnabled = true,
    allowsFileAccess = false,
    onNavigating = { url ->
        // Block external navigation
        url.startsWith("https://github.com")
    }
)
```

### Common Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `modifier` | Modifier | Compose modifier for layout and styling |
| `placeholderColor` | Color | Background color shown while WebView loads |
| `onUrlChanged` | `((String) -> Unit)?` | Callback when the URL changes |
| `onNavigating` | `((String) -> Boolean)?` | Callback before navigation; return false to block |
| `onCreated` | `(() -> Unit)?` | Callback when WebView is successfully created |
| `onDisposed` | `(() -> Unit)?` | Callback when WebView is disposed |
| `onUnavailable` | `@Composable ((WebViewAvailability) -> Unit)?` | Composable to show when WebView is unavailable |

### WebViewAvailability

The `WebViewAvailability` sealed class indicates the availability status of the native WebView:

```kotlin
sealed class WebViewAvailability {
    object Available : WebViewAvailability()
    object NotInstalled : WebViewAvailability()
    data class Error(val message: String) : WebViewAvailability()
}
```

## ⚙️ Configuration

### Basic Configuration

Configure the WebView behavior using the available parameters:

```kotlin
PlatformWebView(
    url = "https://example.com",
    modifier = Modifier.fillMaxSize(),
    javaScriptEnabled = true,  // Enable JavaScript
    allowsFileAccess = false,  // Disable file access for security
    placeholderColor = Color.LightGray,
    onUrlChanged = { newUrl ->
        println("Current URL: $newUrl")
    }
)
```

### Navigation Control

Control navigation behavior using the `onNavigating` callback:

```kotlin
val webViewState = rememberPlatformWebViewState(
    url = "https://example.com",
    onNavigating = { url ->
        when {
            // Block navigation to external domains
            !url.startsWith("https://example.com") -> {
                println("Blocked navigation to: $url")
                false
            }
            // Block specific patterns
            url.contains("ads") || url.contains("tracker") -> {
                println("Blocked: $url")
                false
            }
            // Allow all other navigation
            else -> true
        }
    }
)

PlatformWebView(
    state = webViewState,
    modifier = Modifier.fillMaxSize()
)
```

### Lifecycle Management

Handle WebView lifecycle events:

```kotlin
var isWebViewReady by remember { mutableStateOf(false) }

PlatformWebView(
    url = "https://example.com",
    modifier = Modifier.fillMaxSize(),
    onCreated = {
        println("WebView created and ready")
        isWebViewReady = true
    },
    onDisposed = {
        println("WebView disposed")
        isWebViewReady = false
    }
)
```

### Error Handling

Handle cases where WebView is unavailable:

```kotlin
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Text(
                            "WebView Not Available",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            "Please install WebView2 Runtime (Windows) or update macOS",
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = {
                            // Open download page
                        }) {
                            Text("Download WebView2")
                        }
                    }
                }
                is WebViewAvailability.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null)
                        Text("Error: ${availability.message}")
                    }
                }
                else -> {
                    Text("WebView unavailable")
                }
            }
        }
    }
)
```

## 🔒 Security Considerations

### JavaScript Control

JavaScript can be enabled or disabled based on your security requirements:

```kotlin
// Disable JavaScript for untrusted content
PlatformWebView(
    url = "https://untrusted-site.com",
    javaScriptEnabled = false,
    modifier = Modifier.fillMaxSize()
)

// Enable JavaScript for trusted content
PlatformWebView(
    url = "https://trusted-app.com",
    javaScriptEnabled = true,
    modifier = Modifier.fillMaxSize()
)
```

### File Access Control

Control access to local files:

```kotlin
// Deny file access for security
val webViewState = rememberPlatformWebViewState(
    url = "https://example.com",
    allowsFileAccess = false  // Recommended for web content
)

// Allow file access only when needed
val localWebViewState = rememberPlatformWebViewState(
    url = "file:///path/to/local/content.html",
    allowsFileAccess = true  // Required for local files
)
```

### Navigation Filtering

Implement URL filtering to prevent malicious navigation:

```kotlin
val webViewState = rememberPlatformWebViewState(
    url = "https://myapp.com",
    onNavigating = { url ->
        val allowedDomains = listOf("myapp.com", "cdn.myapp.com", "api.myapp.com")
        val uri = URI(url)
        
        // Allow only whitelisted domains
        allowedDomains.any { domain ->
            uri.host?.endsWith(domain) == true
        }
    }
)
```

## 🔧 Advanced Usage

### Dynamic URL Loading

```kotlin
@Composable
fun DynamicWebView() {
    var currentUrl by remember { mutableStateOf("https://example.com") }
    val webViewState = rememberPlatformWebViewState(
        url = currentUrl,
        onNavigating = { url ->
            currentUrl = url
            true
        }
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // URL bar
        OutlinedTextField(
            value = currentUrl,
            onValueChange = { newUrl ->
                currentUrl = newUrl
                // Note: WebView state needs to be recreated for new URL
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            label = { Text("URL") },
            singleLine = true
        )
        
        PlatformWebView(
            state = webViewState,
            modifier = Modifier.weight(1f)
        )
    }
}
```

### Multi-WebView Management

```kotlin
@Composable
fun TabbedBrowser() {
    var selectedTab by remember { mutableStateOf(0) }
    val urls = remember {
        listOf(
            "https://github.com",
            "https://stackoverflow.com",
            "https://kotlinlang.org"
        )
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            urls.forEachIndexed { index, url ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(url.substringAfter("://").substringBefore("/")) }
                )
            }
        }
        
        PlatformWebView(
            url = urls[selectedTab],
            modifier = Modifier.weight(1f),
            onUrlChanged = { newUrl ->
                println("Tab $selectedTab: $newUrl")
            }
        )
    }
}
```

### Conditional Content Loading

```kotlin
@Composable
fun ConditionalWebView(userIsLoggedIn: Boolean) {
    if (userIsLoggedIn) {
        PlatformWebView(
            url = "https://myapp.com/dashboard",
            javaScriptEnabled = true,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        PlatformWebView(
            url = "https://myapp.com/login",
            javaScriptEnabled = true,
            modifier = Modifier.fillMaxSize(),
            onNavigating = { url ->
                // Redirect to dashboard after successful login
                if (url.contains("/dashboard")) {
                    // Handle login success
                }
                true
            }
        )
    }
}
```

## 🌐 Web Standards Support

The native WebView implementation provides full support for modern web standards through the underlying platform engines:

| Feature | Windows (WebView2) | macOS (WKWebView) |
|---------|-------------------|-------------------|
| HTML5 | ✅ | ✅ |
| CSS3 | ✅ | ✅ |
| ES6+ JavaScript | ✅ | ✅ |
| WebGL | ✅ | ✅ |
| WebAssembly | ✅ | ✅ |
| WebSockets | ✅ | ✅ |
| Service Workers | ✅ | ✅ |
| Local Storage | ✅ | ✅ |
| IndexedDB | ✅ | ✅ |
| Geolocation* | ✅ | ✅ |
| WebRTC | ✅ | ✅ |
| Media Capture | ✅ | ✅ |
| Canvas API | ✅ | ✅ |
| Web Audio API | ✅ | ✅ |
| Web Workers | ✅ | ✅ |

*Geolocation and other permission-based features require appropriate system permissions.

## 📊 Performance Comparison

| Metric | Native WebView | Embedded Chromium |
|--------|---------------|------------------|
| App Size Increase | ~2MB | ~100MB+ |
| Memory Footprint | Low | High |
| Startup Time | Fast | Slow |
| System Integration | Native | Isolated |
| Security Updates | Automatic | Manual |

## 🛠️ Troubleshooting

### Windows WebView2 Not Found

If you encounter an error about WebView2 not being installed:

1. Download the [WebView2 Runtime](https://developer.microsoft.com/microsoft-edge/webview2/)
2. Install the Evergreen Standalone Installer
3. Restart your application

### macOS WKWebView Issues

Ensure your macOS version is 10.15 or later:

```kotlin
fun checkWKWebViewSupport(): Boolean {
    val osVersion = System.getProperty("os.version")
    // Implement version check logic
    return true
}
```

### JNA Loading Issues

If you encounter JNA library loading errors:

```kotlin
dependencies {
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
}
```

## 🧪 Testing

### Unit Testing WebView Integration

```kotlin
@Test
fun testWebViewStateCreation() = runComposeUiTest {
    lateinit var state: PlatformWebViewState
    
    setContent {
        state = rememberPlatformWebViewState(
            url = "https://example.com",
            javaScriptEnabled = true
        )
    }
    
    // Verify state was created
    assertNotNull(state)
}
```

### Testing Navigation Blocking

```kotlin
@Test
fun testNavigationBlocking() = runComposeUiTest {
    var navigationAttempted = false
    var navigationBlocked = false
    
    setContent {
        val state = rememberPlatformWebViewState(
            url = "https://example.com",
            onNavigating = { url ->
                navigationAttempted = true
                if (url.contains("blocked")) {
                    navigationBlocked = true
                    false
                } else {
                    true
                }
            }
        )
        
        PlatformWebView(state = state)
    }
    
    waitUntil(timeoutMillis = 5000) {
        navigationAttempted
    }
}
```

### Testing Lifecycle Callbacks

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
    
    waitUntil { created }
    
    // Dispose the composable
    setContent { }
    
    waitUntil { disposed }
}
```

## 📚 Additional Resources

- [Windows WebView2 Documentation](https://learn.microsoft.com/en-us/microsoft-edge/webview2/)
- [macOS WKWebView Documentation](https://developer.apple.com/documentation/webkit/wkwebview)
- [JNA Documentation](https://github.com/java-native-access/jna)
- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)

## 🗺️ Roadmap

### Planned Features
- ✅ Windows x64 support
- ✅ macOS Apple Silicon & Intel support
- 🔄 Advanced developer tools integration
- 🔄 Custom protocol handlers
- 🔄 Enhanced debugging capabilities
- ⏳ Linux support (help wanted!)

## 🤝 Contributing

We welcome contributions to make Compose Native WebView even better! Whether it's bug reports, feature requests, documentation improvements, or code contributions, your help is appreciated.

### 🐧 Linux Support - We Need Your Help!

**We're looking for contributors to help implement Linux support!**

The library currently supports Windows and macOS through native WebView integration. We'd love to extend support to Linux using one of the following approaches:

**Potential Linux WebView Options:**
- GTK WebKitGTK
- Qt WebEngine
- Electron (as a fallback)

**What We Need:**
- Developers familiar with Linux desktop development
- Experience with JNA/JNI and native library integration
- Knowledge of GTK, Qt, or other Linux GUI frameworks
- Testing across different Linux distributions

**How to Contribute:**

1. **Fork the repository**
   ```bash
   git clone https://github.com/saral-apps/composemultiplatformwebview
   cd composemultiplatformwebview
   ```

2. **Create a feature branch**
   ```bash
   git checkout -b feature/linux-support
   ```

3. **Make your changes** and test thoroughly

4. **Submit a Pull Request** with:
   - Clear description of changes
   - Platform-specific implementation details
   - Test results on different Linux distributions

5. **Report Issues** at [GitHub Issues](https://github.com/saral-apps/composemultiplatformwebview/issues)

### Contribution Guidelines

- Follow Kotlin coding conventions
- Write clear commit messages
- Include tests for new features
- Update documentation as needed
- Ensure cross-platform compatibility

For Linux support specifically, please reach out to us at **info@saralapps.com** or open a discussion on GitHub. We're happy to provide guidance, technical details, and support throughout the development process.

## 💬 Community & Support

- 🐛 [Report Issues](https://github.com/saral-apps/ComposeMultiplatformWebView/issues)
- 💡 [Feature Requests](https://github.com/saral-apps/composemultiplatformwebview/discussions)
- 📧 Email: info@saralapps.com
- 📞 Phone: +977 9851275536
- 🌐 Website: [https://saralapps.com](https://saralapps.com)
- 📍 Address: New Baneshwor, Kathmandu, Nepal

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

---

## 🌟 If you find this library useful, please star ⭐ the repository!

**Built with ❤️ by [Saral Apps Pvt. Ltd.](https://saralapps.com) in Kathmandu, Nepal**

*Crafting digital experiences that elevate brands and solve real-world problems*