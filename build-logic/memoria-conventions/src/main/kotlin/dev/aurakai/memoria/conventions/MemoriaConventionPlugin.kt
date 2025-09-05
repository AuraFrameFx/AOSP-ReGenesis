package dev.aurakai.memoria.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * MemoriaOs Convention Plugin
 * 
 * Applies base conventions and configurations for MemoriaOs projects.
 * Includes ROM tools verification and other project-specific configurations.
 */
class MemoriaConventionPlugin : Plugin<Project> {
    
    override fun apply(target: Project) {
        // Set basic project properties
        with(target) {
            group = "dev.aurakai.memoria"
            version = "1.0.0"
            
            // Apply base plugins
            plugins.apply("org.jetbrains.kotlin.android")
            plugins.apply("com.android.library")
            plugins.apply("org.jetbrains.kotlin.plugin.serialization")
            
            // Configure Android build features
            android {
                compileSdk = 34
                
                defaultConfig {
                    minSdk = 26
                    targetSdk = 34
                    
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
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
                
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
                
                kotlinOptions {
                    jvmTarget = "21"
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-Xjvm-default=all",
                        "-opt-in=kotlin.RequiresOptIn"
                    )
                }
                
                buildFeatures {
                    compose = true
                    buildConfig = true
                }
                
                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.3"
                }
            }
            
            // Configure ROM tools verification task
            tasks.register("verifyRomTools", VerifyRomToolsTask::class.java) {
                it.group = "verification"
                it.description = "Verifies ROM tools are properly configured"
                it.romToolsDir = file("${rootProject.projectDir}/romtools")
            }
            
            // Configure test tasks
            tasks.withType<Test> {
                useJUnitPlatform()
                testLogging {
                    events("passed", "skipped", "failed")
                }
            }
            
            // Configure dependencies
            dependencies {
                // Core Android dependencies
                "implementation"(libs.findLibrary("androidx-core-ktx").get())
                "implementation"(libs.findLibrary("androidx-appcompat").get())
                
                // Compose
                val composeBom = platform(libs.findLibrary("androidx-compose-bom").get())
                "implementation"(composeBom)
                "implementation"(libs.findLibrary("androidx-compose-ui").get())
                "implementation"(libs.findLibrary("androidx-compose-material3").get())
                "implementation"(libs.findLibrary("androidx-activity-compose").get())
                
                // Testing
                "testImplementation"(libs.findLibrary("junit-jupiter").get())
                "androidTestImplementation"(libs.findLibrary("androidx-test-ext-junit").get())
                "androidTestImplementation"(libs.findLibrary("espresso-core").get())
                "androidTestImplementation"(composeBom)
            }
            
            logger.info("✅ MemoriaOs conventions applied to project: $name")
        }
    }
}

/**
 * Task to verify ROM tools configuration
 */
open class VerifyRomToolsTask : org.gradle.api.DefaultTask() {
    
    @get:org.gradle.api.tasks.InputDirectory
    var romToolsDir: File? = null
    
    @org.gradle.api.tasks.TaskAction
    fun verify() {
        val dir = romToolsDir ?: throw org.gradle.api.GradleException("ROM tools directory not specified")
        
        if (!dir.exists()) {
            throw org.gradle.api.GradleException("ROM tools directory does not exist: ${dir.absolutePath}")
        }
        
        val requiredFiles = listOf(
            "CMakeLists.txt",
            "romtools_native.cpp"
        )
        
        val missingFiles = requiredFiles.filter { !File(dir, it).exists() }
        
        if (missingFiles.isNotEmpty()) {
            throw org.gradle.api.GradleException("Missing required ROM tools files: ${missingFiles.joinToString()}")
        }
        
        logger.lifecycle("✅ ROM tools verification passed")
    }
}

// Helper extensions
private fun Project.android(configure: com.android.build.gradle.LibraryExtension.() -> Unit) {
    extensions.configure("com.android.library", configure)
}

private fun Project.libs(): org.gradle.api.artifacts.VersionCatalog = 
    extensions.getByType(org.gradle.api.artifacts.dsl.VersionCatalogsExtension::class.java).named("libs")

private fun <T> Project.libs(alias: String): org.gradle.api.provider.Provider<org.gradle.api.artifacts.MinimalExternalModuleDependency> = 
    libs().findLibrary(alias).get()
