// Apply plugins without version to avoid conflicts
// Versions are managed in the root build.gradle.kts and version catalog
plugins {
    id("com.android.library")  // Apply without version
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    // YukiHook API KSP plugin
    id("com.highcapable.yukihookapi") version "2.1.1"
}
android {
    namespace = "dev.aurakai.auraframefx.collabcanvas"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 34
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Kotlin compiler options for Compose
    kotlin {
        jvmToolchain(17)

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers",
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
        // Optionally, set composeOptions if not managed by version catalog
        // composeOptions { kotlinCompilerExtensionVersion = "1.5.0" }
    }

    dependencies {
        // Core AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.compose)

        // Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.material.icons.extended)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.navigation.compose)

        // Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)

        // Coroutines
        implementation(libs.ktx.coroutines.core)
        implementation(libs.ktx.coroutines.android)

        // Network
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.kotlinx.serialization)
        implementation(libs.okhttp3.logging.interceptor)

        // Room
        implementation(libs.room.runtime)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)

        // Firebase
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.analytics.ktx)
        implementation(libs.firebase.crashlytics.ktx)

        // UI
        implementation(libs.coil.compose)
        implementation(libs.timber)
        implementation(fileTree("../Libs") { include("*.jar") })
        implementation(libs.gson)

        // Core library desugaring

        // Testing
        testImplementation(libs.junit)
        testImplementation(libs.mockk)

        // Android Testing
        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.androidx.test.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui)

        // Debug
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.tooling.preview)
        
        // YukiHook API
        implementation(libs.yukihook.core)
        ksp(libs.yukihook.ksp)
        implementation(libs.yukihook.prefs)
        
        // Xposed API (compile only, provided by the framework at runtime)
        compileOnly(libs.xposed.api)
    }
    
    // Configure YukiHook API
    yukihook {
        // Enable debug mode in debug build
        isDebug = true
        
        // Enable YukiHook logger
        isEnableLog = true
        
        // Enable YukiHook API debug log
        isEnableDebugLog = true
        
        // Enable YukiHook API debug log with tag
        isEnableDebugLogWithTag = true
    }
    tasks.register("collabStatus") {
        group = "aegenesis"
        doLast {
            println("🎨 COLLAB CANVAS - Ready!")
        }
    }


