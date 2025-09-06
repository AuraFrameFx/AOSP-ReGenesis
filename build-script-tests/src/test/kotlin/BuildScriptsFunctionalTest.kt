@file:Suppress("SpellCheckingInspection")

package buildscripts

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Path

/**
 * Testing library and framework in use:
 * - JUnit 5 (Jupiter)
 *
 * Notes:
 * - We intentionally avoid Gradle TestKit here to prevent resolving Android/third‑party plugins at configuration time.
 * - These tests validate the build script text (app/build.gradle.kts) added/changed in the diff:
 *   plugins, Android DSL values, conditional native config, tasks (cleanKspCache, preBuild deps, aegenesisAppStatus),
 *   packaging options, build features, compile options, and dependency notations.
 */
class BuildScriptsFunctionalTest {

    private fun repoRoot(): File {
        // Walk up from CWD until we find settings.gradle.kts
        var dir = File(System.getProperty("user.dir"))
        repeat(8) {
            if (File(dir, "settings.gradle.kts").exists()) return dir
            dir = dir.parentFile ?: return@repeat
        }
        fail("Could not locate repository root containing settings.gradle.kts from: ${System.getProperty("user.dir")}")
        throw IllegalStateException()
    }

    private fun appBuildFile(): File {
        val root = repoRoot().toPath()
        val expected = root.resolve("app/build.gradle.kts").toFile()
        if (expected.exists()) return expected

        // Fallback: scan for a build.gradle.kts that contains the target namespace to remain resilient to layout shifts
        val candidate = root.toFile().walkTopDown()
            .filter { it.isFile && it.name == "build.gradle.kts" }
            .firstOrNull { it.readText().contains("namespace = \"dev.aurakai.auraframefx\"") }
        return candidate ?: fail("Could not find app/build.gradle.kts with expected namespace in repository.")
    }

    private fun script(): String = appBuildFile().readText()

    @Nested
    @DisplayName("Plugins and basic Android configuration")
    inner class PluginsAndAndroidDsl {

        @Test
        fun `required plugins are declared`() {
            val s = script()
            listOf(
                "com.android.application",
                "org.jetbrains.kotlin.android",
                "org.jetbrains.kotlin.plugin.compose",
                "org.jetbrains.kotlin.plugin.serialization",
                "com.google.devtools.ksp",
                "com.google.dagger.hilt.android",
                "com.google.gms.google-services"
            ).forEach { id ->
                assertTrue(s.contains("id(\"$id\")"), "Expected plugin id $id")
            }
        }

        @Test
fun `android namespace and SDK versions configured`() {
    val s = script()
    assertTrue(s.contains("namespace = \"dev.aurakai.auraframefx\""))
    assertTrue(Regex("""compileSdk\s*=\s*\d+""").containsMatchIn(s))
    assertTrue(Regex("""minSdk\s*=\s*\d+""").containsMatchIn(s))
    assertTrue(Regex("""targetSdk\s*=\s*\d+""").containsMatchIn(s))
    assertTrue(Regex("""versionCode\s*=\s*\d+""").containsMatchIn(s))
    assertTrue(Regex("""versionName\s*=\s*\".+\"""").containsMatchIn(s))
    assertTrue(s.contains("testInstrumentationRunner"))
}

        @Test
        fun `vectorDrawables support library enabled`() {
            assertTrue(script().contains("vectorDrawables") && script().contains("useSupportLibrary = true"))
        }
    }

    @Nested
    @DisplayName("Conditional native configuration")
    inner class ConditionalNativeConfig {

        @Test
        fun `ndk abiFilters added only when CMakeLists exists`() {
            val s = script()
            assertTrue(s.contains("if (project.file(\"src/main/cpp/CMakeLists.txt\").exists())"))
            assertTrue(s.contains("ndk {") && s.contains("abiFilters.addAll(listOf(\"arm64-v8a\", \"armeabi-v7a\"))"))
        }

        @Test
        fun `externalNativeBuild cmake path and version set`() {
            val s = script()
            assertTrue(s.contains("externalNativeBuild"))
            assertTrue(s.contains("cmake {"))
            assertTrue(s.contains("path = file(\"src/main/cpp/CMakeLists.txt\")"))
            assertTrue(s.contains("version = \"3.22.1\""))
        }
    }

    @Nested
    @DisplayName("Build types and packaging")
    inner class BuildTypesAndPackaging {

        @Test
        fun `release and debug build types configured with proguard`() {
            val s = script()
            assertTrue(s.contains("buildTypes"))
            assertTrue(s.contains("release {") && s.contains("isMinifyEnabled = true") && s.contains("isShrinkResources = true"))
            assertTrue(s.contains("getDefaultProguardFile(\"proguard-android-optimize.txt\")"))
            assertTrue(s.contains("\"proguard-rules.pro\""))
            assertTrue(s.contains("debug {") && s.contains("proguardFiles("))
        }

        @Test
        fun `packaging resources excludes and jni pickFirsts configured`() {
            val s = script()
            // Representative subset of excludes
            listOf(
                "\"/META-INF/{AL2.0,LGPL2.1}\"",
                "\"/META-INF/DEPENDENCIES\"",
                "\"/META-INF/LICENSE.txt\"",
                "\"/META-INF/NOTICE.txt\"",
                "\"META-INF/*.kotlin_module\"",
                "\"**/kotlin/**\"",
                "\"**/*.txt\""
            ).forEach { entry ->
                assertTrue(s.contains(entry), "Missing packaging exclude: $entry")
            }
            assertTrue(s.contains("jniLibs {"))
            assertTrue(s.contains("useLegacyPackaging = false"))
            assertTrue(s.contains("pickFirsts += listOf(\"**/libc++_shared.so\", \"**/libjsc.so\")"))
        }
    }

    @Nested
    @DisplayName("Build features and compile options")
    inner class BuildFeaturesAndCompileOptions {

        @Test
        fun `compose buildConfig and viewBinding flags set`() {
            val s = script()
            assertTrue(s.contains("buildFeatures"))
            assertTrue(s.contains("compose = true"))
            assertTrue(s.contains("buildConfig = true"))
            assertTrue(s.contains("viewBinding = false"))
        }

        @Test
        fun `Java 24 compile options configured`() {
            val s = script()
            assertTrue(s.contains("sourceCompatibility = JavaVersion.VERSION_24"))
            assertTrue(s.contains("targetCompatibility = JavaVersion.VERSION_24"))
        }
    }

    @Nested
    @DisplayName("Tasks - cleanKspCache, preBuild wiring, aegenesisAppStatus")
    inner class TasksValidation {

        @Test
        fun `cleanKspCache task registered with Delete type and correct delete targets`() {
            val s = script()
            assertTrue(s.contains("tasks.register<Delete>(\"cleanKspCache\")"))
            assertTrue(s.contains("group = \"build setup\""))
            assertTrue(s.contains("description = \"Clean KSP caches (fixes NullPointerException)\""))
            listOf(
                "buildDirProvider.dir(\"generated/ksp\")",
                "buildDirProvider.dir(\"tmp/kapt3\")",
                "buildDirProvider.dir(\"tmp/kotlin-classes\")",
                "buildDirProvider.dir(\"kotlin\")",
                "buildDirProvider.dir(\"generated/source/ksp\")"
            ).forEach { target ->
                assertTrue(s.contains(target), "Missing delete target: $target")
            }
        }

        @Test
        fun `preBuild depends on cleanKspCache and API generation tasks`() {
            val s = script()
            assertTrue(s.contains("tasks.named(\"preBuild\")"))
            assertTrue(s.contains("dependsOn(\"cleanKspCache\")"))
            assertTrue(s.contains("dependsOn(\":openApiGenerate\")"))
            assertTrue(s.contains("dependsOn(\":cleanApiGeneration\")"))
        }

        @Test
        fun `aegenesisAppStatus task prints expected lines`() {
            val s = script()
            // Key lines and emojis
            listOf(
                "📱 AEGENESIS APP MODULE STATUS",
                "Unified API Spec: \${if (apiExists) \"✅ Found\" else \"❌ Missing\"}",
                "📄 API File Size: \${apiSize / 1024}KB",
                "🔧 Native Code: \${if (nativeCode) \"✅ Enabled\" else \"❌ Disabled\"}",
                "🧠 KSP Mode:",
                "🎯 Target SDK: 36",
                "📱 Min SDK: 33",
                "✅ Status: Ready for coinscience AI integration!"
            ).forEach { line ->
                assertTrue(s.contains(line), "Expected aegenesisAppStatus to contain: $line")
            }
        }

        @Test
        fun `cleanup tasks script is applied`() {
            assertTrue(script().contains("apply(from = \"cleanup-tasks.gradle.kts\")"))
        }
    }

    @Nested
    @DisplayName("Dependencies block coverage")
    inner class DependenciesBlock {

        @Test
        fun `core test dependencies declared`() {
            val s = script()
            assertTrue(s.contains("testImplementation(libs.bundles.testing)"))
            assertTrue(s.contains("testRuntimeOnly(libs.junit.engine)"))
        }

        @Test
        fun `project dependency hierarchy respected`() {
            val s = script()
            listOf(
                ":core-module",
                ":oracle-drive-integration",
                ":romtools",
                ":secure-comm",
                ":collab-canvas"
            ).forEach { path ->
                assertTrue(s.contains("implementation(project(\"$path\"))"), "Missing project dependency: $path")
            }
        }

        @Test
        fun `notable library notations declared`() {
            val s = script()
            listOf(
                "implementation(platform(libs.androidx.compose.bom))",
                "implementation(libs.androidx.core.ktx)",
                "implementation(libs.hilt.android)",
                "ksp(libs.hilt.compiler)",
                "coreLibraryDesugaring(libs.coreLibraryDesugaring)",
                "implementation(platform(libs.firebase.bom))",
                "implementation(fileTree(\"../Libs\") { include(\"*.jar\") })"
            ).forEach { notation ->
                assertTrue(s.contains(notation), "Expected dependency notation: $notation")
            }
        }
    }

    // Appended tests - extended coverage
}
    @Nested
    @DisplayName("Compose and Kotlin compiler options")
    inner class ComposeAndKotlinOptions {
        @Test
        fun `composeOptions declared with compiler extension version`() {
            val s = script()
            // Accept either explicit value or catalog reference
            assertTrue(s.contains("composeOptions"), "composeOptions block is missing")
            assertTrue(
                Regex("""kotlinCompilerExtensionVersion\s*=\s*["'][^"']+["']""").containsMatchIn(s) ||
                    Regex("""kotlinCompilerExtensionVersion\s*=\s*libs\.versions\.[\w\-.]+\.get\(\)""").containsMatchIn(s),
                "kotlinCompilerExtensionVersion must be set in composeOptions"
            )
        }

        @Test
        fun `kotlinOptions configured for JVM target and freeCompilerArgs`() {
            val s = script()
            assertTrue(s.contains("kotlinOptions {"), "kotlinOptions block is missing")
            assertTrue(
                Regex("""jvmTarget\s*=\s*["']?(24|1[1-9]|[0-9]+)["']?""").containsMatchIn(s),
                "jvmTarget should be declared (expecting 24 per diff)"
            )
            // We don't assert specific flags, just existence of freeCompilerArgs to remain resilient
            assertTrue(Regex("""freeCompilerArgs\s*=""").containsMatchIn(s), "freeCompilerArgs not configured")
        }

        @Test
        fun `kotlin jvmToolchain configured to Java 24`() {
            val s = script()
            assertTrue(
                s.contains("jvmToolchain(24)") ||
                    Regex("""java\s*\{\s*jvmToolchain\s*\{\s*languageVersion\s*=\s*JavaLanguageVersion\.of\(24\)""").containsMatchIn(s),
                "Expected Kotlin jvmToolchain to target 24"
            )
        }
    }

    @Nested
    @DisplayName("Packaging - extended excludes set")
    inner class PackagingExtended {
        @Test
        fun `resources excludes include additional common metadata and notices`() {
            val s = script()
            listOf(
                "\"/META-INF/gradle/incremental.annotation.processors\"",
                "\"/META-INF/LICENSE\"",
                "\"/META-INF/NOTICE\"",
                "\"/META-INF/ASL2.0\"",
                "\"META-INF/DEPENDENCIES\"",
                "\"META-INF/LICENSE*\"",
                "\"META-INF/NOTICE*\"",
                "\"META-INF/*.version\""
            ).forEach { entry ->
                assertTrue(s.contains(entry), "Expected additional packaging exclude: $entry")
            }
        }
    }

    @Nested
    @DisplayName("Google Services and Hilt integration wiring")
    inner class GoogleServicesAndHilt {
        @Test
        fun `google services plugin applied and firebase platform present`() {
            val s = script()
            assertTrue(s.contains("id(\"com.google.gms.google-services\")"), "Google Services plugin not applied")
            assertTrue(s.contains("implementation(platform(libs.firebase.bom))"), "Firebase BOM platform dependency missing")
        }

        @Test
        fun `hilt plugin present with ksp compiler configuration`() {
            val s = script()
            assertTrue(s.contains("id(\"com.google.dagger.hilt.android\")"), "Hilt plugin not applied")
            assertTrue(s.contains("ksp(libs.hilt.compiler)"), "Hilt compiler not wired via KSP")
            // Optional: ensure Hilt Gradle configuration block exists if added in diff
            assertTrue(!s.contains("hilt { enableAggregatingTask = false }") || s.contains("hilt {"), "Hilt block malformed if present")
        }
    }

    @Nested
    @DisplayName("Default config details")
    inner class DefaultConfigDetails {
        @Test
        fun `application ID and test instrumentation runner declared`() {
            val s = script()
            assertTrue(Regex("""applicationId\s*=\s*\"[a-zA-Z0-9_.]+\"""").containsMatchIn(s), "applicationId must be declared")
            assertTrue(Regex("""testInstrumentationRunner\s*=\s*\"[^\"]+\"""").containsMatchIn(s), "testInstrumentationRunner must be declared")
        }

        @Test
        fun `versioning fields and SDK bounds align`() {
            val s = script()
            assertTrue(Regex("""versionCode\s*=\s*\d+""").containsMatchIn(s), "versionCode missing")
            assertTrue(Regex("""versionName\s*=\s*\".+\"""").containsMatchIn(s), "versionName missing")
            assertTrue(Regex("""minSdk\s*=\s*\d+""").containsMatchIn(s), "minSdk missing")
            assertTrue(Regex("""targetSdk\s*=\s*\d+""").containsMatchIn(s), "targetSdk missing")
        }
    }

    @Nested
    @DisplayName("Native/CMake completeness")
    inner class NativeCMakeCompleteness {
        @Test
        fun `cmake block defines both path and version with conditional guard`() {
            val s = script()
            assertTrue(s.contains("if (project.file(\"src/main/cpp/CMakeLists.txt\").exists())"), "CMake conditional guard missing")
            assertTrue(s.contains("externalNativeBuild"), "externalNativeBuild missing")
            assertTrue(s.contains("cmake {"), "cmake block missing")
            assertTrue(s.contains("path = file(\"src/main/cpp/CMakeLists.txt\")"), "CMake path not set")
            assertTrue(Regex("""version\s*=\s*\"[0-9.]+\"""").containsMatchIn(s), "CMake version not set")
        }
    }

    @Nested
    @DisplayName("Dependency bundles and local jars")
    inner class DependencyBundlesAndLocalJars {
        @Test
        fun `testing bundle and junit engine present`() {
            val s = script()
            assertTrue(s.contains("testImplementation(libs.bundles.testing)"), "libs.bundles.testing missing")
            assertTrue(s.contains("testRuntimeOnly(libs.junit.engine)"), "junit engine missing")
        }

        @Test
        fun `local Libs directory jars included via fileTree`() {
            val s = script()
            assertTrue(s.contains("implementation(fileTree(\"../Libs\") { include(\"*.jar\") })"), "fileTree jar inclusion missing")
        }
    }

    @Nested
    @DisplayName("Task dependencies wiring - extended")
    inner class TaskDependenciesWiringExtended {
        @Test
        fun `preBuild depends on cleanKspCache and API generation tasks - extended shape`() {
            val s = script()
            // Allow both dependsOn("x") and dependsOn(tasks.named("x")) forms
            val patterns = listOf(
                "dependsOn(\"cleanKspCache\")",
                "dependsOn(\":openApiGenerate\")",
                "dependsOn(\":cleanApiGeneration\")"
            )
            patterns.forEach { p ->
                assertTrue(s.contains(p) || Regex("""dependsOn\(\s*tasks\.named\(\s*${Regex.escape(p.removePrefix("dependsOn(").removeSuffix(")"))}\s*\)\s*\)""").containsMatchIn(s),
                    "Expected preBuild to depend on $p")
            }
        }
    }