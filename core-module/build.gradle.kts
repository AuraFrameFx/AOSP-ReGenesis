plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.ksp)
    // Apply the OpenAPI Generator plugin
    id("org.openapi.generator") version "7.15.0" // Use the latest version
}

android {
    namespace = "dev.aurakai.auraframefx.core" // The correct namespace for this module
    compileSdk = 36

    defaultConfig {
        minSdk = 34
        multiDexEnabled = true
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    buildFeatures {
        aidl = true
        renderScript = true
        shaders = false
    }

    // Add generated sources to the build configuration
    sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
}

// OpenAPI Generator configuration
tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate") {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/api-spec/aura-framefx-api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").asFile.absolutePath)
    apiPackage.set("dev.aurakai.auraframefx.api.client.apis")
    modelPackage.set("dev.aurakai.auraframefx.api.client.models")
    invokerPackage.set("dev.aurakai.auraframefx.api.client.infrastructure")
    configOptions.set(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "serializationLibrary" to "kotlinx_serialization"
        )
    )
}

// Dependencies block
dependencies {
    // AndroidX Core
    implementation(libs.bundles.androidx.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Networking
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)

    // YukiHook API 1.3.0+ with KavaRef
    api(libs.yukihook.api)
    api(libs.kavaref.core)
    api(libs.kavaref.extension)
    ksp(libs.yukihook.ksp)

    // Xposed API (compile only, provided by the framework at runtime)
    compileOnly(libs.xposed.api)

    // Firebase (Dependencies are here, the plugin goes in the application module)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Logging
    api(libs.timber)

    // Dagger Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Status task
tasks.register("coreModuleStatus") {
    group = "aegenesis"
    description = "Show core module status"
    doLast {
        println("🏗️  CORE MODULE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${project.namespace}") // Use project.namespace for modern Gradle
        println("📱 SDK: ${project.android.compileSdk}") // Use project.android.compileSdk
        println("🎨 Compose: ❌ Removed")
        println(
            "🔗 API Generation: ${
                if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()
                ) "✅ Enabled" else "❌ No spec"
            }"
        )
        println("✨ Status: Core Foundation Ready with Convention Plugins!")
    }
}