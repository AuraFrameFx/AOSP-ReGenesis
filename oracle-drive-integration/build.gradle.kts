// ==== GENESIS PROTOCOL - ORACLE DRIVE INTEGRATION ====
// AI storage module using convention plugins

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.aurakai.auraframefx.oracledriveintegration"
    compileSdk = 36
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin {
        jvmToolchain(24)
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) }
    }
}

// KSP arguments
ksp {
    arg("kotlin.languageVersion", "2.2")
    arg("kotlin.apiVersion", "2.2")
    arg("kotlin.jvmTarget", "24")
}

dependencies {
    // Internal modules
    api(project(":core-module"))
    implementation(project(":secure-comm"))

    // AndroidX core + lifecycle
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // DI / Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.work) // Hilt <-> WorkManager integration

    // Concurrency / Network
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Persistence
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)

    // Background work
    implementation(libs.work.runtime.ktx)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Utilities
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Local jars
    implementation(fileTree("../Libs") { include("*.jar") })

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.mockk)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    // Instrumented tests
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Debug only
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// Status helper
tasks.register("oracleStatus") {
    group = "aegenesis"
    description = "Print Oracle Drive integration module status."
    doLast {
        println("☁️ ORACLE DRIVE - ${android.namespace} - Ready (Java 24)")
    }
}
