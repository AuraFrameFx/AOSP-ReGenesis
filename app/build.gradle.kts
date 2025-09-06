// ==== GENESIS PROTOCOL - MAIN APPLICATION ====
// This build script uses the modern plugins DSL and relies on the version catalog.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(24)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) }
}

// KSP arguments (moved outside android block)
ksp {
    arg("room.incremental", "true")
    arg("dagger.hilt.android.internal.projectType", "application")
    arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
    arg("room.schemaLocation", layout.buildDirectory.dir("schemas").get().asFile.toString())
}

android {
    namespace = "com.aura.memoria"
    compileSdk = 36

    // Retain app-specific defaultConfig overrides
    defaultConfig {
        applicationId = "com.aura.memoria"
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        // Keep NDK abi filters if native code present
        if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
            ndk { abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a")) }
        }
    }
    buildTypes {
        release { buildConfigField("String", "ENVIRONMENT", "\"production\"") }
        debug { buildConfigField("String", "ENVIRONMENT", "\"debug\"") }
    }
    buildFeatures { compose = true; buildConfig = true }
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    compileOptions { isCoreLibraryDesugaringEnabled = true }

    // External native build if present
    if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
        externalNativeBuild {
            cmake { path = file("src/main/cpp/CMakeLists.txt"); version = "3.22.1" }
        }
    }
    packaging { resources { excludes += setOf("META-INF/io.netty.versions.properties") } }
}

// Provide a single informational task instead
tasks.register("googleServicesStatus") { doLast { println("google-services plugin: managed by convention (skipped under AGP 9 alpha unless -PenableGoogleServices=true & AGP8.x)") } }

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // YukiHook offline jars (unchanged)
    compileOnly(files("Libs/api-82.jar"))
    implementation(files("Libs/yukihookapi-core.jar"))
    ksp(files("Libs/yukihookapi-ksp.jar"))
    implementation(files("Libs/yukihookapi-prefs.jar"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(project(":core-module"))
    implementation(project(":feature-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":romtools"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":colorblendr"))
    implementation(project(":sandbox-ui"))
    implementation(project(":datavein-oracle-native"))

    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(libs.hilt.work) // Hilt <-> WorkManager integration
    implementation(libs.work.runtime.ktx) // WorkManager runtime
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)
    implementation(platform(libs.firebase.bom)); implementation(libs.bundles.firebase)
    implementation(libs.timber); implementation(libs.coil.compose)

    testImplementation(libs.bundles.testing); testImplementation(libs.mockk); androidTestImplementation(libs.mockk.android)
    testImplementation(libs.hilt.android.testing); androidTestImplementation(libs.hilt.android.testing); kspTest(libs.hilt.compiler); kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit); androidTestImplementation(libs.androidx.test.espresso.core); androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.compose.ui.tooling); debugImplementation(libs.androidx.compose.ui)
}

// Keep OpenAPI generate dependency if spec exists
tasks.named("preBuild") {
    if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) dependsOn(":openApiGenerate")
}

tasks.register("appStatus") {
    group = "aegenesis"; description = "Show main application status"
    doLast { println("📱 MAIN APPLICATION STATUS (Convention) :: Namespace=com.aura.memoria :: google-services managed externally") }

    }
