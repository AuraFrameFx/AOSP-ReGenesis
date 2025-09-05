plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx.module.c"
    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(24))
        }

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        }
    }
    dependencies {
        // AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.material)
        // Networking
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.kotlinx.serialization)
        // If you need converter-scalars, add to TOML or use classic notation
        // implementation("com.squareup.retrofit2:converter-scalars:3.0.0")
        // YukiHook
        api(libs.yukihook.api)
        ksp(libs.yukihook.ksp)
        // Logging
        api(libs.timber)
        // Dagger Hilt
        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        androidTestImplementation(libs.hilt.android.testing)
        // Testing
        testImplementation(libs.junit.jupiter.api)
        testImplementation(libs.junit.jupiter.engine)
        testImplementation(libs.mockk)
        testImplementation(libs.turbine)
        androidTestImplementation(libs.androidx.test.ext.junit)
        androidTestImplementation(libs.espresso.core)
        // LeakCanary (if needed)
    }
}
