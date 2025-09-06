package dev.genesis.android.test

import com.android.build.api.dsl.BuildFeatures
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Action

class FakeLibraryExtension : LibraryExtension {
    private val features = object : BuildFeatures {
        override var compose: Boolean = false
    }

    override val buildFeatures: BuildFeatures
        get() = features

    /**
     * Applies the given Action to this extension's internal BuildFeatures instance.
     *
     * Executes the provided [action] with the backing BuildFeatures object so callers (typically tests) can configure feature flags such as `compose`.
     *
     * @param action Action to execute against the internal BuildFeatures instance.
     */
    override fun buildFeatures(action: Action<BuildFeatures>) {
        action.execute(features)
    }
}