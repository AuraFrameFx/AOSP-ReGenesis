// ==== GENESIS PROTOCOL - SANDBOX UI ====
plugins {
    id("com.android.library")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
}

android {
    namespace = "dev.aurakai.auraframefx.sandboxui"
    compileSdk = 36
    defaultConfig { minSdk = 34 }
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin { jvmToolchain(24); compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) } }
    buildFeatures { compose = true }
}

dependencies {
    api(project(":core-module"))
    implementation(libs.bundles.androidx.core)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(libs.bundles.coroutines)
    implementation(libs.timber); implementation(libs.coil.compose)
    testImplementation(libs.bundles.testing); testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    testImplementation(libs.hilt.android.testing); kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing); kspAndroidTest(libs.hilt.compiler)
}

tasks.register("sandboxStatus") { group = "aegenesis"; doLast { println("🧪 SANDBOX UI - Ready (Java 24)") } }
