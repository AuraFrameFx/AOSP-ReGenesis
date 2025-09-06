// ==== GENESIS PROTOCOL - COLOR BLENDR ====
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
}
android {
    namespace = "dev.aurakai.auraframefx.colorblendr"
    compileSdk = 36
    defaultConfig { minSdk = 34 }
    buildFeatures { compose = true; buildConfig = true }
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin { jvmToolchain(24); compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) } }
}

dependencies {
    api(project(":core-module"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(libs.ktx.coroutines.core); implementation(libs.ktx.coroutines.android)
    implementation(libs.datastore.preferences)
    implementation(libs.timber); implementation(libs.coil.compose)
    testImplementation(libs.junit); testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register("colorStatus") { group = "aegenesis"; doLast { println("🎨 COLOR BLENDR - Ready (Java 24)") } }
