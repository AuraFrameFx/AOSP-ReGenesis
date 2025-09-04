// ==== GENESIS PROTOCOL - BUILD LOGIC ====
// Convention plugins for consistent build configuration

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Access to Android Gradle Plugin and Kotlin plugin APIs)
    implementation("com.android.tools.build:gradle")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("com.google.dagger:hilt-android-gradle-plugin")
    
    // Other plugins
    implementation(libs.dokka.gradle.plugin)
     implementation(libs.detekt.gradle.plugin)
    implementation(libs.org.jlleitschuh.gradle.ktlint.gradle.plugin)
    implementation(libs.openapi.generator.gradle.plugin)
    
    // Development dependencies
    implementation(gradleApi())
    implementation(localGroovy())
}
