// ==== GENESIS PROTOCOL - COLOR BLENDR ====
plugins {
    id("com.android.library") version "9.0.0-alpha02"
    id("org.jetbrains.kotlin.android") version "2.2.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    id("com.google.devtools.ksp") version "2.2.10-2.2.10"
    id("com.google.dagger.hilt.android") version "2.51.1"
    alias(libs.plugins.dokka)
}
android {
    namespace = "dev.aurakai.auraframefx.colorblendr"
    compileSdk = 36 // Required for AGP 9 and dependency resolution
    buildFeatures { compose = true }

}
dependencies {
    // Core dependencies
    api(project(":core-module"))
    implementation(libs.bundles.androidx.core)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    // Coroutines
    implementation(libs.bundles.coroutines)
    
    // DataStore
    implementation(libs.datastore.preferences)
    
    // UI
    implementation(libs.timber)
    implementation(libs.coil.compose)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
tasks.register("colorStatus") { group = "aegenesis"; doLast { println("🎨 COLOR BLENDR - Ready!") } }
