package com.saralapps.common.desktop.webview.windows

import java.io.File
import java.io.FileOutputStream

/**
 * Utility to load Windows WebView2 DLLs from bundled resources.
 *
 * This extracts NativeWebView.dll and WebView2Loader.dll from the
 * resources/win folder to a temp directory and configures JNA to load them.
 *
 * Call [ensureLoaded] before using any WebView functionality.
 */
object WindowsDllLoader {

    private var loaded = false
    private var loadError: String? = null
    private var dllDirectory: File? = null

    /**
     * Check if we're running on Windows.
     */
    val isWindows: Boolean by lazy {
        System.getProperty("os.name").lowercase().contains("win")
    }

    /**
     * Check if DLLs are loaded successfully.
     */
    val isLoaded: Boolean
        get() = loaded

    /**
     * Get the error message if loading failed.
     */
    val error: String?
        get() = loadError

    /**
     * Get the directory where DLLs are extracted.
     */
    val dllPath: File?
        get() = dllDirectory

    /**
     * Ensures DLLs are extracted and loaded.
     * Safe to call multiple times - only loads once.
     *
     * @return true if DLLs are ready, false if loading failed
     */
    @Synchronized
    fun ensureLoaded(): Boolean {
        if (loaded) return true
        if (!isWindows) {
            loadError = "Not running on Windows"
            return false
        }

        return try {
            extractAndLoadDlls()
            loaded = true
            true
        } catch (e: Exception) {
            loadError = e.message ?: "Unknown error"
            println("[WindowsDllLoader] Failed to load DLLs: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun extractAndLoadDlls() {
        // Create a directory for our DLLs
        // Use app-specific folder to avoid conflicts
        val tempBase = File(System.getProperty("java.io.tmpdir"))
        val appName = "SaralpAppsWebView2"
        val dllDir = File(tempBase, appName)

        if (!dllDir.exists()) {
            dllDir.mkdirs()
        }

        dllDirectory = dllDir

        // List of DLLs to extract (order matters - WebView2Loader must be available when NativeWebView loads)
        val dlls = listOf("WebView2Loader.dll", "NativeWebView.dll")

        for (dllName in dlls) {
            extractDll(dllName, dllDir)
        }

        // Set JNA library path to include our DLL directory
        val currentPath = System.getProperty("jna.library.path") ?: ""
        val newPath = if (currentPath.isEmpty()) {
            dllDir.absolutePath
        } else {
            "${dllDir.absolutePath};$currentPath"
        }
        System.setProperty("jna.library.path", newPath)

        // Also add to java.library.path for good measure
        val javaLibPath = System.getProperty("java.library.path") ?: ""
        val newJavaLibPath = if (javaLibPath.isEmpty()) {
            dllDir.absolutePath
        } else {
            "${dllDir.absolutePath};$javaLibPath"
        }
        System.setProperty("java.library.path", newJavaLibPath)

        println("[WindowsDllLoader] DLLs extracted to: ${dllDir.absolutePath}")
        println("[WindowsDllLoader] JNA library path: $newPath")
    }

    private fun extractDll(dllName: String, targetDir: File) {
        val targetFile = File(targetDir, dllName)

        // Try multiple resource paths
        val resourcePaths = listOf(
            "win/$dllName",           // resources/win/
            "windows/$dllName",       // resources/windows/
            dllName,                   // resources/
            "/win/$dllName",          // absolute path
            "/windows/$dllName",
            "/$dllName"
        )

        var inputStream: java.io.InputStream? = null
        var foundPath: String? = null

        for (path in resourcePaths) {
            inputStream = javaClass.classLoader?.getResourceAsStream(path)
                ?: javaClass.getResourceAsStream(path)
                        ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(path)

            if (inputStream != null) {
                foundPath = path
                break
            }
        }

        if (inputStream == null) {
            throw RuntimeException(
                "Could not find $dllName in resources. Tried paths: $resourcePaths\n" +
                        "Make sure the DLL is in desktopMain/resources/win/"
            )
        }

        println("[WindowsDllLoader] Found $dllName at: $foundPath")

        // Check if we need to extract (file doesn't exist or is different size)
        val needsExtract = !targetFile.exists() ||
                targetFile.length() == 0L ||
                shouldReplace(inputStream, targetFile)

        if (needsExtract) {
            // Re-open stream if we consumed it checking size
            val extractStream = javaClass.classLoader?.getResourceAsStream(foundPath)
                ?: javaClass.getResourceAsStream(foundPath)
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(foundPath)
                ?: throw RuntimeException("Could not reopen stream for $dllName")

            println("[WindowsDllLoader] Extracting $dllName to ${targetFile.absolutePath}")

            try {
                // Delete old file if exists (might be locked)
                if (targetFile.exists()) {
                    targetFile.delete()
                }

                FileOutputStream(targetFile).use { out ->
                    extractStream.copyTo(out)
                }
            } finally {
                extractStream.close()
            }

            println("[WindowsDllLoader] Extracted $dllName (${targetFile.length()} bytes)")
        } else {
            println("[WindowsDllLoader] $dllName already exists (${targetFile.length()} bytes)")
            inputStream.close()
        }
    }

    private fun shouldReplace(inputStream: java.io.InputStream, existingFile: File): Boolean {
        // Simple check - if sizes differ, replace
        // Note: This consumes the stream, so caller needs to reopen
        return try {
            val resourceSize = inputStream.available()
            val fileSize = existingFile.length()
            resourceSize > 0 && resourceSize.toLong() != fileSize
        } catch (e: Exception) {
            true // If we can't check, replace to be safe
        }
    }

    /**
     * Cleanup extracted DLLs.
     * Call this when app exits if you want to clean up temp files.
     * Usually not necessary as the OS will clean temp eventually.
     */
    fun cleanup() {
        dllDirectory?.let { dir ->
            try {
                dir.listFiles()?.forEach { it.delete() }
                dir.delete()
                println("[WindowsDllLoader] Cleaned up DLL directory")
            } catch (e: Exception) {
                // DLLs might be in use, ignore
            }
        }
    }
}