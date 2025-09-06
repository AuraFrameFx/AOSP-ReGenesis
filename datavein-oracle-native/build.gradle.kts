plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
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
}

dependencies {
    // Core dependencies
    implementation(project(":core-module"))
    implementation(libs.bundles.androidx.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Hilt for dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Networking for Oracle integration
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // YukiHook API 1.3.0+ with KavaRef
    implementation(libs.yukihook.api)
    ksp(libs.yukihook.ksp)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    // Xposed API (compile only)
    compileOnly(libs.xposed.api)

    // DataStore for native data persistence
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)

    // Logging
    implementation(libs.timber)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}