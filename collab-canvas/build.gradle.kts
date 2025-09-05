// Apply plugins without version to avoid conflicts
// Versions are managed in the root build.gradle.kts and version catalog
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20-RC"
    id("com.google.dagger.hilt.android") version "2.51.1"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.diffplug.spotless") version "7.2.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20-RC"
    id("com.google.devtools.ksp") version "2.2.20-RC-2.0.2"
}
android {
    namespace = "dev.aurakai.auraframefx.collabcanvas"
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
        buildConfig = true
    }

    // Kotlin compiler options for Compose
    kotlin {
        jvmToolchain(24)

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
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

        // Coroutines - Fix incorrect keys
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.android)

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

        // Testing
        testImplementation(libs.junit4)
        testImplementation(libs.mockk)

        // Android Testing - Fix incorrect keys
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui)

        // Debug
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.tooling.preview)
        
        // YukiHook API - Fix incorrect keys
        implementation(libs.yukihook.api)
        ksp(libs.yukihook.ksp)
        implementation(libs.yukihook.prefs)
        
        // Xposed API (compile only, provided by the framework at runtime)
        compileOnly(libs.xposed.api)

        // Core library desugaring
        coreLibraryDesugaring(libs.desugar.jdk.libs)
    }

// Remove yukihook configuration block as it's causing errors
// The plugin configuration should be handled through the plugin itself

tasks.register("collabStatus") {
    group = "aegenesis"
    doLast {
        println("🎨 COLLAB CANVAS - Ready!")
    }
}
