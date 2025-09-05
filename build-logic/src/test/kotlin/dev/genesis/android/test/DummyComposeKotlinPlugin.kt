package dev.genesis.android.test

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Dummy stand-in for "org.jetbrains.kotlin.plugin.compose" for unit tests.
 */
class DummyComposeKotlinPlugin : Plugin<Project> {
    /**
 * No-op implementation of Plugin.apply for the test dummy compose plugin.
 *
 * This method intentionally performs no actions so the class can act as a stand-in
 * for `org.jetbrains.kotlin.plugin.compose` in unit tests without affecting the target project.
 *
 * @param target The Gradle project to which the plugin is applied; ignored by this implementation.
 */
override fun apply(target: Project) { /* no-op */ }
}