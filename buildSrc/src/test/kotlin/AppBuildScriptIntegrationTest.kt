package buildsrc

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.io.File
import java.nio.file.Path
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.writeText

class AppBuildScriptIntegrationTest {

    // Locate app/build.gradle.kts that contains the namespace marker from the diff.
    private fun locateBuildScript(): File {
        val marker = "namespace = \"dev.aurakai.auraframefx\""
        val candidates = sequenceOf(
            File("app/build.gradle.kts"),
            File("build.gradle.kts"),
            File("application/build.gradle.kts"),
            File("mobile/build.gradle.kts")
        ).filter { it.exists() && it.isFile && it.readText().contains(marker) }.toList()
        if (candidates.isNotEmpty()) return candidates.first()

        // Fallback: search all *.gradle.kts files
        val matches = Files.walk(File(".").toPath())
            .filter { it.toString().endsWith(".gradle.kts") }
            .map { it.toFile() }
            .filter { it.isFile && it.readText().contains(marker) }
            .toList()
        return matches.firstOrNull()
            ?: error("Could not find build.gradle.kts containing '$marker'")
    }

    private fun text(): String = locateBuildScript().readText()

    @Test
    @DisplayName("Plugins block includes Android, Kotlin, Compose, Serialization, KSP, Hilt, Google Services")
    fun plugins_present() {
        val t = text()
        listOf(
            "id(\"com.android.application\")",
            "id(\"org.jetbrains.kotlin.android\")",
            "id(\"org.jetbrains.kotlin.plugin.compose\")",
            "id(\"org.jetbrains.kotlin.plugin.serialization\")",
            "id(\"com.google.devtools.ksp\")",
            "id(\"com.google.dagger.hilt.android\")",
            "id(\"com.google.gms.google-services\")"
        ).forEach { line ->
            assertTrue(t.contains(line), "Missing plugin: $line")
        }
    }

    @Test
    @DisplayName("Android DSL: namespace, SDKs, versioning, test runner, vector drawables")
    fun android_core_config() {
        val t = text()
        assertTrue(t.contains("namespace = \"dev.aurakai.auraframefx\""), "namespace mismatch")
        assertTrue(Regex("""compileSdk\s*=\s*36""").containsMatchIn(t), "compileSdk should be 36")
        assertTrue(Regex("""minSdk\s*=\s*33""").containsMatchIn(t), "minSdk should be 33")
        assertTrue(Regex("""targetSdk\s*=\s*36""").containsMatchIn(t), "targetSdk should be 36")
        assertTrue(t.contains("applicationId = \"dev.aurakai.auraframefx\""), "applicationId mismatch")
        assertTrue(t.contains("versionCode = 1"), "versionCode should be 1")
        assertTrue(t.contains("versionName = \"1.0.0-genesis-alpha\""), "versionName mismatch")
        assertTrue(t.contains("testInstrumentationRunner = \"androidx.test.runner.AndroidJUnitRunner\""), "Missing testInstrumentationRunner")
        assertTrue(Regex("""vectorDrawables\s*\{[^}]*useSupportLibrary\s*=\s*true""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "vectorDrawables.useSupportLibrary should be true")
    }

    @Test
    @DisplayName("Conditional native config guarded by CMakeLists existence; externalNativeBuild uses CMake 3.22.1")
    fun native_config_conditional() {
        val t = text()
        assertTrue(t.contains("if (project.file(\"src/main/cpp/CMakeLists.txt\").exists())"), "Missing native existence guard")
        assertTrue(Regex("""ndk\s*\{[^}]*abiFilters\.addAll""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t), "Missing ndk.abiFilters.addAll")
        assertTrue(Regex("""externalNativeBuild\s*\{[^}]*cmake\s*\{[^}]*version\s*=\s*\"3\.22\.1\"""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "CMake version should be 3.22.1")
    }

    @Test
    @DisplayName("buildTypes configured: release minify+shrink; debug has proguardFiles")
    fun build_types() {
        val t = text()
        assertTrue(Regex("""buildTypes\s*\{[^}]*release\s*\{[^}]*isMinifyEnabled\s*=\s*true""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "release.isMinifyEnabled should be true")
        assertTrue(Regex("""release\s*\{[^}]*isShrinkResources\s*=\s*true""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "release.isShrinkResources should be true")
        assertTrue(Regex("""release\s*\{[^}]*proguardFiles\(""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "release should declare proguardFiles")
        assertTrue(Regex("""debug\s*\{[^}]*proguardFiles\(""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "debug should declare proguardFiles")
    }

    @Test
    @DisplayName("Packaging: resources excludes and jniLibs settings/pickFirsts")
    fun packaging() {
        val t = text()
        listOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/DEPENDENCIES",
            "/META-INF/LICENSE",
            "/META-INF/LICENSE.txt",
            "/META-INF/NOTICE",
            "/META-INF/NOTICE.txt",
            "META-INF/*.kotlin_module",
            "**/kotlin/**",
            "**/*.txt"
        ).forEach { ex ->
            assertTrue(t.contains(ex), "Missing resource exclude: $ex")
        }
        assertTrue(Regex("""jniLibs\s*\{[^}]*useLegacyPackaging\s*=\s*false""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "jniLibs.useLegacyPackaging should be false")
        assertTrue(t.contains("pickFirsts += listOf(\"**/libc++_shared.so\", \"**/libjsc.so\")"),
            "jniLibs.pickFirsts should include libc++_shared.so and libjsc.so")
    }

    @Test
    @DisplayName("buildFeatures: compose true, buildConfig true, viewBinding false")
    fun build_features() {
        val t = text()
        assertTrue(Regex("""buildFeatures\s*\{[^}]*compose\s*=\s*true""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "compose should be enabled")
        assertTrue(Regex("""buildFeatures\s*\{[^}]*buildConfig\s*=\s*true""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "buildConfig should be enabled")
        assertTrue(Regex("""buildFeatures\s*\{[^}]*viewBinding\s*=\s*false""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "viewBinding should be disabled")
    }

    @Test
    @DisplayName("compileOptions: JavaVersion.VERSION_24 for source and target")
    fun compile_options_java24() {
        val t = text()
        assertTrue(t.contains("sourceCompatibility = JavaVersion.VERSION_24"), "sourceCompatibility should be JavaVersion.VERSION_24")
        assertTrue(t.contains("targetCompatibility = JavaVersion.VERSION_24"), "targetCompatibility should be JavaVersion.VERSION_24")
    }

    @Test
    @DisplayName("Task: cleanKspCache registered and deletes expected build directories")
    fun clean_ksp_cache_task() {
        val t = text()
        assertTrue(t.contains("tasks.register<Delete>(\"cleanKspCache\")"), "cleanKspCache task not registered as Delete")
        listOf("generated/ksp","tmp/kapt3","tmp/kotlin-classes","kotlin","generated/source/ksp").forEach { path ->
            assertTrue(t.contains(path), "cleanKspCache should delete: $path")
        }
    }

    @Test
    @DisplayName("preBuild dependsOn cleanKspCache, :cleanApiGeneration, :openApiGenerate")
    fun prebuild_dependencies() {
        val t = text()
        assertTrue(Regex("""tasks\.named\("preBuild"\)\s*\{[^}]*dependsOn\("cleanKspCache"\)""", RegexOption.DOT_MATCHES_ALL).containsMatchIn(t),
            "preBuild should depend on cleanKspCache")
        assertTrue(t.contains("dependsOn(\":cleanApiGeneration\")"), "preBuild should depend on :cleanApiGeneration")
        assertTrue(t.contains("dependsOn(\":openApiGenerate\")"), "preBuild should depend on :openApiGenerate")
    }

    @Test
    @DisplayName("AeGenesis status task exists and includes expected prints")
    fun aegenesis_status_task() {
        val t = text()
        assertTrue(t.contains("tasks.register(\"aegenesisAppStatus\")"), "aegenesisAppStatus task not registered")
        listOf(
            "AEGENESIS APP MODULE STATUS",
            "Unified API Spec",
            "API File Size",
            "Native Code",
            "KSP Mode",
            "Target SDK: 36",
            "Min SDK: 33",
            "Ready for coinscience AI integration!"
        ).forEach { line ->
            assertTrue(t.contains(line), "aegenesisAppStatus should print: $line")
        }
    }

    @Test
    @DisplayName("Cleanup tasks script is applied")
    fun cleanup_tasks_applied() {
        assertTrue(text().contains("apply(from = \"cleanup-tasks.gradle.kts\")"),
            "cleanup-tasks.gradle.kts should be applied")
    }

    @Test
    @DisplayName("Dependencies include key modules, libraries, KSP processors, and test stacks")
    fun dependencies_declared() {
        val t = text()
        // BOMs and modules
        assertTrue(t.contains("implementation(platform(libs.androidx.compose.bom))"), "Missing Compose BOM platform")
        listOf(":core-module", ":oracle-drive-integration", ":romtools", ":secure-comm", ":collab-canvas").forEach { m ->
            assertTrue(t.contains("implementation(project(\"$m\"))"), "Missing module dependency: $m")
        }
        // Hilt + Room with KSP
        assertTrue(t.contains("implementation(libs.hilt.android)"), "Missing Hilt runtime")
        assertTrue(t.contains("ksp(libs.hilt.compiler)"), "Missing Hilt KSP compiler")
        assertTrue(t.contains("implementation(libs.room.runtime)"), "Missing Room runtime")
        assertTrue(t.contains("ksp(libs.room.compiler)"), "Missing Room KSP compiler")
        // Core library desugaring
        assertTrue(t.contains("coreLibraryDesugaring(libs.coreLibraryDesugaring)"), "Missing coreLibraryDesugaring")
        // Firebase BOM and bundle
        assertTrue(t.contains("implementation(platform(libs.firebase.bom))"), "Missing Firebase BOM platform")
        assertTrue(t.contains("implementation(libs.bundles.firebase)"), "Missing Firebase bundle")
        // Xposed + Yuki KSP
        assertTrue(t.contains("implementation(libs.bundles.xposed)"), "Missing Xposed bundle")
        assertTrue(t.contains("ksp(libs.yuki.ksp.xposed)"), "Missing Yuki Xposed KSP")
        // Local JARs
        assertTrue(t.contains("implementation(fileTree(\"../Libs\") { include(\"*.jar\") })"), "Missing local JARs fileTree include")
        // Debug tools
        assertTrue(t.contains("debugImplementation(libs.leakcanary.android)"), "Missing LeakCanary debugImplementation")
        assertTrue(t.contains("debugImplementation(libs.androidx.compose.ui.tooling)"), "Missing compose tooling debugImplementation")
        assertTrue(t.contains("debugImplementation(libs.androidx.compose.ui.test.manifest)"), "Missing compose test manifest debugImplementation")
        // Unit test stack
        assertTrue(t.contains("testImplementation(libs.bundles.testing)"), "Missing testImplementation testing bundle")
        assertTrue(t.contains("testRuntimeOnly(libs.junit.engine)"), "Missing testRuntimeOnly junit engine")
        // Android test stack
        assertTrue(t.contains("androidTestImplementation(libs.androidx.test.ext.junit)"), "Missing androidTest ext junit")
        assertTrue(t.contains("androidTestImplementation(libs.androidx.test.core)"), "Missing androidTest core")
        assertTrue(t.contains("androidTestImplementation(platform(libs.androidx.compose.bom))"), "Missing androidTest compose BOM platform")
        assertTrue(t.contains("androidTestImplementation(libs.androidx.compose.ui.test.junit4)"), "Missing androidTest compose ui test")
        assertTrue(t.contains("androidTestImplementation(libs.hilt.android.testing)"), "Missing androidTest Hilt testing")
        assertTrue(t.contains("kspAndroidTest(libs.hilt.compiler)"), "Missing kspAndroidTest hilt compiler")
    }

    // Optional dynamic checks that execute Gradle. Guarded by -DenableTestKit=true to avoid CI issues.
    @Test
    @EnabledIfSystemProperty(named = "enableTestKit", matches = "true")
    @DisplayName("Dynamic: aegenesisAppStatus prints expected lines when files and props are present")
    fun aegenesis_status_dynamic(@TempDir tmp: Path) {
        val projectDir = tmp
        projectDir.resolve("api").createDirectories()
        projectDir.resolve("src/main/cpp").createDirectories()

        // settings.gradle.kts with repos
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
            """.trimIndent()
        )

        // Copy the target build script content into the temp project
        val target = locateBuildScript().readText()
        projectDir.resolve("build.gradle.kts").writeText(target)

        // Create API file (to show Found + size)
        projectDir.resolve("api/unified-aegenesis-api.yml").writeText("# api\npaths: {}\n")
        // Create CMakeLists to enable native branch
        projectDir.resolve("src/main/cpp/CMakeLists.txt").writeText("# cmake")

        // Execute
        val pb = ProcessBuilder(listOf("gradle", "-q", "aegenesisAppStatus", "-Pksp.useKSP2=true"))
            .directory(projectDir.toFile())
            .redirectErrorStream(true)
        val proc = pb.start()
        val out = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()

        assertTrue(code == 0, "Gradle exited with $code\n$out")
        listOf(
            "AEGENESIS APP MODULE STATUS",
            "Unified API Spec: ✅ Found",
            "API File Size:",
            "Native Code: ✅ Enabled",
            "KSP Mode: true",
            "Target SDK: 36",
            "Min SDK: 33",
            "Ready for coinscience AI integration!"
        ).forEach { line ->
            assertTrue(out.contains(line), "Missing output line: $line\n$out")
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "enableTestKit", matches = "true")
    @DisplayName("Dynamic: cleanKspCache deletes expected directories under build/")
    fun clean_ksp_cache_dynamic(@TempDir tmp: Path) {
        val projectDir = tmp
        // Minimal settings
        projectDir.resolve("settings.gradle.kts").writeText(
            "pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }"
        )
        // Build file
        val target = locateBuildScript().readText()
        projectDir.resolve("build.gradle.kts").writeText(target)

        // Pre-create directories
        projectDir.resolve("build").createDirectories()
        listOf("generated/ksp","tmp/kapt3","tmp/kotlin-classes","kotlin","generated/source/ksp").forEach { rel ->
            val p = projectDir.resolve("build").resolve(rel)
            p.parent?.createDirectories()
            p.createDirectories()
            assertTrue(p.exists() && p.isDirectory(), "Failed to create dir: $rel")
        }

        val pb = ProcessBuilder(listOf("gradle", "-q", "cleanKspCache"))
            .directory(projectDir.toFile())
            .redirectErrorStream(true)
        val proc = pb.start()
        val out = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        assertTrue(code == 0, "Gradle exited with $code\n$out")

        listOf("generated/ksp","tmp/kapt3","tmp/kotlin-classes","kotlin","generated/source/ksp").forEach { rel ->
            val p = projectDir.resolve("build").resolve(rel)
            assertTrue(!p.exists(), "Directory not deleted by cleanKspCache: $rel")
        }
    }
}