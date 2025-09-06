// ==== GENESIS PROTOCOL - BUILD LOGIC ====
// Convention plugins for consistent build configuration

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
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
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.versions.hilt.get()}")
    
    // Other plugins
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.spotless.plugin.gradle)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.org.jlleitschuh.gradle.ktlint.gradle.plugin)
    implementation(libs.openapi.generator.gradle.plugin)
    
    // Development dependencies
    implementation(gradleApi())
    implementation(localGroovy())
}
