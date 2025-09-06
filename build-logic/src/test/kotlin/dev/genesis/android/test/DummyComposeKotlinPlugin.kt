package dev.genesis.android.test

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Dummy stand-in for "org.jetbrains.kotlin.plugin.compose" for unit tests.
 */
class DummyComposeKotlinPlugin : Plugin<Project> {
    /**
 * Dummy no-op implementation of Plugin.apply used in tests as a stand-in for
 * "org.jetbrains.kotlin.plugin.compose".
 *
 * Applying this plugin has no effect on the Project (the target is intentionally ignored).
 */
override fun apply(target: Project) { /* no-op */ }
}