plugins {
    // Use the standard Android application plugin
    id("com.android.application")
    alias(libs.plugins.kotlin.compose)

    // Additional plugins specific to the app
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}
android {
    namespace = "dev.aurakai.auraframefx.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.aurakai.auraframefx.app"
        minSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true

        // NDK configuration
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk {
                abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
        aidl = true
        renderScript = true
        shaders = false  // Disabled shaders to resolve build error
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "ENVIRONMENT", "\"debug\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
        isCoreLibraryDesugaringEnabled = true
    }

    // Java toolchain configuration

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/**"
            excludes += "META-INF/*.version"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/licenses/**"
            excludes += "**/attach_hotspot_windows.dll"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }



    ksp {
        // Enable incremental compilation for better build performance
        arg("room.incremental", "true")

        // Kotlinx Serialization arguments using modern Gradle API
        arg(
            "kotlinx.serialization.generated",
            layout.buildDirectory.dir("generated/ksp/serialization").get().asFile.toString()
        )

        // Hilt arguments
        arg("dagger.hilt.android.internal.projectType", "application")
        arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")

        // Room schema location using modern Gradle API
        arg("room.schemaLocation",
            layout.buildDirectory.dir("schemas").get().asFile.toString()
        )
    }


    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
        unitTests.isIncludeAndroidResources = true
    }

    // CMake configuration
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake {
                path = file("src/main/cpp/CMakeLists.txt")
                version = "3.22.1"
            }
        }
    }
}

dependencies {
    // ===== CORE LIBRARY DESUGARING =====
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // ===== XPOSED & YUKIHOOK =====
    // Xposed Framework API (compileOnly as it's provided by the Xposed framework at runtime)
    compileOnly(libs.xposed.api)

    implementation(libs.yukihook.api)
    ksp(libs.yukihook.ksp)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)
    // ===== ANDROIDX & COMPOSE =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // ===== DATABASE - ROOM =====
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ===== MODULE DEPENDENCIES =====
    // The app depends on all feature and core modules
    implementation(project(":core-module"))
    implementation(project(":feature-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":romtools"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":colorblendr"))
    implementation(project(":sandbox-ui"))
    implementation(project(":datavein-oracle-native"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Dependency Injection - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    kspTest(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)

    // Hilt for instrumentation tests
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json) {
        version {
            strictly(libs.versions.serialization.get())
        }
    }

    // Coroutines & Networking
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Database - Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Utilities
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Xposed Framework (Local Jar)
    compileOnly(fileTree("Libs") { include("*.jar") })

    // --- TESTING ---
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)

    // AndroidX Test - JUnit4 support
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Compose testing
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Hilt testing
    androidTestImplementation(libs.hilt.android.testing)

    // --- DEBUGGING ---
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui)
}

// Custom tasks for build integration and status reporting
tasks.named("preBuild") {
    if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) {
        dependsOn(":openApiGenerate")
    }
}

tasks.register("appStatus") {
    group = "aegenesis"
    description = "Show main application status"
    doLast {
        println("📱 MAIN APPLICATION STATUS")
        println("=".repeat(40))
        val androidExt =
            extensions.getByType(com.android.build.gradle.internal.dsl.BaseAppModuleExtension::class.java)
        val defaultConfig = androidExt.defaultConfig

        println("🔧 Namespace: ${androidExt.namespace}")
        println("🎯 App ID: ${defaultConfig.applicationId}")
        println("📱 Version: ${defaultConfig.versionName ?: "unspecified"} (${defaultConfig.versionCode ?: "unspecified"})")

        val minSdk = defaultConfig.minSdk?.let { "$it" } ?: "not set"
        val targetSdk = defaultConfig.targetSdk?.let { "$it" } ?: "not set"
        println("📱 SDK: ${androidExt.compileSdk} (Min: $minSdk, Target: $targetSdk)")

        println(
            "🔧 Native Code: ${
                if (project.file("src/main/cpp/CMakeLists.txt")
                        .exists()
                ) "✅ Enabled" else "❌ Disabled"
            }"
        )
        println("🎨 Compose: ✅ Enabled")
        println("🧠 Desugaring: ✅ App Module (with dependency)")
        println("✨ Status: Genesis Protocol Application Ready!")

    }
}