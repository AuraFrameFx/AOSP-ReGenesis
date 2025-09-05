// ==== GENESIS PROTOCOL - SANDBOX UI ====
plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
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
    
    // Kotlin compiler options with modern DSL
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(24))
        }
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers"
            )
        }
    }
}

dependencies {
    // Core AndroidX dependencies
    api(project(":core-module"))
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Coroutines & Utilities
    implementation(libs.bundles.coroutines)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    
    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    
    // Android Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

tasks.register("sandboxStatus") { 
    group = "aegenesis"
    doLast { 
        println("🧪 SANDBOX UI - Ready!") 
    } 
}
