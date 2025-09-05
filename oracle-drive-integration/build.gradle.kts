plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx.oracle.drive.integration"
    compileSdk = 36

    defaultConfig {
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    kotlin {
        jvmToolchain(24)
    }
}

// KSP configuration for this module
ksp {
    arg("kotlin.languageVersion", "2.2")
    arg("kotlin.apiVersion", "2.2")
    arg("kotlin.jvmTarget", "24")
}

dependencies {
    // Core dependencies
    api(project(":core-module"))
    implementation(project(":secure-comm"))
    implementation(libs.bundles.androidx.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Networking for Oracle Drive integration
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // YukiHook API 1.3.0+ with KavaRef
    api(libs.yukihook.api)
    ksp(libs.yukihook.ksp)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    // Xposed API (compile only)
    compileOnly(libs.xposed.api)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)

    // Logging
    api(libs.timber)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
