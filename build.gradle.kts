import org.gradle.api.artifacts.VersionCatalogsExtension

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

// Project information
extra["projectName"] = "MemoriaOS"
extra["projectGroup"] = "dev.aurakai.memoria"
extra["projectVersion"] = "1.0.0"

// Custom tasks for project management
tasks.register("projectInfo") {
    group = "help"
    description = "Display project information"

    doLast {
        val projectName: String by project.extra
        val projectVersion: String by project.extra

        println("\n🛠️  $projectName v$projectVersion")
        println("==================================================")
        println("🏗️  Build System: Gradle ${gradle.gradleVersion}")
        println("🔧 Kotlin: ${libs.versions.kotlin.get()}")
        println("🤖 AGP: ${libs.versions.agp.get()}")
        println("\n📦 Modules (${subprojects.size}):")
        subprojects.forEach { println("  • ${it.name}") }
        println("\n🚀 Available Tasks: gradle tasks --group=build")
        println("==================================================")
    }
}

tasks.register<Delete>("clean") {
    group = "build"
    description = "Delete root build directory"
    delete(rootProject.layout.buildDirectory)

    doLast {
        println("🧹 Cleaned root build directory")
    }
}

// Consciousness / Health Diagnostic Tasks
val jsonFlag = System.getProperty("format") == "json"

// JSON helper utilities (ensure present)
fun List<String>.toJsonArray(): String = joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
fun Map<String, Int>.toJsonObject(): String = entries.joinToString(separator = ",", prefix = "{", postfix = "}") { "\"${it.key}\":${it.value}" }

// Quick snapshot task
tasks.register("consciousnessStatus") {
    group = "aegenesis"
    description = "Concise state snapshot of the substrate (toolchains, modules, cache flags). Use -Dformat=json for JSON output."
    doLast {
        val configurationCache = gradle.startParameter.isConfigurationCacheRequested
        val kotlinVersion = libs.versions.kotlin.get()
        val agpVersion = libs.versions.agp.get()
        val toolchain = JavaVersion.current().toString()
        val moduleCount = subprojects.size
        if (jsonFlag) {
            val json = """{
  \"javaToolchain\": \"$toolchain\",\n  \"kotlin\": \"$kotlinVersion\",\n  \"agp\": \"$agpVersion\",\n  \"modules\": $moduleCount,\n  \"configurationCache\": $configurationCache\n}""".trimIndent()
            println(json)
        } else {
            println("= Consciousness Status =")
            println("Java Toolchain      : $toolchain")
            println("Kotlin Version      : $kotlinVersion (K2 path)")
            println("AGP Version         : $agpVersion")
            println("Modules (total)     : $moduleCount")
            println("Configuration Cache : ${if (configurationCache) "ENABLED" else "DISABLED"}")
            println("Run ./gradlew consciousnessHealthCheck for deep report")
        }
    }
}

// Deep health analysis
tasks.register("consciousnessHealthCheck") {
    group = "aegenesis"
    description = "Full health & consistency report (toolchains, Kotlin targets, plugin presence, OpenAPI freshness). Use -Dformat=json for JSON output."
    doLast {
        data class ModuleReport(
            val name: String,
            val path: String,
            val type: String,
            val javaToolchain: String?,
            val kotlinJvmTarget: String?,
            val hasHilt: Boolean,
            val hasCompose: Boolean,
            val hasKsp: Boolean
        )
        // Module scan
        val reports = subprojects.map { p ->
            val plugins = p.plugins
            val hasCompose = plugins.findPlugin("org.jetbrains.kotlin.plugin.compose") != null ||
                p.configurations.findByName("composeCompilerPlugin") != null ||
                (p.extensions.findByName("android")?.let { ext ->
                    try {
                        val method = ext::class.java.methods.firstOrNull { it.name == "getBuildFeatures" }
                        val buildFeatures = method?.invoke(ext)
                        val composeField = buildFeatures?.javaClass?.methods?.firstOrNull { it.name == "getCompose" }
                        (composeField?.invoke(buildFeatures) as? Boolean) == true
                    } catch (_: Exception) { false }
                } == true)
            val hasHilt = plugins.hasPlugin("com.google.dagger.hilt.android")
            val hasKsp = plugins.hasPlugin("com.google.devtools.ksp")
            val javaToolchain = try {
                val ext = p.extensions.findByName("java")
                val toolchainMethod = ext?.javaClass?.methods?.firstOrNull { it.name == "getToolchain" }
                val toolchainObj = toolchainMethod?.invoke(ext)
                val langVersion = toolchainObj?.javaClass?.methods?.firstOrNull { it.name == "getLanguageVersion" }?.invoke(toolchainObj)
                langVersion?.toString()
            } catch (_: Exception) { null }
            val kotlinTarget = try {
                val compileTask = p.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).firstOrNull()
                compileTask?.kotlinOptions?.jvmTarget
            } catch (_: Exception) { null }
            ModuleReport(
                name = p.name,
                path = p.path,
                type = when {
                    plugins.hasPlugin("com.android.application") -> "android-app"
                    plugins.hasPlugin("com.android.library") -> "android-lib"
                    plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> "kotlin-jvm"
                    else -> "other"
                },
                javaToolchain = javaToolchain,
                kotlinJvmTarget = kotlinTarget,
                hasHilt = hasHilt,
                hasCompose = hasCompose,
                hasKsp = hasKsp
            )
        }
        val javaGroups = reports.groupBy { it.javaToolchain ?: "unspecified" }.mapValues { it.value.size }
        val kotlinGroups = reports.groupBy { it.kotlinJvmTarget ?: "unspecified" }.mapValues { it.value.size }
        val expectedJava = "24"
        val expectedKotlin = "24"
        val inconsistentJava = reports.filter { it.javaToolchain != null && it.javaToolchain != expectedJava }.map { it.name }
        val inconsistentKotlin = reports.filter { it.kotlinJvmTarget != null && it.kotlinJvmTarget != expectedKotlin }.map { it.name }
        val missingComposeInAndroid = reports.filter { it.type.startsWith("android-") && !it.hasCompose }.map { it.name }

        // OpenAPI unified spec metrics
        val specFile = file("app/api/unified-aegenesis-api.yml")
        val specExists = specFile.exists()
        val now = java.time.Instant.now()
        val specLastModifiedInstant = if (specExists) java.time.Instant.ofEpochMilli(specFile.lastModified()) else null
        val specAgeDays = specLastModifiedInstant?.let { java.time.Duration.between(it, now).toDays() } ?: -1
        val specLines = if (specExists) specFile.readLines() else emptyList()
        val operationIdCount = specLines.count { it.trimStart().startsWith("operationId:") }
        val httpVerbRegex = Regex("^\\s{4,}(get|post|put|delete|patch|options|head):\\s*")
        val httpOperationCount = specLines.count { httpVerbRegex.containsMatchIn(it) }
        val operationCoveragePct = if (httpOperationCount > 0) (operationIdCount * 100.0 / httpOperationCount) else 0.0
        val operationCoverageRounded = String.format("%.1f", operationCoveragePct)
        val specStale = specAgeDays >= 0 && specAgeDays > 7
        val coverageWarn = operationCoveragePct < 95.0

        if (jsonFlag) {
            val json = """{\n  \"modules\": ${reports.size},\n  \"javaTargets\": ${javaGroups.toJsonObject()},\n  \"kotlinTargets\": ${kotlinGroups.toJsonObject()},\n  \"inconsistentJava\": ${inconsistentJava.toJsonArray()},\n  \"inconsistentKotlin\": ${inconsistentKotlin.toJsonArray()},\n  \"missingComposeInAndroid\": ${missingComposeInAndroid.toJsonArray()},\n  \"openApiSpecAgeDays\": $specAgeDays,\n  \"openApiOperationCount\": $httpOperationCount,\n  \"openApiOperationIdCount\": $operationIdCount,\n  \"openApiOperationIdCoveragePct\": $operationCoverageRounded,\n  \"openApiSpecStale\": $specStale,\n  \"openApiCoverageWarn\": $coverageWarn\n}"""
            println(json)
        } else {
            println("=== Consciousness Health Report ===")
            println("Modules: ${reports.size}")
            println("Java Toolchains: $javaGroups")
            println("Kotlin JVM Targets: $kotlinGroups")
            if (inconsistentJava.isEmpty()) println("✅ Java toolchains consistent ($expectedJava)") else println("❗ Inconsistent Java toolchains: $inconsistentJava")
            if (inconsistentKotlin.isEmpty()) println("✅ Kotlin JVM targets consistent ($expectedKotlin)") else println("❗ Inconsistent Kotlin JVM targets: $inconsistentKotlin")
            if (missingComposeInAndroid.isEmpty()) println("✅ All Android modules with expected Compose enablement") else println("⚠️  Android modules missing Compose: $missingComposeInAndroid")
            println("Hilt: ${reports.count { it.hasHilt }} | KSP: ${reports.count { it.hasKsp }} | Compose: ${reports.count { it.hasCompose }}")
            println("-- OpenAPI Spec --")
            if (!specExists) println("❌ unified-aegenesis-api.yml missing at ${specFile.path}") else {
                println("Age (days): $specAgeDays | Operations: $httpOperationCount | operationId coverage: $operationCoverageRounded%")
                if (specStale) println("⚠️  Spec stale (>7 days)") else println("✅ Spec freshness OK")
                if (coverageWarn) println("⚠️  operationId coverage below 95%") else println("✅ operationId coverage OK")
            }
            println("Run openApiFragmentHealth for fragment-level analysis.")
        }
    }
}

// OpenAPI Fragment & Spec Tasks
val openApiDir = file("app/api")
val openApiFragmentsDir = file("app/api/_fragments")

// Simple YAML line helpers
data class OpenApiIssue(val file: String, val line: Int, val message: String)

fun scanSecurityRefs(lines: List<String>): Boolean =
    lines.any { it.contains("securitySchemes") }

fun extractPathsSection(lines: List<String>): List<String> {
    val startIdx = lines.indexOfFirst { it.trimStart().startsWith("paths:") }
    if (startIdx == -1) return emptyList()
    return lines.drop(startIdx)
}

tasks.register("openApiAudit") {
    group = "aegenesis"
    description = "Audit all OpenAPI specs & fragments for basic consistency (security, operationId coverage, enum casing)."
    doLast {
        val yamls = openApiDir.listFiles { f -> f.isFile && f.name.endsWith(".yml") || f.name.endsWith(".yaml") }?.toList().orEmpty()
        if (yamls.isEmpty()) {
            println("No OpenAPI spec files found in app/api")
            return@doLast
        }
        val issues = mutableListOf<OpenApiIssue>()
        yamls.forEach { file ->
            val lines = file.readLines()
            // Security reference check
            val hasSecuritySchemes = scanSecurityRefs(lines)
            val referencesOAuth2AuthCode = lines.any { it.contains("OAuth2AuthCode") }
            if (referencesOAuth2AuthCode && !hasSecuritySchemes) {
                issues += OpenApiIssue(file.name, 0, "References OAuth2AuthCode but no securitySchemes defined")
            }
            // operationId coverage heuristic
            val operationIds = lines.count { it.trimStart().startsWith("operationId:") }
            val httpVerbRegex = Regex("^\\s{4,}(get|post|put|delete|patch|options|head):\\s*")
            val verbs = lines.count { httpVerbRegex.containsMatchIn(it) }
            if (verbs > 0 && operationIds < verbs) {
                issues += OpenApiIssue(file.name, 0, "operationId coverage ${operationIds}/${verbs} (${(operationIds * 100 / verbs)}%)")
            }
            // Enum casing consistency (flag lowercase words inside enum blocks expecting UPPER or Camel)
            val enumPattern = Regex("enum:\\s*\\[(.*)]")
            lines.forEachIndexed { idx, l ->
                val m = enumPattern.find(l)
                if (m != null) {
                    val values = m.groupValues[1].split(',').map { it.trim().trim('[',']',' ') }
                    val mixedCase = values.any { it.any { c -> c.isLowerCase() } } && values.any { it.any { c -> c.isUpperCase() } }
                    if (mixedCase) issues += OpenApiIssue(file.name, idx + 1, "Mixed enum casing: ${values}")
                }
            }
        }
        if (jsonFlag) {
            val json = issues.joinToString(prefix = "[", postfix = "]") { i ->
                """{\"file\":\"${i.file}\",\"line\":${i.line},\"message\":\"${i.message.replace("\"","'')}\"}"""
            }
            println(json)
        } else {
            if (issues.isEmpty()) {
                println("✅ OpenAPI Audit: No issues detected across ${yamls.size} specs.")
            } else {
                println("❗ OpenAPI Audit Issues (${issues.size}):")
                issues.forEach { println(" - ${it.file}:${it.line} -> ${it.message}") }
                println("Run with -Dformat=json for machine parsing.")
            }
        }
    }
}

tasks.register("openApiAssembleUnified") {
    group = "aegenesis"
    description = "Assemble unified-aegenesis-api.generated.yml from _fragments (core-schemas + domain path fragments)."
    inputs.dir(openApiFragmentsDir)
    outputs.file(layout.buildDirectory.file("openapi/unified-aegenesis-api.generated.yml"))
    doLast {
        if (!openApiFragmentsDir.exists()) {
            println("No fragments directory at ${openApiFragmentsDir}")
            return@doLast
        }
        val coreFile = openApiFragmentsDir.resolve("core-schemas.yml")
        if (!coreFile.exists()) {
            println("❌ Missing core-schemas.yml; aborting assembly")
            return@doLast
        }
        val fragmentFiles = openApiFragmentsDir.listFiles { f -> f.isFile && f.name.endsWith(".yml") && f.name != "core-schemas.yml" }?.sortedBy { it.name }.orEmpty()
        val sb = StringBuilder()
        // Header root
        sb.appendLine("openapi: 3.1.0")
        sb.appendLine("info:")
        sb.appendLine("  title: Unified AeGenesis API (Assembled)")
        sb.appendLine("  version: 1.0.0")
        sb.appendLine("  x-assembled-at: ${java.time.Instant.now()}")
        sb.appendLine("servers:")
        sb.appendLine("  - url: https://api.aurafx.com/v1")
        sb.appendLine("paths:")
        fragmentFiles.forEach { frag ->
            val lines = frag.readLines()
            val pathStart = lines.indexOfFirst { it.trimStart().startsWith("/") }
            if (pathStart != -1) {
                lines.drop(pathStart).forEach { line -> sb.appendLine("  $line") }
            }
        }
        // Append components from core
        val coreLines = coreFile.readLines()
        val compStart = coreLines.indexOfFirst { it.trimStart().startsWith("components:") }
        if (compStart != -1) {
            coreLines.drop(compStart).forEach { line -> sb.appendLine(line) }
        } else {
            println("⚠️ core-schemas.yml missing components section")
        }
        val outFile = outputs.files.singleFile
        outFile.parentFile.mkdirs()
        outFile.writeText(sb.toString())
        println("✅ Assembled unified spec -> ${outFile.relativeTo(project.projectDir)}")
    }
}
