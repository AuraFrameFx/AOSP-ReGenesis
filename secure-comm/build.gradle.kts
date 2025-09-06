// ==== GENESIS PROTOCOL - SECURE COMMUNICATION MODULE ====
// Security module using convention plugins

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.aurakai.auraframefx.securecomm"
    compileSdk = 36 // Required for AGP 9 and dependency resolution

    // Java 24 toolchain (unified across project)
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                events("passed", "skipped", "failed")
                showStandardStreams = true
            }
            it.systemProperty("robolectric.enabled", "true")
        }
        unitTests.isIncludeAndroidResources = true
    }

// KSP configuration for this module
    ksp {
        arg("kotlin.languageVersion", "2.2")
        arg("kotlin.apiVersion", "2.2")
        arg("kotlin.jvmTarget", "24")
    }
}

dependencies {
    api(project(":core-module"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.network)
    implementation(libs.bcprov-jdk18on)
    implementation(libs.androidx-security)
    implementation(libs.timber)
    implementation(libs.gson)
    testImplementation(libs.junit)
    testImplementation(libs.junit-jupiter)
    testImplementation(libs.junit-jupiter-api)
    testRuntimeOnly(libs.junit-jupiter-engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.mockk.android)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
tasks.register("securityStatus") {
    group = "aegenesis"
    doLast { println("🔒 SECURE COMMUNICATION - ${android.namespace} - Ready!") }
}
