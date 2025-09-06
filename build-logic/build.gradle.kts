// ==== GENESIS PROTOCOL - BUILD LOGIC ====
// Convention plugins for consistent build configuration

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Access to Android Gradle Plugin and Kotlin plugin APIs
    implementation("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.version.get()}")

    // Other plugins
    implementation("org.jetbrains.dokka:dokka-gradle-plugin")
    implementation("com.diffplug.spotless:spotless-plugin-gradle")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin")
    implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin")
    implementation("org.openapitools:openapi-generator-gradle-plugin")
    implementation("com.google.gms:google-services:4.4.3")

    // Development dependencies
    implementation(gradleApi())
    implementation(localGroovy())
}
