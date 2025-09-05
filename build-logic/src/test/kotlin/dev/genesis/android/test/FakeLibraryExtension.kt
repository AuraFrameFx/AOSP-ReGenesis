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

    override fun buildFeatures(action: Action<BuildFeatures>) {
        action.execute(features)
    }
}