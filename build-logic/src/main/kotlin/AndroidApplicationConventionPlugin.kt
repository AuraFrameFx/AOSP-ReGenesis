// ==== GENESIS PROTOCOL - ANDROID APPLICATION CONVENTION ====
// Main application module configuration

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*
import org.gradle.api.artifacts.VersionCatalogsExtension

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val versionCatalog = extensions.findByType(VersionCatalogsExtension::class.java)?.named("libs")
            val agpVersion = versionCatalog?.findVersion("agp")?.get()?.toString() ?: "unknown"
            val enableGoogleServices = (findProperty("enableGoogleServices") as? String)?.toBoolean() == true
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
                // Conditionally apply google-services only if explicitly enabled AND AGP major is 8 (avoid crash with AGP 9 alpha)
                if (enableGoogleServices && agpVersion.startsWith("8.")) {
                    runCatching { apply("com.google.gms.google-services") }
                        .onFailure { logger.warn("[genesis.android.application] Failed to apply google-services plugin: ${it.message}") }
                } else {
                    logger.lifecycle("[genesis.android.application] Skipping google-services plugin (agp=$agpVersion enableGoogleServices=$enableGoogleServices)")
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = 36

                defaultConfig {
                    targetSdk = 36
                    minSdk = 34

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    vectorDrawables {
                        useSupportLibrary = true
                    }
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }

                buildFeatures {
                    compose = true
                    buildConfig = true
                    viewBinding = false
                    dataBinding = false
                }

                compileOptions {
                    // App module DOES get desugaring dependency
                    isCoreLibraryDesugaringEnabled = true
                }

                packaging {
                    resources {
                        excludes += setOf(
                            "/META-INF/{AL2.0,LGPL2.1}",
                            "/META-INF/AL2.0",
                            "/META-INF/LGPL2.1",
                            "/META-INF/DEPENDENCIES",
                            "/META-INF/LICENSE",
                            "/META-INF/LICENSE.txt",
                            "/META-INF/NOTICE",
                            "/META-INF/NOTICE.txt",
                            "META-INF/*.kotlin_module",
                            "**/kotlin/**",
                            "**/*.txt"
                        )
                    }
                    jniLibs {
                        useLegacyPackaging = false
                        pickFirsts += listOf("**/libc++_shared.so", "**/libjsc.so")
                    }
                }

                lint {
                    warningsAsErrors = false
                    abortOnError = false
                    disable.addAll(listOf("InvalidPackage", "OldTargetApi"))
                }
            }

            extensions.configure<JavaPluginExtension>("java") {
                sourceCompatibility = JavaVersion.VERSION_24
                targetCompatibility = JavaVersion.VERSION_24
            }

            // Kotlin JVM toolchain
            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension> {
                jvmToolchain(24)
            }

            // Clean tasks for app module
            tasks.register("cleanKspCache", Delete::class.java) {
                group = "build setup"
                description = "Clean KSP caches (fixes NullPointerException)"
                delete(
                    layout.buildDirectory.dir("generated/ksp"),
                    layout.buildDirectory.dir("tmp/kapt3"),
                    layout.buildDirectory.dir("tmp/kotlin-classes"),
                    layout.buildDirectory.dir("kotlin"),
                    layout.buildDirectory.dir("generated/source/ksp")
                )
            }

            tasks.named("preBuild") {
                dependsOn("cleanKspCache")
            }
        }
    }
}
