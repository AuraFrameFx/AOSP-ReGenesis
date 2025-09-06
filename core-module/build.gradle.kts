// Apply plugins with explicit versions
plugins {
    // Replaced explicit versions with catalog aliases for consistency
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx.core"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 34
        
        // Required for YukiHook
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }
    
    buildFeatures {
        // Only enable what’s required; remove renderScript/shaders
        aidl = true
        buildConfig = true
    }
    
    // Add Java toolchain 24
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin {
        jvmToolchain(24)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers"
            )
            progressiveMode.set(true)
        }
    }
    
    sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
}

// OpenAPI generation task
tasks.named("preBuild").configure {
    dependsOn(tasks.named<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate"))
}

dependencies {
    // YukiHook API

    
    // AndroidX
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // Kotlin
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktx.coroutines.core)
    implementation(libs.ktx.coroutines.android)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp3.logging.interceptor)
    // Networking scalars converter via catalog alias (updated)
    implementation(libs.retrofit.converter.scalars)

    // Date/Time
    implementation(libs.kotlinx.datetime)
    
    // OAuth
    implementation(libs.apache.oltu.oauth2.common)
    implementation(libs.apache.oltu.oauth2.client)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Xposed Framework
    compileOnly(libs.xposed.api)

    // YukiHook API
    implementation(libs.yukihook.core)
    ksp(libs.yukihook.ksp)
    implementation(libs.yukihook.prefs)

    // Logging
    api(libs.timber)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.ktx.coroutines.test)
    
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.mockk.android)
    kspTest(libs.hilt.compiler)
}

// Status task
tasks.register("coreModuleStatus") {
    group = "aegenesis"
    description = "Show core module status"
    
    doLast {
        println("🏗️  CORE MODULE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${android.namespace}")
        println("📱 SDK: ${android.compileSdk}")
        println("🎨 Compose: ❌ Removed")
        println("🔗 API Generation: ${if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) "✅ Enabled" else "❌ No spec"}")
        println("✨ Status: Core Foundation Ready with Convention Plugins!")
    }
}
