package test.stubs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

class GenesisAndroidLibraryStubPlugin : Plugin<Project> {
    /**
     * Applies the plugin to the given Gradle project.
     *
     * Registers a no-op `Delete` task named `cleanGeneratedSources` (grouped under "verification")
     * so a separate convention plugin can locate and enhance it later. The task is created
     * without any default delete targets to allow the convention plugin to add deletions.
     *
     * Intentionally does not add Android Library extensions; LibraryExtension configuration is
     * deferred and performed by the convention plugin only when a CMakeLists file exists at
     * apply-time to keep tests lightweight and avoid requiring AGP.
     */
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