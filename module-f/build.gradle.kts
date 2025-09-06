// GENESIS PROTOCOL - MODULE F
plugins { id("genesis.android.compose"); alias(libs.plugins.kotlin.serialization); alias(libs.plugins.ksp); alias(libs.plugins.hilt); alias(libs.plugins.dokka); alias(libs.plugins.spotless); alias(libs.plugins.kotlin.android) }
android {
    namespace = "dev.aurakai.auraframefx.module.f"
    compileSdk = 36
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin { jvmToolchain(24); compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) } }
}
dependencies {
    api(project(":core-module")); implementation(libs.bundles.androidx.core); implementation(libs.androidx.lifecycle.runtime.ktx); implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom)); implementation(libs.bundles.compose); implementation(libs.androidx.activity.compose); implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.bundles.coroutines); implementation(libs.bundles.network)
    implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler); implementation(platform(libs.firebase.bom)); implementation(libs.bundles.firebase)
    implementation(libs.timber); implementation(libs.coil.compose); implementation(fileTree("../Libs") { include("*.jar") })
    testImplementation(libs.bundles.testing); testImplementation(libs.mockk); androidTestImplementation(libs.mockk.android); testImplementation(libs.hilt.android.testing); kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit); androidTestImplementation(libs.androidx.test.espresso.core); androidTestImplementation(platform(libs.androidx.compose.bom));androidTestImplementation(libs.hilt.android.testing); kspAndroidTest(libs.hilt.compiler)
}
tasks.register("moduleFStatus") { group = "aegenesis"; doLast { println("📦 MODULE F - Ready (Java 24)") } }
