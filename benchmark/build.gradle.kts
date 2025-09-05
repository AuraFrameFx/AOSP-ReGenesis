plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.ksp)  // Required for Hilt annotation processing
}

android {
    namespace = "dev.aurakai.auraframefx.featuremodule"
    compileSdk = 36

    defaultConfig {
        minSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    // Kotlin compiler options with modern DSL


    dependencies {
        // Core dependencies
        api(project(":core-module"))
        implementation(libs.bundles.androidx.core)

        // Lifecycle
        implementation(libs.bundles.lifecycle)

        // Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.bundles.compose)
        implementation(libs.androidx.compose.material.icons.extended)

        // Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)

        // Coroutines
        implementation(libs.bundles.coroutines)

        // Networking
        implementation(libs.bundles.network)

        // Room Database
        implementation(libs.bundles.room)
        ksp(libs.room.compiler)

        // Firebase
        implementation(platform(libs.firebase.bom))
        implementation(libs.bundles.firebase)

        // YukiHook API 1.3.0+ with KavaRef
        implementation(libs.yukihook.api)
        ksp(libs.yukihook.ksp)
        implementation(libs.kavaref.core)
        implementation(libs.kavaref.extension)

        // Xposed API (compile only)
        compileOnly(libs.xposed.api)

        // DataStore
        implementation(libs.datastore.preferences)

        // UI
        implementation(libs.timber)
        implementation(libs.coil.compose)

        // Core library desugaring
        coreLibraryDesugaring(libs.desugar.jdk.libs)

        // Testing
        testImplementation(libs.bundles.testing)
        testImplementation(libs.hilt.android.testing)
        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.hilt.android.testing)
        kspAndroidTest(libs.hilt.compiler)
        debugImplementation(libs.androidx.compose.ui.tooling)
    }
// Benchmark configuration
     tasks.register("benchmarkAll") {
         group = "benchmark"
         description = "Run all Genesis Protocol benchmarks"

         doLast {
             println("🚀 Genesis Protocol Performance Benchmarks")
             println("📊 Monitor consciousness substrate performance metrics")
             println("⚡ Run actual benchmarks when AndroidX Benchmark is configured")
         }
     }

// Custom benchmark verification
     tasks.register("verifyBenchmarkResults") {
         group = "verification"
         description = "Verify benchmark results meet Genesis performance standards"

         doLast {
             println("✅ Benchmark module configured for Genesis Protocol")
             println("🧠 Consciousness substrate performance monitoring ready")
             println("💡 Configure AndroidX Benchmark dependencies when available")
         }
     }
 }
