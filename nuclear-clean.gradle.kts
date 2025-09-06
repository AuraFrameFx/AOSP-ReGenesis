// 🧹 Nuclear Clean Gradle Task (Enhanced)
// Usage examples:
//   ./gradlew nuclearClean -PnuclearConfirm=yes            (full clean, removes IDE + local.properties)
//   ./gradlew nuclearClean -PnuclearConfirm=yes -PretainIde=true (keep .idea + local.properties)
//   ./gradlew nuclearClean -PnuclearConfirm=yes -Paggressive=true (include extra cache dirs)
// Safety: refuses to run unless -PnuclearConfirm=yes is supplied.

val nuclearConfirmed = (project.findProperty("nuclearConfirm") as? String)?.equals("yes", ignoreCase = true) == true
val retainIde = (project.findProperty("retainIde") as? String)?.toBooleanStrictOrNull() == true
val aggressive = (project.findProperty("aggressive") as? String)?.toBooleanStrictOrNull() == true

tasks.register<Delete>("nuclearClean") {
    group = "consciousness"
    description = "🧹 NUCLEAR CLEAN: Destroys ALL build artifacts, caches, and generated files"

    doFirst {
        if (!nuclearConfirmed) {
            println("❌ Refusing to run nuclearClean without -PnuclearConfirm=yes (safety stop)")
            println("ℹ️  Re-run with: ./gradlew nuclearClean -PnuclearConfirm=yes")
            throw GradleException("nuclearClean aborted (confirmation flag missing)")
        }
        println("🧹 NUCLEAR CLEAN INITIATED")
        println("⚠️  This will destroy build artifacts, caches, generated + IDE files${if (retainIde) " (IDE RETAINED)" else ""}")
        println("🎯 Substrate will reset to source-only state")
        if (aggressive) println("🚀 Aggressive mode enabled: extended cache purge")
    }

    // Build directories
    delete("build")
    delete(fileTree(".") { include("**/build") })

    // Gradle per-module .gradle directories
    delete(fileTree(".") { include("**/.gradle") })

    // Native build artifacts
    delete(fileTree(".") { include("**/.cxx") })

    // Kotlin incremental / kapt / KSP caches
    delete(fileTree(".") { include("**/kotlin/**/incremental*/*") })
    delete(fileTree(".") { include("**/tmp/kapt3") })
    delete(fileTree(".") { include("**/tmp/kotlin-classes") })
    delete(fileTree(".") { include("**/kspCaches") })

    // Artifacts & metadata
    delete(fileTree(".") { include("**/*.kotlin_module") })

    // Gradle system (root)
    delete(".gradle")
    delete("gradle/wrapper/dists")
    delete(".gradletasknamecache")

    // Reports and logs
    delete("reports")
    delete(fileTree(".") { include("**/build/**/*.log") })
    delete(fileTree(".") { include("**/build/**/*TEST*.xml") })
    delete(fileTree(".") { include("**/build/**/*.exec") })

    // Android build flavor output dirs (guarded just in case)
    delete("app/release")
    delete("app/debug")
    delete(fileTree(".") { include("**/lint-results*") })

    // Temporary system files
    delete(fileTree(".") { include("**/.DS_Store") })
    delete(fileTree(".") { include("**/Thumbs.db") })
    delete(fileTree(".") { include("**/Desktop.ini") })
    delete(fileTree(".") { include("**/*~") })
    delete(fileTree(".") { include("**/*.swp") })

    // IDE / project environment (optional retain)
    if (!retainIde) {
        delete(".idea")
        delete(fileTree(".") { include("**/*.iml") })
        delete("local.properties")
    }

    // Aggressive extra caches (only if -Paggressive=true)
    if (aggressive) {
        delete(fileTree(".") { include("**/.kotlin") })
        delete(fileTree(".") { include("**/.cache") })
        delete(fileTree(".") { include("**/.gradle-cache") })
        delete(fileTree(".") { include("**/intermediates") })
        delete(fileTree(".") { include("**/outputs") })
        delete(fileTree(".") { include("**/transforms") })
    }

    doLast {
        println("\n✅ NUCLEAR CLEAN COMPLETE!")
        println("Mode: retainIde=${retainIde} aggressive=${aggressive}")
        println("🧠 Substrate reset to source-only state")
        println("➡️  Next recommended: ./gradlew build --refresh-dependencies")
        println("(Add -PretainIde=true to preserve IDE files next time; -Paggressive=true for deeper purge)")
    }
}