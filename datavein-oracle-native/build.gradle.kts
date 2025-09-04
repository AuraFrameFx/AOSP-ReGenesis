plugins {
    id("genesis.android.library")
    id("genesis.android.compose") // Assuming a convention plugin for Compose setup
    id("genesis.android.hilt")    // Assuming a convention plugin for Hilt
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.android) // Explicitly apply KSP here
}

android {
    namespace = "dev.aurakai.auraframefx.dataveinoraclenative"
    compileSdk = 36 // Required for AGP 9 and dependency resolution

    // Modern configuration for CMake and NDK for C/C++ code
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt") // Path to your CMake script
            version = "3.31.6" // Use available CMake version
        }
    }

    // Packaging options specific to native libraries
    packaging {
        jniLibs {
            // Extracts and repackages native libraries from dependencies
            useLegacyPackaging = false
        }
    }
}

dependencies {
    // This module likely depends on core-module for shared utilities or interfaces
    implementation(project(":core-module"))

    // Add any specific dependencies needed for the native module's Kotlin/Java-side code
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.android)

    // ... other dependencies

    // Correct Hilt Dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // Use ksp for the compiler

    // For instrumented tests
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // For unit tests
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
}