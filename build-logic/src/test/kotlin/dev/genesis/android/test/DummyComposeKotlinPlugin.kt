package dev.genesis.android.test

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Dummy stand-in for "org.jetbrains.kotlin.plugin.compose" for unit tests.
 */
class DummyComposeKotlinPlugin : Plugin<Project> {
    override fun apply(target: Project) { /* no-op */ }
}