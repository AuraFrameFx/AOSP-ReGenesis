package dev.aurakai.auraframefx

/*
Testing framework and library:
- Using JUnit 5 (Jupiter) for unit tests (org.junit.jupiter.api.*).
- This repository declares testRuntimeOnly(libs.junit.engine), which typically maps to junit-jupiter-engine.
- Tests are text-based validations tailored to app/build.gradle.kts (no new dependencies introduced).
*/

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File

class BuildGradleKtsTest {

    private fun locateBuildFile(): File {
        val candidates = listOf(
            File("build.gradle.kts"),
            File("app/build.gradle.kts"),
            File("../app/build.gradle.kts")
        )
        return candidates.firstOrNull { it.exists() } ?: error(
            "Unable to locate app/build.gradle.kts. Checked: " +
                candidates.joinToString { it.path } +
                "; workingDir=${System.getProperty("user.dir")}"
        )
    }

    private val buildFile: File by lazy { locateBuildFile() }
    private val script: String by lazy { buildFile.readText() }

    @Test
    @DisplayName("Plugins: required plugins are applied")
    fun pluginsAreApplied() {
        val ids = listOf(
            "com.android.application",
            "org.jetbrains.kotlin.android",
            "org.jetbrains.kotlin.plugin.compose",
            "org.jetbrains.kotlin.plugin.serialization",
            "com.google.devtools.ksp",
            "com.google.dagger.hilt.android",
            "com.google.gms.google-services"
        )
        ids.forEach { id ->
            assertTrue(
                Regex("""id\("$id"\)""").containsMatchIn(script),
                "Expected plugin id(\"$id\") in app/build.gradle.kts"
            )
        }
    }

    @Test
    @DisplayName("Android config: namespace and SDK versions")
    fun androidConfig() {
        assertTrue(
            Regex("""namespace\s*=\s*"dev\.aurakai\.auraframefx"""").containsMatchIn(script),
            "Expected correct namespace"
        )

        Regex("""compileSdk\s*=\s*(\d+)""").find(script)?.groupValues?.get(1)?.toIntOrNull()
        Regex("""targetSdk\s*=\s*(\d+)""").find(script)?.groupValues?.get(1)?.toIntOrNull()
        val compile = Regex("""compileSdk\s*=\s*(\d+)""").find(script)?.groupValues?.get(1)?.toIntOrNull()
        val target = Regex("""targetSdk\s*=\s*(\d+)""").find(script)?.groupValues?.get(1)?.toIntOrNull()
        val min = Regex("""minSdk\s*=\s*(\d+)""").find(script)?.groupValues?.get(1)?.toIntOrNull()

        assertEquals(36, compile, "compileSdk should be 36")
        assertEquals(36, target, "targetSdk should be 36")
        assertEquals(33, min, "minSdk should be 33")
    }

    @Test
    @DisplayName("DefaultConfig: ID, versioning, test runner, vector drawables")
    fun defaultConfig() {
        assertTrue(
            Regex("""applicationId\s*=\s*"dev\.aurakai\.auraframefx"""").containsMatchIn(script),
            "Expected applicationId"
        )
        assertTrue(
            Regex("""versionCode\s*=\s*1\b""").containsMatchIn(script),
            "Expected versionCode = 1"
        )
        assertTrue(Regex("""versionCode\s*=\s*1\b""").containsMatchIn(script), "Expected versionCode = 1")
        assertTrue(
            Regex("""versionName\s*=\s*"1\.0\.0-genesis-alpha"""").containsMatchIn(script),
            "Expected versionName = 1.0.0-genesis-alpha"
        )
        assertTrue(
            Regex("""testInstrumentationRunner\s*=\s*"androidx\.test\.runner\.AndroidJUnitRunner"""")
                .containsMatchIn(script),
            "Expected AndroidJUnitRunner"
        )
        assertTrue(
            Regex("""vectorDrawables\s*\{[^}]*useSupportLibrary\s*=\s*true""", RegexOption.DOT_MATCHES_ALL)
            Regex(
                """vectorDrawables\s*\{[^}]*useSupportLibrary\s*=\s*true""",
                RegexOption.DOT_MATCHES_ALL
            )
                .containsMatchIn(script),
            "Expected vectorDrawables.useSupportLibrary = true"
        )
    }

    @Test
    @DisplayName("Native build guards exist for NDK and CMake")
    fun nativeBuildGuardsPresent() {
        assertTrue(
            Regex(
                """if\s*\(project\.file\("src/main/cpp/CMakeLists\.txt"\)\.exists\(\)\)\s*\{\s*ndk\s*\{""",
                RegexOption.DOT_MATCHES_ALL
            ).containsMatchIn(script),
            "NDK guard not found in defaultConfig"
        )
        assertTrue(
            Regex(
                """externalNativeBuild\s*\{[^}]*cmake\s*\{\s*path\s*=\s*file\("src/main/cpp/CMakeLists\.txt"\)""",
                RegexOption.DOT_MATCHES_ALL
            ).containsMatchIn(script),
            "externalNativeBuild CMake guard not found"
        )
    }

    @Test
    @DisplayName("Build types: release enables minify/shrink and uses proguard files; debug has proguardFiles set")
    fun buildTypesConfigured() {
        assertTrue(
            Regex("""buildTypes\s*\{\s*[^}]*release\s*\{[^}]*isMinifyEnabled\s*=\s*true""", RegexOption.DOT_MATCHES_ALL)
            Regex(
                """buildTypes\s*\{\s*[^}]*release\s*\{[^}]*isMinifyEnabled\s*=\s*true""",
                RegexOption.DOT_MATCHES_ALL
            )
                .containsMatchIn(script),
            "Expected release.isMinifyEnabled = true"
        )
        assertTrue(
            Regex("""release\s*\{[^}]*isShrinkResources\s*=\s*true""", RegexOption.DOT_MATCHES_ALL)
                .containsMatchIn(script),
            "Expected release.isShrinkResources = true"
        )
        assertTrue(
            Regex("""proguardFiles\([^)]*"proguard-android-optimize\.txt"[^)]*"proguard-rules\.pro"[^)]*\)""", RegexOption.DOT_MATCHES_ALL)
            Regex(
                """proguardFiles\([^)]*"proguard-android-optimize\.txt"[^)]*"proguard-rules\.pro"[^)]*\)""",
                RegexOption.DOT_MATCHES_ALL
            )
                .containsMatchIn(script),
            "Expected proguard files configuration"
        )
        assertTrue(
            Regex("""buildTypes\s*\{[^}]*debug\s*\{[^}]*proguardFiles\(""", RegexOption.DOT_MATCHES_ALL)
            Regex(
                """buildTypes\s*\{[^}]*debug\s*\{[^}]*proguardFiles\(""",
                RegexOption.DOT_MATCHES_ALL
            )
                .containsMatchIn(script),
            "Expected debug.proguardFiles to be present"
        )
    }

    @Test
    @DisplayName("Packaging: resource excludes and jniLibs configuration")
    fun packagingConfigured() {
        val excludes = listOf(
            """/META-INF/\{AL2\.0,LGPL2\.1\}""",
            """/META-INF/DEPENDENCIES""",
            """/META-INF/LICENSE(\.txt)?""",
            """/META-INF/NOTICE(\.txt)?""",
            """META-INF/\*\.kotlin_module""",
            """\*\*/kotlin/\*\*""",
            """\*\*/\*\.txt"""
        )
        excludes.forEach { pattern ->
            assertTrue(
                Regex(pattern).containsMatchIn(script),
                "Expected packaging.resources.excludes to contain $pattern"
            )
        }
        assertTrue(
            Regex(
                """jniLibs\s*\{[^}]*useLegacyPackaging\s*=\s*false""",
                RegexOption.DOT_MATCHES_ALL
            )
                .containsMatchIn(script),
            "Expected jniLibs.useLegacyPackaging = false"
        )
        assertTrue(
            Regex("""pickFirsts\s*\+\=\s*listOf\("(\*\*/)?libc\+\+_shared\.so",\s*"(\*\*/)?libjsc\.so"\)""")
                .containsMatchIn(script),
            "Expected jniLibs.pickFirsts to include libc++_shared.so and libjsc.so"
        )
    }

    @Test
    @DisplayName("Build features: compose/buildConfig enabled and viewBinding disabled")
    fun buildFeaturesConfigured() {
        assertTrue(
            Regex("""buildFeatures\s*\{[^}]*compose\s*=\s*true""", RegexOption.DOT_MATCHES_ALL)
                .containsMatchIn(script),
            "Expected compose = true"
        )
        assertTrue(
            Regex("""buildFeatures\s*\{[^}]*buildConfig\s*=\s*true""", RegexOption.DOT_MATCHES_ALL)
                .containsMatchIn(script),
            "Expected buildConfig = true"
        )
        assertTrue(
            Regex("""buildFeatures\s*\{[^}]*viewBinding\s*=\s*false""", RegexOption.DOT_MATCHES_ALL)
                .containsMatchIn(script),
            "Expected viewBinding = false"
        )
    }

    @Test
    @DisplayName("Compile options: Java 24 source and target compatibility")
    fun compileOptionsConfigured() {
        assertTrue(
                .containsMatchIn(script),
            "Expected sourceCompatibility = JavaVersion.VERSION_24"
        )
        assertTrue(
                .containsMatchIn(script),
            "Expected targetCompatibility = JavaVersion.VERSION_24"
        )
    }

    @Test
    @DisplayName("Tasks: cleanKspCache registered and preBuild dependsOn required tasks")
    fun tasksConfigured() {
        assertTrue(
            Regex("""tasks\.register<Delete>\("cleanKspCache"\)""").containsMatchIn(script),
            "Expected registration of cleanKspCache task"
        )
        assertTrue(
                .containsMatchIn(script),
            "Expected preBuild.dependsOn(\"cleanKspCache\")"
        )
        assertTrue(
                .containsMatchIn(script),
            "Expected preBuild.dependsOn(\":cleanApiGeneration\")"
        )
        assertTrue(
                .containsMatchIn(script),
            "Expected preBuild.dependsOn(\":openApiGenerate\")"
        )
    }

    @Test
    @DisplayName("Custom status task aegenesisAppStatus is present with expected prints")
    fun statusTaskPresent() {
        assertTrue(
            Regex("""tasks\.register\("aegenesisAppStatus"\)""").containsMatchIn(script),
            "Expected aegenesisAppStatus task"
        )
        val expectedSnippets = listOf(
            "📱 AEGENESIS APP MODULE STATUS",
            "Unified API Spec:",
            "KSP Mode:",
            "Target SDK: 36",
            "Min SDK: 33"
        )
        expectedSnippets.forEach { snippet ->
            assertTrue(script.contains(snippet), "Expected status output to include: $snippet")
        }
    }

    @Test
    @DisplayName("Cleanup tasks script is applied")
    fun cleanupTasksApplied() {
        assertTrue(
            Regex("""apply\(from\s*=\s*"cleanup-tasks\.gradle\.kts"\)""").containsMatchIn(script),
            "Expected apply(from = \"cleanup-tasks.gradle.kts\")"
        )
    }

    @Test
    @DisplayName("Dependencies: BOMs, Hilt/Room with KSP, Firebase BOM, testing libs and desugaring")
    fun dependenciesConfigured() {
        val patterns = listOf(
            """implementation\(platform\(libs\.androidx\.compose\.bom\)\)""",
            """implementation\(libs\.hilt\.android\)""",
            """ksp\(libs\.hilt\.compiler\)""",
            """implementation\(libs\.room\.runtime\)""",
            """implementation\(libs\.room\.ktx\)""",
            """ksp\(libs\.room\.compiler\)""",
            """implementation\(platform\(libs\.firebase\.bom\)\)""",
            """testImplementation\(libs\.bundles\.testing\)""",
            """testRuntimeOnly\(libs\.junit\.engine\)""",
            """coreLibraryDesugaring\(libs\.coreLibraryDesugaring\)"""
        )
        patterns.forEach { pat ->
            assertTrue(
                Regex(pat).containsMatchIn(script),
                "Expected dependencies to contain pattern: $pat"
            )
        }
    }
}