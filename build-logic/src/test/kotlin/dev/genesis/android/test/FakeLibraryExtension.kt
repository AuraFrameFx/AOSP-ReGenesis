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
     * Applies the given [Action] to the extension's internal [BuildFeatures] instance.
     *
     * Typically used in tests to configure feature flags (for example, `compose`) on the backing
     * BuildFeatures object.
     *
     * @param action Action to apply to the internal BuildFeatures instance.
     */
    override fun buildFeatures(action: Action<BuildFeatures>) {
        action.execute(features)
    }
}