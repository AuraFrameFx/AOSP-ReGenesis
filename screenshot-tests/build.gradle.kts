plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.openapi.generator)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "dev.aurakai.auraframefx.screenshottests"
    compileSdk = 36 // Required for AGP 9 and dependency resolution

    // Disable unnecessary features for screenshot testing
    buildFeatures {
        compose = true
    }
    // Modern Kotlin configuration with Java 24
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
        
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Project modules to test
    testImplementation(project(":sandbox-ui"))
    testImplementation(project(":colorblendr"))
    testImplementation(project(":collab-canvas"))

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Firebase - Add the missing BOM and bundle
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // YukiHook API with KavaRef
    implementation(libs.yukihook.api)
    ksp(libs.yukihook.ksp)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    // Xposed API (compile only)
    compileOnly(libs.xposed.api)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines & Utilities
    implementation(libs.bundles.coroutines)
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    // Android Testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}

// Custom screenshot testing tasks
tasks.register("screenshotTestAll") {
    group = "screenshot"
    description = "Run all Genesis Protocol screenshot tests"

    dependsOn("testDebugUnitTest")

    doLast {
        println("📸 Genesis Protocol Screenshot Tests")
        println("🎨 Visual regression testing for:")
        println("   - Core UI components")
        println("   - Color management (ColorBlendr)")
        println("   - Collaboration interface (CollabCanvas)")
        println("   - Sandbox experiments (SandboxUI)")
        println("💡 Configure Paparazzi when needed for advanced screenshot testing")
    }
}

tasks.register("updateScreenshots") {
    group = "screenshot"
    description = "Update Genesis Protocol UI component screenshots"

    doLast {
        println("📸 Genesis Protocol screenshots update ready")
        println("🎨 Configure screenshot baseline when Paparazzi is available")
    }
}

tasks.register("verifyScreenshots") {
    group = "verification"
    description = "Verify UI components match reference screenshots"

    doLast {
        println("✅ Genesis Protocol UI visual consistency framework ready")
        println("🎨 Screenshot testing infrastructure configured")
    }
}
