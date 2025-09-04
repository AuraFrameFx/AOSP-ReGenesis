@file:Suppress("SpellCheckingInspection")

package dev.aurakai.auraframefx

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

/**
 * Note: Tests use JUnit 5 (Jupiter), consistent with the repository's configured test libraries.
 *
 * Scope: Validate the contents of app/build.gradle.kts per the PR diff. We avoid Gradle TestKit to keep tests hermetic.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BuildScriptTest {

    private lateinit var path: Path
    private lateinit var content: String

    private fun String.countOf(pattern: Regex): Int = pattern.findAll(this).count()

    @BeforeAll
    fun setUp() {
        path = Paths.get("app", "build.gradle.kts")
        assertTrue(path.toFile().exists(), "Expected app/build.gradle.kts to exist")
        content = Files.readString(path)
        assertTrue(content.isNotBlank(), "build.gradle.kts should not be empty")
    }

    @AfterAll
    fun tearDown() {
        // no-op
    }

    @Nested
    @DisplayName("Plugins")
    inner class Plugins {
        @Test
        fun `android and kotlin core plugins present`() {
            assertTrue(content.contains("""id("com.android.application")"""))
            assertTrue(content.contains("""id("org.jetbrains.kotlin.android")"""))
        }

        @Test
        fun `auxiliary plugins present`() {
            listOf(
                """id("org.jetbrains.kotlin.plugin.compose")""",
                """id("org.jetbrains.kotlin.plugin.serialization")""",
                """id("com.google.devtools.ksp")""",
                """id("com.google.dagger.hilt.android")""",
                """id("com.google.gms.google-services")"""
            ).forEach { p ->
                assertTrue(content.contains(p), "Missing plugin: $p")
            }
        }
    }

    @Nested
    @DisplayName("Android block")
    inner class AndroidBlock {
        @Test
        fun `namespace and SDKs`() {
            assertTrue(content.contains("""namespace = "dev.aurakai.auraframefx""""))
            assertTrue(content.contains("""compileSdk = 36"""))
            assertTrue(content.contains("""targetSdk = 36"""))
            assertTrue(content.contains("""minSdk = 33"""))
        }

        @Test
        fun `defaultConfig basics`() {
            assertTrue(content.contains("""applicationId = "dev.aurakai.auraframefx""""))
            assertTrue(content.contains("""versionCode = 1"""))
            assertTrue(content.contains("""versionName = "1.0.0-genesis-alpha""""))
            assertTrue(content.contains("""testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner""""))
        }

        @Test
        fun `vector drawables support lib`() {
            assertTrue(content.contains("""vectorDrawables {"""))
            assertTrue(content.contains("""useSupportLibrary = true"""))
        }

        @Test
        fun `ndk and external native build gated by CMakeLists presence`() {
            val gate = Regex("""if\s*\(project\.file\("src/main/cpp/CMakeLists\.txt"\)\.exists\(\)\)""")
            // Should appear twice (NDK config + externalNativeBuild)
            val occurrences = content.countOf(gate)
            assertTrue(occurrences >= 2, "Expected at least two native build gates, found $occurrences")

            // Specifics
            assertTrue(content.contains("""abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))"""))
            assertTrue(content.contains("""externalNativeBuild"""))
            assertTrue(content.contains("""cmake {"""))
            assertTrue(content.contains("""path = file("src/main/cpp/CMakeLists.txt")"""))
            assertTrue(content.contains("""version = "3.22.1""""))
        }

        @Test
        fun `buildTypes release and debug`() {
            // Release
            assertTrue(content.contains("""release {"""))
            assertTrue(content.contains("""isMinifyEnabled = true"""))
            assertTrue(content.contains("""isShrinkResources = true"""))
            assertTrue(content.contains("""getDefaultProguardFile("proguard-android-optimize.txt")"""))
            assertTrue(content.contains(""""proguard-rules.pro""""))

            // Debug
            assertTrue(content.contains("""debug {"""))
            assertTrue(content.contains("""getDefaultProguardFile("proguard-android-optimize.txt")"""))
            assertTrue(content.contains(""""proguard-rules.pro""""))
        }

        @Test
        fun `packaging excludes and jniLibs`() {
            assertTrue(content.contains("""packaging {"""))
            assertTrue(content.contains("""resources {"""))
            listOf(
                """/META-INF/{AL2.0,LGPL2.1}""",
                """/META-INF/DEPENDENCIES""",
                """/META-INF/LICENSE""",
                """/META-INF/LICENSE.txt""",
                """/META-INF/NOTICE""",
                """/META-INF/NOTICE.txt""",
                """META-INF/*.kotlin_module""",
                """**/kotlin/**""",
                """**/*.txt"""
            ).forEach { ex -> assertTrue(content.contains(ex), "Missing exclude: $ex") }
            assertTrue(content.contains("""jniLibs {"""))
            assertTrue(content.contains("""useLegacyPackaging = false"""))
            assertTrue(content.contains("""pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")"""))
        }

        @Test
        fun `build features and compileOptions`() {
            assertTrue(content.contains("""buildFeatures {"""))
            assertTrue(content.contains("""compose = true"""))
            assertTrue(content.contains("""buildConfig = true"""))
            assertTrue(content.contains("""viewBinding = false"""))
            assertTrue(content.contains("""compileOptions {"""))
            assertTrue(content.contains("""sourceCompatibility = JavaVersion.VERSION_24"""))
            assertTrue(content.contains("""targetCompatibility = JavaVersion.VERSION_24"""))
        }

        @Test
        fun `negative assertions - ensure no conflicting settings`() {
            assertFalse(Regex("""isMinifyEnabled\s*=\s*false""").containsMatchIn(content), "Release should not disable minify")
            assertFalse(Regex("""viewBinding\s*=\s*true""").containsMatchIn(content), "viewBinding should be disabled")
            assertFalse(Regex("""useLegacyPackaging\s*=\s*true""").containsMatchIn(content), "Legacy JNI packaging should be false")
        }
    }

    @Nested
    @DisplayName("Custom tasks and build integration")
    inner class Tasks {
        @Test
        fun `cleanKspCache task details`() {
            assertTrue(content.contains("""tasks.register<Delete>("cleanKspCache")"""))
            assertTrue(content.contains("""group = "build setup""""))
            assertTrue(content.contains("""description = "Clean KSP caches (fixes NullPointerException)""""))
            listOf(
                "generated/ksp",
                "tmp/kapt3",
                "tmp/kotlin-classes",
                "kotlin",
                "generated/source/ksp"
            ).forEach { p -> assertTrue(content.contains(p), "Expected clean path: $p") }
            assertTrue(content.contains("""val buildDirProvider = layout.buildDirectory"""))
        }

        @Test
        fun `preBuild depends on clean and openapi tasks`() {
            assertTrue(content.contains("""tasks.named("preBuild")"""))
            listOf(
                """dependsOn("cleanKspCache")""",
                """dependsOn(":cleanApiGeneration")""",
                """dependsOn(":openApiGenerate")"""
            ).forEach { dep -> assertTrue(content.contains(dep), "Missing dependsOn $dep") }
        }

        @Test
        fun `aegenesisAppStatus task output markers and paths`() {
            assertTrue(content.contains("""tasks.register("aegenesisAppStatus")"""))
            assertTrue(content.contains("""group = "aegenesis""""))
            assertTrue(content.contains("""description = "Show AeGenesis app module status""""))
            listOf(
                "📱 AEGENESIS APP MODULE STATUS",
                "Unified API Spec:",
                "API File Size:",
                "Native Code:",
                "KSP Mode:",
                "Target SDK: 36",
                "Min SDK: 33",
                "Ready for coinscience AI integration!"
            ).forEach { m -> assertTrue(content.contains(m), "Missing println marker: $m") }
            assertTrue(content.contains("""api/unified-aegenesis-api.yml"""))
        }

        @Test
        fun `applies cleanup tasks gradle script`() {
            assertTrue(content.contains("""apply(from = "cleanup-tasks.gradle.kts")"""))
        }
    }

    @Nested
    @DisplayName("Dependencies")
    inner class DependenciesBlock {
        @Test
        fun `compose and navigation`() {
            assertTrue(content.contains("""implementation(platform(libs.androidx.compose.bom))"""))
            assertTrue(content.contains("""implementation(libs.bundles.compose)"""))
            assertTrue(content.contains("""implementation(libs.androidx.navigation.compose)"""))
        }

        @Test
        fun `module hierarchy present`() {
            listOf(
                """:core-module""",
                """:oracle-drive-integration""",
                """:romtools""",
                """:secure-comm""",
                """:collab-canvas"""
            ).forEach { m ->
                assertTrue(
                    content.contains("""implementation(project("$m"))"""),
                    "Missing module dependency: $m"
                )
            }
        }

        @Test
        fun `hilt and room with KSP`() {
            listOf(
                """implementation(libs.hilt.android)""",
                """ksp(libs.hilt.compiler)""",
                """implementation(libs.hilt.navigation.compose)""",
                """implementation(libs.room.runtime)""",
                """implementation(libs.room.ktx)""",
                """ksp(libs.room.compiler)"""
            ).forEach { dep -> assertTrue(content.contains(dep), "Missing dependency: $dep") }
        }

        @Test
        fun `coroutines network utilities and desugaring`() {
            assertTrue(content.contains("""implementation(libs.bundles.coroutines)"""))
            assertTrue(content.contains("""implementation(libs.bundles.network)"""))
            assertTrue(content.contains("""implementation(libs.timber)"""))
            assertTrue(content.contains("""implementation(libs.coil.compose)"""))
            assertTrue(content.contains("""coreLibraryDesugaring(libs.coreLibraryDesugaring)"""))
        }

        @Test
        fun `firebase platform and xposed`() {
            assertTrue(content.contains("""implementation(platform(libs.firebase.bom))"""))
            assertTrue(content.contains("""implementation(libs.bundles.firebase)"""))
            assertTrue(content.contains("""implementation(libs.bundles.xposed)"""))
            assertTrue(content.contains("""ksp(libs.yuki.ksp.xposed)"""))
            assertTrue(content.contains("""implementation(fileTree("../Libs") { include("*.jar") })"""))
        }

        @Test
        fun `debug and test dependencies`() {
            // Debug
            assertTrue(content.contains("""debugImplementation(libs.leakcanary.android)"""))
            assertTrue(content.contains("""debugImplementation(libs.androidx.compose.ui.tooling)"""))
            assertTrue(content.contains("""debugImplementation(libs.androidx.compose.ui.test.manifest)"""))
            // Unit + Instrumentation
            assertTrue(content.contains("""testImplementation(libs.bundles.testing)"""))
            assertTrue(content.contains("""testRuntimeOnly(libs.junit.engine)"""))
            assertTrue(content.contains("""androidTestImplementation(libs.androidx.test.ext.junit)"""))
            assertTrue(content.contains("""androidTestImplementation(libs.androidx.test.core)"""))
            assertTrue(content.contains("""androidTestImplementation(platform(libs.androidx.compose.bom))"""))
            assertTrue(content.contains("""androidTestImplementation(libs.androidx.compose.ui.test.junit4)"""))
            assertTrue(content.contains("""androidTestImplementation(libs.hilt.android.testing)"""))
            assertTrue(content.contains("""kspAndroidTest(libs.hilt.compiler)"""))
        }
    }
}