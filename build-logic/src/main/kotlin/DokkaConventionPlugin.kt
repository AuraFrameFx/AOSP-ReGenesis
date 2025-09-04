// ==== GENESIS PROTOCOL - DOKKA CONVENTION ====
// Professional API documentation generation

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaTask

class DokkaConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.dokka")
            
            tasks.withType<DokkaTask>().configureEach {
                dokkaSourceSets {
                    configureEach {
                        // Include source links to GitHub
                        sourceLink {
                            localDirectory.set(projectDir.resolve("src"))
                            remoteUrl.set(java.net.URL("https://github.com/AeGenesis/MemoriaOs/tree/main/${project.name}/src"))
                            remoteLineSuffix.set("#L")
                        }
                        
                        // Configure external documentation links
                        externalDocumentationLink {
                            url.set(java.net.URL("https://kotlinlang.org/api/latest/jvm/stdlib/"))
                        }
                        
                        externalDocumentationLink {
                            url.set(java.net.URL("https://developer.android.com/reference/"))
                        }
                        
                        externalDocumentationLink {
                            url.set(java.net.URL("https://developer.android.com/reference/kotlin/androidx/"))
                        }
                        
                        // Skip generated files
                        suppressedFiles.from(
                            fileTree(layout.buildDirectory.dir("generated")) {
                                include("**/*.kt")
                            }
                        )
                    }
                }
            }
            
            // Aggregate docs task for multi-module documentation
            if (project == rootProject) {
                tasks.register("dokkaHtmlMultiModule") {
                    group = "documentation"
                    description = "Generate HTML documentation for all modules"
                    dependsOn(subprojects.map { ":${it.name}:dokkaHtml" })
                    
                    doLast {
                        logger.lifecycle("📚 Genesis Protocol documentation generated")
                        logger.lifecycle("📁 Location: ${layout.buildDirectory.dir("dokka/htmlMultiModule").get().asFile}")
                    }
                }
            }
        }
    }
}
