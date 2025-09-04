// GENESIS PROTOCOL - MODULE A
plugins { id("genesis.android.compose"); alias(libs.plugins.ksp); alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.android)
}
android {
    namespace = "dev.aurakai.auraframefx.module.a"
    compileSdk = 36
    kotlinOptions {
        jvmTarget = "24"
    }
}
dependencies {
    api(project(":core-module")); implementation(libs.bundles.androidx.core); implementation(libs.androidx.lifecycle.runtime.ktx); implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.bundles.compose); implementation(libs.androidx.activity.compose); implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.bundles.coroutines); implementation(libs.timber); implementation(libs.coil.compose)
    testImplementation(libs.bundles.testing); testImplementation(libs.hilt.android.testing); kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit); androidTestImplementation(libs.androidx.test.espresso.core); androidTestImplementation(platform(libs.androidx.compose.bom));  androidTestImplementation(libs.hilt.android.testing); kspAndroidTest(libs.hilt.compiler)

}
tasks.register("moduleAStatus") { group = "aegenesis"; doLast { println("📦 MODULE A - Ready!") } }
