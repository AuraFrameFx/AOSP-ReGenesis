// Gradle cache clearing task
tasks.register("clearGradleCache") {
    group = "build"
    description = "Clear all Gradle caches and dependencies"

    doLast {
        // Clear Gradle daemon
        exec {
            commandLine("gradlew", "--stop")
        }

        // Clear build directories
        project.allprojects.forEach { proj ->
            proj.layout.buildDirectory.asFile.get().deleteRecursively()
        }

        // Clear Gradle cache directories
        val gradleUserHome = System.getProperty("gradle.user.home") ?: "${System.getProperty("user.home")}/.gradle"
        val cacheDir = File(gradleUserHome, "caches")
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        }

        println("✅ Gradle cache cleared successfully")
        println("🔄 Please run 'gradlew clean build' to rebuild with fresh dependencies")
    }
}

