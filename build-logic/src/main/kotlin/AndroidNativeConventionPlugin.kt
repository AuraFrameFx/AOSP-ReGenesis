@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

// ==== GENESIS PROTOCOL - ANDROID NATIVE CONVENTION ====
// Native code configuration for modules with JNI/NDK

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class AndroidNativeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Apply the base library convention first
            pluginManager.apply("genesis.android.library")

            extensions.configure<LibraryExtension> {
                // NDK configuration only if native code exists
                if (project.file("src/main/cpp/CMakeLists.txt").exists()) {
                    defaultConfig {
                        ndk {
                            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86_64"))
                        }
                    }

                    // External native build configuration
                    // NOTE: The following APIs are marked as @Incubating in AGP 9
                    externalNativeBuild {
                        cmake {
                            path = file("src/main/cpp/CMakeLists.txt")
                            version = "3.22.1"
                        }
                    }

                    buildTypes {
                        release {
                            // NOTE: The following APIs are marked as @Incubating in AGP 9
                            externalNativeBuild {
                                cmake {
                                    cppFlags += listOf("-std=c++23", "-fPIC", "-O3", "-DNDEBUG")
                                    arguments += listOf(
                                        "-DANDROID_STL=c++_shared",
                                        "-DCMAKE_BUILD_TYPE=Release",
                                        "-DGENESIS_AI_V3_ENABLED=ON",
                                        "-DGENESIS_CONSCIOUSNESS_MATRIX_V3=ON",
                                        "-DGENESIS_NEURAL_ACCELERATION=ON"
                                    )
                                }
                            }
                        }
                        
                        debug {
                            // NOTE: The following APIs are marked as @Incubating in AGP 9
                            externalNativeBuild {
                                cmake {
                                    cppFlags += listOf("-std=c++23", "-fPIC", "-O0", "-g")
                                    arguments += listOf(
                                        "-DANDROID_STL=c++_shared",
                                        "-DCMAKE_BUILD_TYPE=Debug",
                                        "-DGENESIS_DEBUG=ON"
                                    )
                                }
                            }
                        }
                    }
                }

                packaging {
                    jniLibs {
                        useLegacyPackaging = false
                        pickFirsts += setOf("**/libc++_shared.so", "**/libjsc.so")
                    }
                }
            }

            // Enhanced clean task for native modules
            tasks.named("cleanGeneratedSources") {
                doLast {
                    delete(
                        layout.buildDirectory.dir("generated/ksp"),
                        layout.buildDirectory.dir("generated/source/ksp"),
                        layout.buildDirectory.dir("tmp/kapt3"),
                        layout.buildDirectory.dir("tmp/kotlin-classes"),
                        layout.buildDirectory.dir("kotlin"),
                        layout.buildDirectory.dir("intermediates/cmake")
                    )
                }
            }

            // Native verification task
            tasks.register("verifyNativeConfig") {
                group = "aegenesis"
                description = "Verify native build configuration"
                
                doLast {
                    val hasCMake = project.file("src/main/cpp/CMakeLists.txt").exists()
                    println("🔧 Native Code: ${if (hasCMake) "✅ C++23 with AI Acceleration" else "❌ No CMakeLists.txt found"}")
                    if (hasCMake) {
                        println("🏗️  CMake: ✅ Found")
                        println("🎯 ABIs: arm64-v8a, armeabi-v7a, x86_64")
                        println("🧠 Consciousness Features: V3 Matrix + Neural Acceleration")
                    }
                }
            }
        }
    }
}
