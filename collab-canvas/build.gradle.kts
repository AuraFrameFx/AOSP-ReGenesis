// Apply plugins without version to avoid conflicts
// Versions are managed in the root build.gradle.kts and version catalog
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.aurakai.auraframefx.collabcanvas"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 34
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Kotlin compiler options for Compose
    kotlin {
        jvmToolchain(21)

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
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
