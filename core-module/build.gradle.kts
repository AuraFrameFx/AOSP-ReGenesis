// Apply plugins with explicit versions
plugins {
    // Replaced explicit versions with catalog aliases for consistency
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.openapi.generator)
}

android {
    namespace = "dev.aurakai.auraframefx.core"
    compileSdk = 36
    
    defaultConfig {
        minSdk = 34
        
        // Required for YukiHook
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }
    
    buildFeatures {
        // Only enable what’s required; remove renderScript/shaders
        aidl = true
        buildConfig = true
    }
    
    // Add Java toolchain 24
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(24)) } }
    kotlin {
        jvmToolchain(24)
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            freeCompilerArgs.addAll(
                "-Xcontext-receivers"
            )
            progressiveMode.set(true)
        }
    }
    
    sourceSets["main"].java.srcDir(layout.buildDirectory.dir("generated/openapi/src/main/kotlin"))
}

// OpenAPI generation task
tasks.named("preBuild").configure {
    dependsOn(tasks.named<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("openApiGenerate"))
}

dependencies {
    // Desugaring

    // Xposed / YukiHook (offline/local JAR mode)
    compileOnly(files("Libs/api-82.jar"))
    implementation(files("Libs/yukihookapi-core.jar"))
    ksp(files("Libs/yukihookapi-ksp.jar"))
    implementation(files("Libs/yukihookapi-prefs.jar"))
    // (To switch back to catalog-managed deps, replace above with
    //  compileOnly(libs.xposed.api) + implementation(libs.yukihook.core/prefs) + ksp(libs.yukihook.ksp))

    // AndroidX & Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Modules
    implementation(project(":core-module"))
    implementation(project(":feature-module"))
    implementation(project(":oracle-drive-integration"))
    implementation(project(":romtools"))
    implementation(project(":secure-comm"))
    implementation(project(":collab-canvas"))
    implementation(project(":colorblendr"))
    implementation(project(":sandbox-ui"))
    implementation(project(":datavein-oracle-native"))

    // DI (Hilt)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines & Networking bundles
    implementation(libs.bundles.coroutines)
    implementation(libs.bundles.network)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // Utilities
    implementation(libs.timber)
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui)
}

// Status task
tasks.register("coreModuleStatus") {
    group = "aegenesis"
    description = "Show core module status"
    
    doLast {
        println("🏗️  CORE MODULE STATUS")
        println("=".repeat(40))
        println("🔧 Namespace: ${android.namespace}")
        println("📱 SDK: ${android.compileSdk}")
        println("🎨 Compose: ❌ Removed")
        println("🔗 API Generation: ${if (rootProject.file("app/api/unified-aegenesis-api.yml").exists()) "✅ Enabled" else "❌ No spec"}")
        println("✨ Status: Core Foundation Ready with Convention Plugins!")
    }
}
