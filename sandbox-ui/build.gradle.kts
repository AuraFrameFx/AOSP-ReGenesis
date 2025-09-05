plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.aurakai.auraframefx.sandboxui"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 34
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    buildFeatures {
        compose = true
    }

    kotlin {
        jvmToolchain(24)
    }
}

dependencies {
    // Core dependencies
    api(project(":core-module"))
    implementation(libs.bundles.androidx.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Networking
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

    // DataStore
    implementation(libs.datastore.preferences)

    // UI
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
