// Apply plugins with explicit versions
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.openapi.generator)
    // YukiHook plugin
}

// Apply YukiHook plugin after the Android plugin is applied
// Removed, as the plugin is now applied directly

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
        // Required for YukiHook
        aidl = true
        renderScript = true
        shaders = true
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
    implementation(libs.converter.scalars) // TODO: Move to version catalog
    
    // Date/Time
    implementation(libs.kotlinx.datetime)
    
    // OAuth
    implementation(libs.apache.oltu.oauth2.common)
    implementation(libs.apache.oltu.oauth2.client)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Xposed Framework
    compileOnly(files("${project.rootDir}/Libs/api-82.jar"))
    compileOnly(files("${project.rootDir}/Libs/api-82-sources.jar"))
    
    // YukiHook API
    api(libs.yukihook.api)
    ksp(libs.com.highcapable.yukihookapi.ksp)
    
    // Logging
    api(libs.timber)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.ktx.coroutines.test)
    
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    kspTest(libs.hilt.compiler)
}

// YukiHook configuration
yukihook {
    // Enable the API for the current build type
    isEnable = true
    
    // Load the API in the application class
    // Replace with your actual application class if different
    // loadOnApp = true
    
    // Enable debug mode
    isDebug = true
    
    // Configure the module name (optional)
    // name = "CoreModule"
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
