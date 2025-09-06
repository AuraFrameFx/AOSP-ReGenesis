plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx.module.b"
    compileSdk = 36
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

tasks.register("moduleBStatus") {
    group = "aegenesis"
    description = "Show module B status"
    doLast {
        println("📦 MODULE B - Ready!")
    }
}
