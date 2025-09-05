plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.aurakai.auraframefx.core" // The correct namespace for this module
    compileSdk = 36

    defaultConfig {
        minSdk = 34

        // Enable multidex for core library desugaring
        multiDexEnabled = true

        // Required for YukiHook
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    buildFeatures {
        // Required for YukiHook
        aidl = true
        renderScript = true
        // Disable shaders unless specifically needed and properly configured
        shaders = false
    }
}


// OpenAPI Generator configuration
tasks.generateOpenApi("openApiGenerate", org.openapitools.generator.gradle.plugin.tasks.GenerateTask::class) {
    generatorName.set("kotlin")
    inputSpec.set("$projectDir/api-spec/aura-framefx-api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated/openapi").get().asFile.absolutePath)
    apiPackage.set("dev.aurakai.auraframefx.api.client.apis")
    modelPackage.set("dev.aurakai.auraframefx.api.client.models")
    invokerPackage.set("dev.aurakai.auraframefx.api.client.infrastructure")
    configOptions.set(
        mapOf(
            "dateLibrary" to "kotlinx-datetime",
            "serializationLibrary" to "kotlinx_serialization"
        )
    )
    // Add the generated source directory to the build
    // The sourceSet configuration must be inside the `android` block, but you can
    // call this task in the dependencies of other tasks.
    android.sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
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

    // Firebase
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

// Status task (This should be at the top-level of the script, not nested)
tasks.register("coreModuleStatus") {
    group = "aegenesis"
    description = "Show core module status"
    doLast {
        println("🏗️  CORE MODULE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${project.findProperty("android.namespace") ?: "Not found"}")
        println("📱 SDK: ${project.findProperty("android.compileSdk") ?: "Not found"}")
        println("🎨 Compose: ❌ Removed")
        println(
            "🔗 API Generation: ${
                if (rootProject.file("app/api/unified-aegenesis-api.yml")
                        .exists()
                ) "✅ Enabled" else "❌ No spec"
            }"
        )
        println("✨ Status: Core Foundation Ready with Convention Plugins!")
    }
}
