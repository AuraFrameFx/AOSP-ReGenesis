// ===== GENESIS PROTOCOL - ROOT BUILD =====
// Multi-module project root - using build-logic for convention plugins
plugins {
    // Base plugins (applied to all projects) - versions managed in settings.gradle.kts
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
    id("org.jetbrains.kotlin.plugin.serialization") apply false
    id("com.google.dagger.hilt.android") apply false
    id("com.google.devtools.ksp") apply false
}


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

// Apply dependency resolution fix
apply(from = "dependency-fix.gradle.kts")


