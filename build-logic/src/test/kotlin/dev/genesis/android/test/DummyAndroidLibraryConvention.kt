package dev.genesis.android.test

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Dummy replacement for "genesis.android.library" in tests.
 * Registers a fake 'android' LibraryExtension so the compose convention can configure it.
 */
class DummyAndroidLibraryConvention : Plugin<Project> {
    /**
     * Ensures a test-friendly Android library extension exists on the target project.
     *
     * If the project does not already have a `LibraryExtension`, this registers a new
     * extension named `"android"` backed by a `FakeLibraryExtension` so tests and
     * conventions that expect an Android library extension can run without the real
     * Android Gradle plugin applied.
     */
    override fun apply(target: Project) {
        if (target.extensions.findByType(LibraryExtension::class.java) == null) {
            target.extensions.add(LibraryExtension::class.java, "android", FakeLibraryExtension())
        }
    }
}