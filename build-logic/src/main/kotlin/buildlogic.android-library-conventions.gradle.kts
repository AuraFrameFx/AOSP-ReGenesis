/*
 * Android Library conventions for AeGenesis project
 * Applies common conventions and configures Android library specific settings
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("buildlogic.kotlin-common-conventions")
}

android {
    namespace = "com.aegenesis.${project.name.replace("-", "")}"

    compileSdk = 36 // Unified to 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Java toolchain replaces manual source/target compatibility
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(24))
        }
    }

    // Modern Kotlin compiler options
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
            languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-Xjvm-default=all",
                "-Xskip-prerelease-check"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Configure test tasks
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

// Configure publishing if needed
if (project.hasProperty("publish")) {
    apply(plugin = "com.vanniktech.maven.publish")

    configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
        publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
        signAllPublications()

        pom {
            name.set(project.name)
            description.set("AeGenesis - ${project.name} module")
            url.set("https://github.com/AuraFrameFxDev/MemoriaOs")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    id.set("aegenesis")
                    name.set("AeGenesis Team")
                    email.set("contact@aegenesis.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/AuraFrameFxDev/MemoriaOs.git")
                developerConnection.set("scm:git:ssh://github.com/AuraFrameFxDev/MemoriaOs.git")
                url.set("https://github.com/AuraFrameFxDev/MemoriaOs")
            }
        }
    }
}
