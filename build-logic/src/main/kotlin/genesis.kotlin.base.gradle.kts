// Precompiled convention plugin: genesis.kotlin.base
// Provides unified Kotlin + Java toolchain config for all modules.
plugins {
    kotlin("android") apply false
    // We don't apply Android here; this plugin focuses purely on Kotlin/JVM toolchain & flags.
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Apply when included
extensions.findByName("kotlin")?.let { /* already applied in module */ }

val desiredJvmTarget = JvmTarget.JVM_24

kotlin {
    jvmToolchain(24)
    compilerOptions {
        jvmTarget.set(desiredJvmTarget)
        freeCompilerArgs.addAll(
            "-Xcontext-receivers",
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

// Progressive flags / fallback for tasks where direct compilerOptions may not yet wire
tasks.withType<KotlinCompile>().configureEach {
    if (!kotlinOptions.freeCompilerArgs.contains("-Xcontext-receivers")) {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xcontext-receivers",
            "-Xjsr305=strict",
            "-Xjvm-default=all"
        )
    }
}

// Expose a simple verification task (optional)
val verifyGenesisKotlin = tasks.register("verifyGenesisKotlin") {
    group = "verification"
    description = "Print Kotlin convention plugin status"
    doLast {
        println("[genesis.kotlin.base] JVM target = ${desiredJvmTarget}")
    }
}

