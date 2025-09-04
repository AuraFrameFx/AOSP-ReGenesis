/*
 * Library conventions for AeGenesis project modules
 * Applies common conventions and configures library-specific settings
 */

plugins {
    // Apply common conventions
    id("buildlogic.kotlin-common-conventions")

    // Standard Java library plugin for API/implementation separation
    `java-library`

    // Apply Kotlin JVM plugin for all libraries
    id("org.jetbrains.kotlin.jvm")
}

// Configure publishing if needed
java {
    withJavadocJar()
    withSourcesJar()
}

// Configure test tasks
tasks.named<Test>("test") {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Configure JavaDoc
// tasks.named<Javadoc>("javadoc") {
//     isFailOnError = false
// }
