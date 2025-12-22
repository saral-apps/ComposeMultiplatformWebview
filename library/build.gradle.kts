import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.saralapps"
version = "0.0.2"

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.saralapps.composemultiplatformwebview"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "library"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }


        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.jna.platform)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "composemultiplatformwebview", version.toString())

    pom {
        name = "Compose Multiplatform WebView 1"
        description = "A WebView component for Compose Multiplatform supporting Desktop"
        inceptionYear = "2025"
        url = "https://github.com/saral-apps/composemultiplatformwebview/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "saral-apps"
                name = "Saral Apps"
                url = "https://github.com/saral-apps/"
            }
        }
        scm {
            url = "https://github.com/saral-apps/composemultiplatformwebview/"
            connection = "scm:git:git://github.com/saral-apps/composemultiplatformwebview.git"
            developerConnection = "scm:git:ssh://git@github.com/saral-apps/composemultiplatformwebview.git"
        }
    }
}
