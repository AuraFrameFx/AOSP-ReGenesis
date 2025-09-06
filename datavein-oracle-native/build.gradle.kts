plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // Assuming a convention plugin for Hilt
    alias(libs.plugins.ksp)
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

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(24))
        }
    }
    kotlin {
        jvmToolchain(24)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
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

    // MockK for mocking in tests
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
}