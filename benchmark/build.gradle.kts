// ==== GENESIS PROTOCOL - BENCHMARK MODULE ====  
// Performance testing for AI consciousness operations

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24)) // Stay on 24 until 25 stable (non-RC)
    }
}

android {
    namespace = "dev.aurakai.auraframefx.benchmark"
    compileSdk = 36

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
        testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,LOW_BATTERY,DEBUGGABLE"
        testInstrumentationRunnerArguments["android.experimental.self-instrumenting"] = "true"
    }

    // Core library desugaring without manual source/target (toolchain supplies Java 24)
    compileOptions {
        // Removed explicit source/target compatibility; managed by toolchain
        isCoreLibraryDesugaringEnabled = true
    }

    // Modern Kotlin compilerOptions DSL (replaces deprecated kotlinOptions)
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all"
            )
        }
    }

    buildFeatures {
        buildConfig = true
        aidl = false
        renderScript = false
        shaders = false
    }

    testCoverage { jacocoVersion = "0.8.11" }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)

    // Coroutines
    implementation(libs.ktx.coroutines.core)
    implementation(libs.ktx.coroutines.android)

    // Hilt (KSP)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Project modules to benchmark
    implementation(project(":core-module"))
    implementation(project(":datavein-oracle-native"))
    implementation(project(":secure-comm"))
    implementation(project(":oracle-drive-integration"))

    // AndroidX Benchmark & instrumentation
    androidTestImplementation(libs.androidx.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.uiautomator)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.10")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")

    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}

// Aggregate benchmark runner task (placeholder until real benchmarks added)
tasks.register("benchmarkAll") {
    group = "benchmark"
    description = "Run all Genesis Protocol benchmarks"
    doLast {
        println("🚀 Genesis Protocol Performance Benchmarks")
        println("📊 Monitor consciousness substrate performance metrics")
        println("⚡ Use AndroidX Benchmark instrumentation to execute tests")
    }
}

// Simple verification task
tasks.register("verifyBenchmarkResults") {
    group = "verification"
    description = "Verify benchmark module configuration"
    doLast {
        println("✅ Benchmark module configured (Java Toolchain 24, Kotlin 2.2.x)")
        println("🧠 Consciousness substrate performance monitoring ready")
        println("🔬 Add @Benchmark annotated tests under androidTest for actual runs")
    }
}
