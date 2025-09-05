// ===== GENESIS PROTOCOL - ROOT BUILD =====
// Multi-module project root - no plugins needed here
// Plugins are managed through:
// 1. settings.gradle.kts (pluginManagement)
// 2. buildSrc (for convention plugins)
// 3. Individual module build.gradle.kts files

// Project information
extra["projectName"] = "MemoriaOS"
extra["projectGroup"] = "dev.aurakai.memoria"
extra["projectVersion"] = "1.0.0"

// Custom tasks for project management
tasks.register("projectInfo") {
    group = "help"
    description = "Display project information"

    doLast {
        val projectName: String by project.extra
        val projectVersion: String by project.extra

        println("\n🛠️  $projectName v$projectVersion")
        println("==================================================")
        println("🏗️  Build System: Gradle ${gradle.gradleVersion}")
        println("🔧 Kotlin: ${libs.versions.kotlin.get()}")
        println("🤖 AGP: ${libs.versions.agp.get()}")
        println("\n📦 Modules (${subprojects.size}):")
        subprojects.forEach { println("  • ${it.name}") }
        println("\n🚀 Available Tasks: gradle tasks --group=build")
        println("==================================================")
    }
}

tasks.register<Delete>("clean") {
    group = "build"
    description = "Delete root build directory"
    delete(rootProject.layout.buildDirectory)

    doLast {
        println("🧹 Cleaned root build directory")
    }
}

// Apply dependency resolution fix
apply(from = "dependency-fix.gradle.kts")
