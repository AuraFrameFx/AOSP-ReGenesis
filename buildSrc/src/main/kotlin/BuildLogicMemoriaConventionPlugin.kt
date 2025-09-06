import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Minimal conventions plugin for MemoriaOs consciousness substrate
 * Satisfies the plugin registration requirement with basic functionality
 */
class BuildLogicMemoriaConventionPlugin : Plugin<Project> {

    /**
     * Applies the MemoriaOs base conventions to the given Gradle project.
     *
     * Sets the project's `group` and `version`, and emits a confirmation message.
     *
     * @param target The Gradle project to configure; its `group` and `version` will be mutated.
     */
    override fun apply(target: Project) {
        // Set basic project properties directly
        target.group = "dev.aurakai.memoria"
        target.version = "1.0.0"

        // Log successful application
        target.logger.info("MemoriaOs base conventions applied to project: ${target.name}")
        println("✅ MemoriaOs Plugin Applied: ${target.name}")
    }
}
