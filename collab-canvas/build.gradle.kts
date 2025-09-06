// Apply plugins (versions via version catalog)
plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.yukihook) // replaces inline version
}

android {
    namespace = "dev.aurakai.auraframefx.collabcanvas"
    compileSdk = 36

    defaultConfig { minSdk = 34 }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Unified Java toolchain & modern Kotlin config
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }

    kotlin {
        jvmToolchain(24)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers",
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all"
            )
        }
    }

    // Rely on BOM + plugin for compose compiler; remove hardcoded composeOptions
    // composeOptions { kotlinCompilerExtensionVersion = "<managed>" }
}

dependencies {
    // Core AndroidX / Lifecycle
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

    // UI / Utils
    implementation(libs.coil.compose)
    implementation(libs.timber)
    implementation(fileTree("../Libs") { include("*.jar") })
    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)

    // YukiHook / Xposed
    implementation(libs.yukihook.core)
    ksp(libs.yukihook.ksp)
    implementation(libs.yukihook.prefs)
    compileOnly(libs.xposed.api)
}

// YukiHook configuration
yukihook {
    isDebug = true
    isEnableLog = true
    isEnableDebugLog = true
    isEnableDebugLogWithTag = true
}

tasks.register("collabStatus") {
    group = "aegenesis"
    doLast { println("COLLAB CANVAS - Ready (Java 24 toolchain, unified).") }
}
