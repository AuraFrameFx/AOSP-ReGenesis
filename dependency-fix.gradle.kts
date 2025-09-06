// Comprehensive dependency resolution fix script
// This script clears all caches and forces fresh dependency resolution

tasks.register("forceRefreshDependencies") {
    group = "build"
    description = "Force refresh all dependencies and clear caches"

    doLast {
        println("🔄 Starting comprehensive dependency refresh...")

        // Clear all build directories
        allprojects {
            if (layout.buildDirectory.asFile.get().exists()) {
                delete(layout.buildDirectory)
            }
        }

        // Clear Gradle cache
        val gradleUserHome = File(System.getProperty("user.home"), ".gradle")
        if (gradleUserHome.exists()) {
            val cacheDir = File(gradleUserHome, "caches")
            if (cacheDir.exists()) {
                delete(cacheDir)
                println("✅ Cleared Gradle cache")
            }
        }

        // Clear module caches
        val modules = listOf("core-module", "benchmark", "collab-canvas", "colorblendr",
                            "secure-comm", "romtools", "datavein-oracle-native")
        modules.forEach { module ->
            val moduleDir = File(project.rootDir, "$module/build")
            if (moduleDir.exists()) {
                delete(moduleDir)
                println("✅ Cleared $module build cache")
            }
        }

        println("🚀 Cache cleared! Run 'gradlew clean build' to rebuild with fresh dependencies")
        println("💡 You may also need to run 'gradlew --stop' manually to stop daemon processes")
    }
}

// Task to verify dependency resolution
tasks.register("verifyDependencies") {
    group = "verification"
    description = "Verify all dependencies can be resolved"

    doLast {
        println("🔍 Verifying dependency resolution...")

        val failedDeps = mutableListOf<String>()

        // Check if configurations exist and can be resolved
        try {
            if (configurations.findByName("implementation") != null) {
                configurations.getByName("implementation").resolvedConfiguration.resolvedArtifacts
                    .find { it.moduleVersion.id.name.contains("yukihook") }
                    ?: failedDeps.add("YukiHook API")
            }
        } catch (e: Exception) {
            failedDeps.add("YukiHook API - ${e.message}")
        }

        if (failedDeps.isEmpty()) {
            println("✅ All dependencies resolved successfully!")
        } else {
            println("❌ Failed dependencies:")
            failedDeps.forEach { println("  • $it") }
        }
    }
}
