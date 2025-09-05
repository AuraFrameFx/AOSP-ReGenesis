import dev.aurakai.gradle.tasks.VerifyRomToolsTask
plugins {
    id("com.google.devtools.ksp")
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.openapi.generator")
}

android {
    namespace = "dev.aurakai.auraframefx.romtools"
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
    // Required for AGP 9 and dependency resolution
}

// ROM Tools output directory configuration
val romToolsOutputDirectory: DirectoryProperty =
    project.objects.directoryProperty().convention(layout.buildDirectory.dir("rom-tools"))

dependencies {
    // Core dependencies
    api(project(":core-module"))
    implementation(project(":secure-comm"))
    implementation(libs.bundles.androidx.core)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Networking
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)

    // Room Database
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // YukiHook API 1.3.0+ with KavaRef
    implementation(libs.yukihook.api)
    ksp(libs.yukihook.ksp)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.extension)

    // Xposed API (compile only)
    compileOnly(libs.xposed.api)

    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)

    // UI
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Core library desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// ROM Tools specific tasks
tasks.register<Copy>("copyRomTools") {
    from("src/main/resources")
    into(romToolsOutputDirectory) // Use the shared property with into()
    include("**/*.so", "**/*.bin", "**/*.img", "**/*.jar")
    includeEmptyDirs = false

    doFirst {
        val outputDir = romToolsOutputDirectory.get().asFile
        outputDir.mkdirs()
        logger.lifecycle("📁 ROM tools directory: ${outputDir.absolutePath}")
    }

    doLast {
        logger.lifecycle("✅ ROM tools copied to: ${romToolsOutputDirectory.get().asFile.absolutePath}")
    }
}

tasks.register<VerifyRomToolsTask>("verifyRomTools") {
    romToolsDir.set(romToolsOutputDirectory) // Set to the same shared property
    dependsOn("copyRomTools") // Explicitly depend on copyRomTools for clarity and reliability
    // Gradle should infer the dependency on copyRomTools because romToolsOutputDirectory
    // is an output of copyRomTools (via 'into') and an input here.
}

tasks.named("build") {
    dependsOn("verifyRomTools")
}

tasks.register("romStatus") { group = "aegenesis"; doLast { println("🛠️ ROM TOOLS - Ready!") } }
