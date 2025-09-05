package dev.genesis.android.test

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Dummy replacement for "genesis.android.library" in tests.
 * Registers a fake 'android' LibraryExtension so the compose convention can configure it.
 */
class DummyAndroidLibraryConvention : Plugin<Project> {
    override fun apply(target: Project) {
        if (target.extensions.findByType(LibraryExtension::class.java) == null) {
            target.extensions.add(LibraryExtension::class.java, "android", FakeLibraryExtension())
        }
    }
}