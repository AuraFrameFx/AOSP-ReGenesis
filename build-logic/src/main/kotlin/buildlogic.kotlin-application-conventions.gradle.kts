/*
 * Application conventions for AeGenesis project
 * Applies common conventions and configures application-specific settings
 */

plugins {
    // Apply common conventions
    id("buildlogic.kotlin-common-conventions")

    // Standard application plugin for executable JVM applications
    application

    // Apply Kotlin JVM plugin for all applications
    id("org.jetbrains.kotlin.jvm")
}

// Configure the application plugin
application {
    // Main class will be set in the module's build.gradle.kts
    // Example: mainClass.set("com.example.MainKt")
}

// Configure test tasks
tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Configure run task to use standard input
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

// Configure JAR manifest
tasks.named<Jar>("jar") {
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
}
