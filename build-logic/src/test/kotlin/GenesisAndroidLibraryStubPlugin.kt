package test.stubs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

class GenesisAndroidLibraryStubPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Provide a dummy cleanGeneratedSources task so the convention plugin can enhance it.
            // Use Delete task type to allow safe execution in tests.
            tasks.register("cleanGeneratedSources", Delete::class.java) {
                it.group = "verification"
                // No default delete targets here; the convention plugin will add its own deletions.
            }
            // NOTE: We intentionally do NOT add Android Library extensions to keep the test lightweight.
            // The convention plugin only configures LibraryExtension when CMakeLists exists at apply-time,
            // and our tests control that timing to avoid requiring AGP.
        }
    }
}