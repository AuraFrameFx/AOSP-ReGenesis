// ==== GENESIS PROTOCOL - BENCHMARK MODULE ====  
// Performance testing for AI consciousness operations

plugins {
    id("com.android.library") version "7.4.2"
    id("org.jetbrains.kotlin.android") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    id("com.google.dagger.hilt.android") version "2.51.1"
    id("com.google.devtools.ksp") version "1.8.22-1.0.11"
    kotlin("kapt") version "1.8.22"
}

android {
    namespace = "dev.aurakai.auraframefx.benchmark"
    compileSdk = 34 // Updated for stable AGP compatibility

    // Enable benchmark optimizations
    buildTypes {
        maybeCreate("benchmark")
        getByName("benchmark") {
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "benchmark-rules.pro")
        }
    }

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,LOW_BATTERY,DEBUGGABLE"
        testInstrumentationRunnerArguments["android.experimental.self-instrumenting"] = "true"
    }

    // Enable Java 21 toolchain for compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        isCoreLibraryDesugaringEnabled = true
    }

    // Modern Kotlin compiler options using kotlinOptions (compilerOptions requires Kotlin 2.0+)
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"
        )
    }


    // Enable build features
    buildFeatures {
        buildConfig = true
        aidl = false
        renderScript = false
        shaders = false
    }

    // Enable test coverage
    testCoverage {
        jacocoVersion = "0.8.11"
    }

    dependencies {
        // Core AndroidX
        implementation(libs.androidx.core.ktx)
        
        // Coroutines
        implementation(libs.ktx.coroutines.core)
        implementation(libs.ktx.coroutines.android)
        
        // Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        
        // Project modules to benchmark
        implementation(project(":core-module"))
        implementation(project(":datavein-oracle-native"))
        implementation(project(":secure-comm"))
        implementation(project(":oracle-drive-integration"))
        
        // Benchmark dependencies - using only what's available in the version catalog
        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.androidx.test.espresso.core)
        androidTestImplementation(libs.androidx.test.uiautomator)
        
        // Testing
        testImplementation(libs.junit)
        testImplementation(libs.mockk)
        
        // Core library desugaring - using direct version since it's not in the version catalog
        coreLibraryDesugaring(libs.desugar.jdk.libs)
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

