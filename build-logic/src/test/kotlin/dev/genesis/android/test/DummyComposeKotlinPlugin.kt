package dev.genesis.android.test

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Dummy stand-in for "org.jetbrains.kotlin.plugin.compose" for unit tests.
 */
class DummyComposeKotlinPlugin : Plugin<Project> {
    /**
 * No-op apply implementation for the dummy Compose Kotlin plugin used in unit tests.
 *
 * Intentionally performs no actions when applied so tests can substitute the real
 * "org.jetbrains.kotlin.plugin.compose" plugin without changing the Project.
 *
 * @param target The Gradle Project to which the plugin is applied; ignored by this implementation.
 */
override fun apply(target: Project) { /* no-op */ }
}