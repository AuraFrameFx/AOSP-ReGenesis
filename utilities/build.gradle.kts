import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")  // Changed from alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
}
group = "dev.aurakai.auraframefx.utilities"
version = "1.0.0"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(24)) }
}

kotlin {
    jvmToolchain(24)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
}

dependencies {
    // Module dependencies - utilities depends on list (not circular)
    api(project(":list"))

    // Kotlin standard library
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    // Utilities for file operations and compression
    implementation(libs.commons.io)
    implementation(libs.commons.compress)
    implementation(libs.xz)

    // Logging
    implementation(libs.timber)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
}
