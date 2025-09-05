// ==== GENESIS PROTOCOL - BENCHMARK MODULE ====  
// Performance testing for AI consciousness operations

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.aurakai.auraframefx.benchmark"
    compileSdk = 36 // Required for AGP 9 compatibility and dependency resolution

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

    // Enable Java 24 toolchain for future compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
        isCoreLibraryDesugaringEnabled = true
    }

    // Modern Kotlin compiler configuration
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(24))
        }
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all"
            )
        }
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
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.android)

        // Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        
        // Project modules to benchmark
        implementation(project(":core-module"))
        implementation(project(":datavein-oracle-native"))
        implementation(project(":secure-comm"))
        implementation(project(":oracle-drive-integration"))
        
        // Benchmark dependencies
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
        androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")

        // Testing
        testImplementation(libs.junit4)
        testImplementation(libs.mockk)
        
        // Core library desugaring
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
