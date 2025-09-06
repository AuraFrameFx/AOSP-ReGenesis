package dev.aurakai.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

abstract class VerifyRomToolsTask : DefaultTask() {
    @get:InputDirectory
    @get:Optional
    abstract val romToolsDir: DirectoryProperty

    /**
     * Verifies that the optional `romToolsDir` input directory exists and logs the result.
     *
     * Checks the task's `romToolsDir` DirectoryProperty (if provided). If the directory is missing or does not exist,
     * logs a warning indicating ROM-related functionality may be limited. If the directory exists, logs a lifecycle
     * message with the directory's absolute path.
     */
    @TaskAction
    fun verify() {
        val dir = romToolsDir.orNull?.asFile
        if (dir?.exists() != true) {
            logger.warn("⚠️  ROM tools directory not found - ROM functionality may be limited")
        } else {
            logger.lifecycle("✅ ROM tools verified and ready: ${dir.absolutePath}")
        }
    }
}
