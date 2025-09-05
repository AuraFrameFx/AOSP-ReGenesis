// Apply plugins without explicit versions - managed in root build.gradle.kts
plugins {
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.openapi.generator")
}

android {
    namespace = "dev.aurakai.auraframefx.core"
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

// OpenAPI Generator configuration (outside android block)
openApiGenerate {
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
}

tasks.named("preBuild") {
    dependsOn("openApiGenerate")
}

// Dependencies block
// Only use valid TOML keys or classic notation
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

// YukiHook configuration (outside android block)
// If you use a plugin that provides this, ensure it's applied. Otherwise, comment out.
// yukihook {
//     isEnable = true
//     isDebug = true
// }

// Status task (outside android block)
tasks.register("coreModuleStatus") {
    group = "aegenesis"
    description = "Show core module status"
    doLast {
        println("🏗️  CORE MODULE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${project.findProperty("android.namespace")}")
        println("📱 SDK: ${project.findProperty("android.compileSdk")}")
        println("🎨 Compose: ❌ Removed")
        println(
            "🔗 API Generation: ${
                if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) "✅ Enabled" else "❌ No spec"
            }"
        )
        println("✨ Status: Core Foundation Ready with Convention Plugins!")
    }
}
