package dev.genesis.android

import com.android.build.api.dsl.LibraryExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests for AndroidComposeConventionPlugin.
 *
 * Testing library/framework: JUnit 5 (Jupiter).
 * Approach:
 *  - Use ProjectBuilder for unit tests.
 *  - Provide test-only stubs and plugin markers so pluginManager.apply(...) succeeds.
 *
 * Validates PR diff concerns:
 *  - Applies base "genesis.android.library"
 *  - Applies "org.jetbrains.kotlin.plugin.compose"
 *  - Enables android.buildFeatures.compose = true
 *  - Idempotency
 *  - Applying via plugin id "genesis.android.compose" also works
 *  - Descriptor maps "genesis.android.compose" -> implementation class
 */
class AndroidComposeConventionPluginTest {

    private fun newProject() = ProjectBuilder.builder()
        .withName("compose-convention-test")
        .build()

    @Test
    @DisplayName("apply(): succeeds on fresh project and is idempotent")
    fun apply_isSuccessful_andIdempotent() {
        val project = newProject()
        val plugin = AndroidComposeConventionPlugin()

        assertDoesNotThrow { plugin.apply(project) }
        assertDoesNotThrow { plugin.apply(project) } // re-apply safely
    }

    @Test
    @DisplayName("apply(): applies base 'genesis.android.library' and Kotlin Compose plugin ids")
    fun apply_appliesExpectedPluginIds() {
        val project = newProject()
        AndroidComposeConventionPlugin().apply(project)

        assertTrue(
            project.pluginManager.hasPlugin("genesis.android.library"),
            "Expected base convention plugin 'genesis.android.library' to be applied"
        )
        assertTrue(
            project.pluginManager.hasPlugin("org.jetbrains.kotlin.plugin.compose"),
            "Expected 'org.jetbrains.kotlin.plugin.compose' to be applied"
        )
    }

    @Test
    @DisplayName("compose build feature is enabled on LibraryExtension")
    fun composeBuildFeature_enabled() {
        val project = newProject()
        AndroidComposeConventionPlugin().apply(project)

        val libExt = project.extensions.findByType(LibraryExtension::class.java)
        assertNotNull(libExt, "LibraryExtension should be registered by the base convention")

        assertTrue(
            libExt!!.buildFeatures.compose,
            "Expected android.buildFeatures.compose to be true after applying the compose convention"
        )
    }

    @Test
    @DisplayName("Applying plugin by id 'genesis.android.compose' configures Compose and applies Kotlin Compose plugin")
    fun applyById_configuresCompose() {
        val project = newProject()

        assertDoesNotThrow {
            project.pluginManager.apply("genesis.android.compose")
        }

        val libExt = project.extensions.findByType(LibraryExtension::class.java)
        assertNotNull(libExt, "LibraryExtension should be present after applying plugin by id")

        assertAll(
            { assertTrue(libExt!!.buildFeatures.compose, "compose should be enabled") },
            { assertTrue(project.pluginManager.hasPlugin("org.jetbrains.kotlin.plugin.compose"), "Kotlin Compose plugin should be applied") },
            { assertTrue(project.pluginManager.hasPlugin("genesis.android.library"), "Base convention should be applied") }
        )
    }

    @Test
    @DisplayName("When base plugin is applied first, compose is false by default and becomes true after compose convention")
    fun baseFirst_thenComposeConvention_enablesCompose() {
        val project = newProject()
        // Simulate consumer applying base convention first
        project.pluginManager.apply("genesis.android.library")

        val libExt = project.extensions.findByType(LibraryExtension::class.java)
        assertNotNull(libExt, "LibraryExtension should be present after base convention")
        assertFalse(libExt!!.buildFeatures.compose, "compose should be disabled by default before compose convention")

        AndroidComposeConventionPlugin().apply(project)
        assertTrue(libExt.buildFeatures.compose, "compose should be enabled after compose convention")
    }

    @Nested
    @DisplayName("Plugin descriptor correctness")
    inner class DescriptorTests {

        @Test
        @DisplayName("META-INF mapping for 'genesis.android.compose' points to implementation class")
        fun descriptor_pointsToImplementation() {
            val classLoader = this::class.java.classLoader
            val candidates = listOf(
                "META-INF/gradle-plugins/genesis.android.compose.properties",
                "META-INF/gradle-plugins/genesis.android.compose.gradle-plugin"
            )

            val url = candidates.asSequence()
                .mapNotNull { classLoader.getResource(it) }
                .firstOrNull()

            assertNotNull(url, "Expected plugin descriptor for 'genesis.android.compose' to exist in resources")

            val text = url!!.readText()
            assertTrue(
                text.contains("implementation-class=dev.genesis.android.AndroidComposeConventionPlugin"),
                "Descriptor should map to dev.genesis.android.AndroidComposeConventionPlugin"
            )
        }
    }
}