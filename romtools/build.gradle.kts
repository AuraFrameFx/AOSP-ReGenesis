import dev.aurakai.gradle.tasks.VerifyRomToolsTask


plugins {
    id("genesis.android.compose")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.aurakai.auraframefx.romtools"
    compileSdk = 36
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin { jvmToolchain(24); compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24) } }
}

// Unified output directory property
val romToolsOutputDirectory: DirectoryProperty =
    project.objects.directoryProperty().convention(layout.buildDirectory.dir("rom-tools"))

dependencies {
    api(project(":core-module"))
    implementation(project(":secure-comm"))
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.bundles.androidx.core)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)
    implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler)
    implementation(libs.bundles.firebase)
    implementation(libs.timber); implementation(libs.coil.compose)
    debugImplementation(libs.leakcanary.android); debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.bundles.testing); testImplementation(libs.mockk); androidTestImplementation(libs.mockk.android)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.hilt.android.testing); kspAndroidTest(libs.hilt.compiler)
}

// Copy task
tasks.register<Copy>("copyRomTools") {
    from("src/main/resources")
    into(romToolsOutputDirectory)
    include("**/*.so", "**/*.bin", "**/*.img", "**/*.jar")
    includeEmptyDirs = false
    doFirst { romToolsOutputDirectory.get().asFile.mkdirs(); logger.lifecycle("📁 ROM tools directory: ${romToolsOutputDirectory.get().asFile}") }
    doLast { logger.lifecycle("✅ ROM tools copied to: ${romToolsOutputDirectory.get().asFile}") }
}

// Verification task
tasks.register<VerifyRomToolsTask>("verifyRomTools") {
    romToolsDir.set(romToolsOutputDirectory)
    dependsOn("copyRomTools")
}

tasks.named("build") { dependsOn("verifyRomTools") }

tasks.register("romStatus") { group = "aegenesis"; doLast { println("🛠️ ROM TOOLS - Ready (Java 24)") } }
