// ==== GENESIS PROTOCOL - KOVER CONVENTION ====
// Code coverage configuration for Genesis modules

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class KoverConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")
            
            // Configure Kover for Genesis modules
            extensions.configure<kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension> {
                reports {
                    total {
                        html {
                            onCheck = false
                            htmlDir = layout.buildDirectory.dir("reports/kover/html")
                        }
                        
                        xml {
                            onCheck = false
                            xmlFile = layout.buildDirectory.file("reports/kover/coverage.xml")
                        }
                        
                        verify {
                            onCheck = true
                            rule {
                                minBound(80)
                            }
                        }
                    }
                }
            }
        }
    }
}
