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
     * Applies the given [Action] to the internal [BuildFeatures] instance.
     *
     * @param action Action that will be executed with the fake extension's `buildFeatures` instance.
     */
    override fun buildFeatures(action: Action<BuildFeatures>) {
        action.execute(features)
    }
}