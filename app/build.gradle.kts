// ==== GENESIS PROTOCOL - MAIN APPLICATION ====
// This build script uses the modern plugins DSL and relies on the version catalog.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.aura.memoria"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aura.memoria"
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
        // Removed deprecated renderScript & unnecessary shaders
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

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(24))
        }
    }

    kotlin {
        jvmToolchain(24);
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/versions/**",
                "META-INF/*.version",
                "META-INF/*.kotlin_module",
                "META-INF/licenses/**",
                "**/attach_hotspot_windows.dll",
                "META-INF/INDEX.LIST",
                "META-INF/io.netty.versions.properties"
            )
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
        arg(
            "room.schemaLocation",
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
    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Xposed / YukiHook
    compileOnly(libs.xposed.api)
    implementation(libs.yukihook.core); ksp(libs.yukihook.ksp); implementation(libs.yukihook.prefs)

    // AndroidX & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Room
    implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler)

    // Modules
    implementation(project(":core-module"))
    implementation(project(":feature-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":romtools"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":colorblendr"))
    implementation(project(":sandbox-ui"))
    implementation(project(":datavein-oracle-native"))

    // DI
    implementation(libs.hilt.android); kspTest(libs.hilt.compiler); kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.hilt.android.testing); kspAndroidTest(libs.hilt.compiler)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines & Networking
    implementation(libs.bundles.coroutines); implementation(libs.bundles.network)

    // Firebase
    implementation(platform(libs.firebase.bom)); implementation(libs.bundles.firebase)

    // Utilities
    implementation(libs.timber); implementation(libs.coil.compose)

    // Local Xposed jars fallback (keep if needed)
    compileOnly(fileTree("Libs") { include("*.jar") })

    // Testing
    testImplementation(libs.bundles.testing); testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui)
}

tasks.named("preBuild") {
    if (rootProject.file("app/api/unified-aegenesis-api.yml")
            .exists()
    ) dependsOn(":openApiGenerate")
}

tasks.register("appStatus") {
    group = "aegenesis"
    description = "Show main application status"
    doLast {
        println("📱 MAIN APPLICATION STATUS")
        println("=".repeat(40))
        val androidExt =
            extensions.getByType(com.android.build.gradle.internal.dsl.BaseAppModuleExtension::class.java)
        val cfg = androidExt.defaultConfig
        println("🔧 Namespace: ${androidExt.namespace}")
        println("🎯 App ID: ${cfg.applicationId}")
        println("📱 Version: ${cfg.versionName ?: "unspecified"} (${cfg.versionCode ?: "unspecified"})")
        println("📱 SDK: ${androidExt.compileSdk} (Min: ${cfg.minSdk ?: "?"}, Target: ${cfg.targetSdk ?: "?"})")
        println("🎨 Compose: ✅ Enabled")
        println("🧠 Desugaring: ✅ Enabled")
        println("🧪 Tests: MockK + Hilt configured")
        println("✨ Status: Genesis Protocol Application Ready (Java 24)")
    }
}
