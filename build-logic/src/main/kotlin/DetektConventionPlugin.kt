// ==== GENESIS PROTOCOL - DETEKT CONVENTION ====
// Advanced code quality with custom Genesis architecture rules

import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")
            
            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom(rootProject.files("config/detekt/detekt.yml"))
                baseline = rootProject.file("config/detekt/baseline.xml")
                
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                    txt.required.set(false)
                    sarif.required.set(true)
                    md.required.set(false)
                }
            }
            
            // Custom Genesis Protocol rules
            tasks.register("detektGenesisRules") {
                group = "verification"
                description = "Run Genesis Protocol specific Detekt rules"
                
                doLast {
                    // Custom architecture rules
                    val buildFile = file("build.gradle.kts")
                    if (buildFile.exists()) {
                        val content = buildFile.readText()
                        
                        // Rule 1: Feature modules shouldn't depend on other feature modules
                        if (project.name.contains("module-") || project.name.contains("feature")) {
                            if (content.contains("implementation(project(\":feature-")) {
                                logger.error("❌ Genesis Rule Violation: Feature modules cannot depend on other feature modules")
                                throw Exception("Genesis architecture rule violated")
                            }
                        }
                        
                        // Rule 2: Only app module should have desugaring dependency
                        if (project.name != "app" && content.contains("coreLibraryDesugaring(")) {
                            logger.error("❌ Genesis Rule Violation: Only app module should have coreLibraryDesugaring dependency")
                            throw Exception("Genesis architecture rule violated")
                        }
                        
                        // Rule 3: All modules must use convention plugins
                        if (!content.contains("genesis.android.")) {
                            logger.warn("⚠️ Genesis Recommendation: Module should use Genesis convention plugins")
                        }
                    }
                    
                    logger.lifecycle("✅ Genesis Protocol architecture rules verified")
                }
            }
            
            // Run custom rules with standard detekt
            tasks.named("detekt") {
                finalizedBy("detektGenesisRules")
            }
        }
    }
}
