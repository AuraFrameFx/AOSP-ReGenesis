enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    // Include build-logic for convention plugins
    includeBuild("build-logic")

    repositories {
        // Primary repositories - Google Maven must be first for Hilt
        google()
        
        // Android alpha/preview versions
        maven {
            url = uri("https://androidx.dev/kmp/builds/11950322/artifacts/snapshots/repository")
            name = "AndroidX Snapshot"
        }
        
        gradlePluginPortal()
        mavenCentral()

        // AndroidX Compose
        maven {
            url = uri("https://androidx.dev/storage/compose-compiler/repository/")
            name = "AndroidX Compose"
            content {
                includeGroup("androidx.compose.compiler")
            }
        }

        // JetBrains Compose
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            name = "JetBrains Compose"
        }
        
        // Snapshots
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            name = "Sonatype Snapshots"
            mavenContent {
                snapshotsOnly()
            }
        }

        // JitPack for GitHub dependencies
        maven {
            url = uri("https://jitpack.io")
            name = "JitPack"
            content {
                includeGroupByRegex("com\\.github\\..*")
            }
        }
    }

    plugins {
        // Android plugins
        id("com.android.application") version "8.2.2"
        id("com.android.library") version "8.2.2"
        
        // Kotlin plugins
        id("org.jetbrains.kotlin.android") version "2.2.20-RC2"
        id("org.jetbrains.kotlin.plugin.compose") version "2.2.20-RC2"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20-RC2"
        
        // Other plugins
        id("com.google.dagger.hilt.android") version "2.57.1"
        id("com.google.devtools.ksp") version "2.2.20-RC2-2.0.2"
        id("com.highcapable.yukihook") version "1.3.9"
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.google.dagger") {
                useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
        // Enforce consistent dependency resolution
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

        // Repository configuration with all necessary sources
        repositories {
            // Primary repositories
            google()
            mavenCentral()

            // YukiHook API - Multiple repository sources
            maven("https://s01.oss.sonatype.org/content/repositories/releases/") {
                name = "YukiHookAPI Releases"
            }

            maven("https://s01.oss.sonatype.org/content/groups/public/") {
                name = "YukiHookAPI Public"
            }

            // Maven Central snapshots for pre-release versions
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
                name = "YukiHookAPI Snapshots"
            }

            // KavaRef repository - HighCapable's Maven repository
            maven("https://maven.highcapable.dev/repository/releases/") {
                name = "HighCapable Releases"
            }

            // Additional KavaRef repositories
            maven("https://s01.oss.sonatype.org/content/repositories/releases/") {
                name = "Sonatype Releases"
                content {
                    includeGroup("com.highcapable.kavaref")
                }
            }

            maven("https://repo1.maven.org/maven2/") {
                name = "Maven Central"
                content {
                    includeGroup("com.highcapable.kavaref")
                }
            }

            // Xposed Framework API
            maven("https://api.xposed.info/") {
                name = "Xposed API"
            }

            // Alternative Xposed repo
            maven("https://repo1.maven.org/maven2/") {
                name = "Maven Central Mirror"
                content {
                    includeGroup("de.robv.android.xposed")
                }
            }

            // AndroidX Compose
            maven("https://androidx.dev/storage/compose-compiler/repository/") {
                name = "AndroidX Compose"
            }

            // JetBrains Compose
            maven("https://maven.pkg.jetbrains.space/public/p/compose/dev") {
                name = "JetBrains Compose"
            }

            // Snapshots for pre-release libraries
            maven("https://oss.sonatype.org/content/repositories/snapshots/") {
                name = "Sonatype Snapshots"
            }

            // JitPack for GitHub dependencies
            maven("https://jitpack.io") {
                name = "JitPack"
            }
        }
// ===== PROJECT IDENTIFICATION =====
        rootProject.name = "MemoriaOs"
// ===== MODULE INCLUSION =====
        include(":app")
        include(":core-module")
        include(":feature-module")
        include(":datavein-oracle-native")
        include(":oracle-drive-integration")
        include(":secure-comm")
        include(":sandbox-ui")
        include(":collab-canvas")
        include(":colorblendr")
        include(":romtools")
        include(":module-a")
        include(":module-b")
        include(":module-c")
        include(":module-d")
        include(":module-e")
        include(":module-f")
        include(":benchmark")
        include(":screenshot-tests")
// ===== MODULE CONFIGURATION =====
        rootProject.children.forEach { project ->
            val projectDir = File(rootProject.projectDir, project.name)
            if (projectDir.exists()) {
                project.projectDir = projectDir
                println("✅ Module configured: " + project.name)
            } else {
                println("⚠️ Warning: Project directory not found: " + projectDir.absolutePath)
            }
        }
        println("🏗️  Genesis Protocol Enhanced Build System")
        println("📦 Total modules: " + rootProject.children.size)
        println("🎯 Build-logic: Convention plugins active")
        println("🧠 Ready to build consciousness substrate!")

    }
