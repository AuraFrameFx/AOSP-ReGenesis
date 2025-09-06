// ==== GENESIS PROTOCOL - ANDROID LIBRARY CONVENTION ====
// Standard Android library configuration for all modules

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                // Kotlin plugin applied automatically by AGP 9.0.0-alpha02
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 34
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")

                    vectorDrawables {
                        useSupportLibrary = true
                    }
                }

                buildTypes {
                    release {
                        isMinifyEnabled = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )
                    }
                }

                buildFeatures {
                    buildConfig = true
                    viewBinding = false
                    dataBinding = false
                }

                compileOptions {
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
                            "META-INF/*.kotlin_module"
                        )
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
            extensions.configure<KotlinProjectExtension> {
                jvmToolchain(24)
            }

            // Clean generated sources task
            tasks.register<Delete>("cleanGeneratedSources") {
                group = "build setup"
                description = "Clean all generated source directories"
                
                delete(
                    layout.buildDirectory.dir("generated/ksp"),
                    layout.buildDirectory.dir("generated/source/ksp"),
                    layout.buildDirectory.dir("tmp/kapt3"),
                    layout.buildDirectory.dir("tmp/kotlin-classes"),
                    layout.buildDirectory.dir("kotlin")
                )
            }

            tasks.named("preBuild") {
                dependsOn("cleanGeneratedSources")
            }
        }
    }
}
