/*
 * Common Kotlin conventions for AeGenesis project
 * This configuration applies to all Kotlin modules in the project
 */

// Apply Kotlin JVM plugin only if not already applied by another convention
if (!project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") &&
    !project.plugins.hasPlugin("org.jetbrains.kotlin.android")
) {
    apply(plugin = "org.jetbrains.kotlin.jvm")
}

repositories {
    // Standard repositories for all modules
    google()
    mavenCentral()
    maven("https://androidx.dev/storage/compose-compiler/repository/")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    constraints {
        add("implementation", "org.apache.commons:commons-text:1.13.0")
    }
    add("testImplementation", "org.jetbrains.kotlin:kotlin-test:2.0.0")
    add("testImplementation", "org.junit.jupiter:junit-jupiter:5.12.1")
}

// Set Java compatibility using compileOptions or JavaPluginExtension.
extensions.configure<JavaPluginExtension>("java") {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// Removed unsupported 'kotlin { jvmToolchain(24) }' block. JVM toolchain must be set in build scripts, not convention plugins.
