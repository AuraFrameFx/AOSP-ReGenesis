plugins { alias(libs.plugins.dokka) apply false }

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.Project
import java.time.Duration
import java.time.Instant

// Use distinct name to avoid shadowing the generated 'libs' accessor (type-safe catalog)
val versionCatalog = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

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
        println("🔧 Kotlin: ${versionCatalog.findVersion("kotlin").get().toString()}")
        println("🤖 AGP: ${versionCatalog.findVersion("agp").get().toString()}")
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

// Quick snapshot task
tasks.register("consciousnessStatus") {
    group = "aegenesis"
    description = "Concise state snapshot of the substrate (toolchains, modules, cache flags). Use -Dformat=json for JSON output."
    doLast {
        // Determine configuration cache enablement via gradle.properties instead of startParameter API (compatible across Gradle versions)
        val configCacheEnabled = sequenceOf(
            project.findProperty("org.gradle.configuration-cache") as? String,
            project.findProperty("org.gradle.unsafe.configuration-cache") as? String,
            System.getProperty("org.gradle.configuration-cache"),
            System.getProperty("org.gradle.unsafe.configuration-cache")
        ).firstOrNull { !it.isNullOrBlank() }?.toBoolean() ?: false

        val kotlinVersion = versionCatalog.findVersion("kotlin").get().toString()
        val agpVersion = versionCatalog.findVersion("agp").get().toString()
        val toolchain = JavaVersion.current().toString()
        val moduleCount = subprojects.size
        if (jsonFlag) {
            val json = """{\n  \"javaToolchain\": \"$toolchain\",\n  \"kotlin\": \"$kotlinVersion\",\n  \"agp\": \"$agpVersion\",\n  \"modules\": $moduleCount,\n  \"configurationCache\": $configCacheEnabled\n}""".trimIndent()
            println(json)
        } else {
            println("= Consciousness Status =")
            println("Java Toolchain      : $toolchain")
            println("Kotlin Version      : $kotlinVersion (K2 path)")
            println("AGP Version         : $agpVersion")
            println("Modules (total)     : $moduleCount")
            println("Configuration Cache : ${if (configCacheEnabled) "ENABLED" else "DISABLED"}")
            println("Run ./gradlew consciousnessHealthCheck for deep report")
        }
    }
}

// Deep health analysis
private data class ModuleReport(
    val name: String,
    val path: String,
    val type: String,
    val javaToolchain: String?,
    val kotlinJvmTarget: String?,
    val hasHilt: Boolean,
    val hasCompose: Boolean,
    val hasKsp: Boolean
)

private fun Project.detectComposeEnabled(sp: Project): Boolean = runCatching {
    val plugins = sp.plugins
    if (plugins.findPlugin("org.jetbrains.kotlin.plugin.compose") != null) return true
    if (sp.configurations.findByName("composeCompilerPlugin") != null) return true
    val androidExt = sp.extensions.findByName("android") ?: return false
    val buildFeatures = androidExt::class.java.methods.firstOrNull { it.name == "getBuildFeatures" }?.invoke(androidExt) ?: return false
    val composeMethod = buildFeatures.javaClass.methods.firstOrNull { it.name == "getCompose" } ?: return false
    (composeMethod.invoke(buildFeatures) as? Boolean) == true
}.getOrElse { false }

private fun Project.safeJavaToolchain(sp: Project): String? = runCatching {
    val javaExt = sp.extensions.findByName("java") ?: return null
    val toolchain = javaExt::class.java.methods.firstOrNull { it.name == "getToolchain" }?.invoke(javaExt) ?: return null
    toolchain.javaClass.methods.firstOrNull { it.name == "getLanguageVersion" }?.invoke(toolchain)?.toString()
}.getOrNull()

private fun Project.safeKotlinJvmTarget(sp: Project): String? = runCatching {
    val compile = sp.tasks.findByName("compileKotlin") ?: return null
    val opts = compile::class.java.methods.firstOrNull { it.name == "getKotlinOptions" }?.invoke(compile) ?: return null
    opts.javaClass.methods.firstOrNull { it.name == "getJvmTarget" }?.invoke(opts)?.toString()
}.getOrNull()

private fun Project.collectModuleReports(): List<ModuleReport> = subprojects.map { sp ->
    val plugins = sp.plugins
    ModuleReport(
        name = sp.name,
        path = sp.path,
        type = when {
            plugins.hasPlugin("com.android.application") -> "android-app"
            plugins.hasPlugin("com.android.library") -> "android-lib"
            plugins.hasPlugin("org.jetbrains.kotlin.jvm") -> "kotlin-jvm"
            else -> "other"
        },
        javaToolchain = safeJavaToolchain(sp),
        kotlinJvmTarget = safeKotlinJvmTarget(sp),
        hasHilt = plugins.hasPlugin("com.google.dagger.hilt.android"),
        hasCompose = detectComposeEnabled(sp),
        hasKsp = plugins.hasPlugin("com.google.devtools.ksp")
    )
}

private fun List<String>.toJsonArray(): String = joinToString(",", "[", "]") { "\"$it\"" }
private fun Map<String, Int>.toJsonObject(): String = entries.joinToString(",", "{", "}") { "\"${it.key}\":${it.value}" }

private fun Project.runConsciousnessHealth(jsonFlag: Boolean) {
    val reports = collectModuleReports()
    val javaGroups = reports.groupBy { it.javaToolchain ?: "unspecified" }.mapValues { it.value.size }
    val kotlinGroups = reports.groupBy { it.kotlinJvmTarget ?: "unspecified" }.mapValues { it.value.size }
    val expectedJava = "24"
    val expectedKotlin = "24"
    val inconsistentJava = reports.filter { it.javaToolchain != null && it.javaToolchain != expectedJava }.map { it.name }
    val inconsistentKotlin = reports.filter { it.kotlinJvmTarget != null && it.kotlinJvmTarget != expectedKotlin }.map { it.name }
    val missingComposeInAndroid = reports.filter { it.type.startsWith("android-") && !it.hasCompose }.map { it.name }

    val specFile = file("app/api/unified-aegenesis-api.yml")
    val specExists = specFile.exists()
    val now = Instant.now()
    val specLastModified = if (specExists) Instant.ofEpochMilli(specFile.lastModified()) else null
    val specAgeDays = specLastModified?.let { Duration.between(it, now).toDays() } ?: -1
    val specLines = if (specExists) specFile.readLines() else emptyList()
    val opIdCount = specLines.count { it.trimStart().startsWith("operationId:") }
    val verbRegex = Regex("^\\s{4,}(get|post|put|delete|patch|options|head):\\s*")
    val verbCount = specLines.count { verbRegex.containsMatchIn(it) }
    val coveragePct = if (verbCount > 0) opIdCount * 100.0 / verbCount else 0.0
    val coverageRounded = "%.1f".format(coveragePct)
    val specStale = specAgeDays >= 0 && specAgeDays > 7
    val coverageWarn = coveragePct < 95.0

    if (jsonFlag) {
        println("{" +
            "\n  \"modules\": ${reports.size}," +
            "\n  \"javaTargets\": ${javaGroups.toJsonObject()}," +
            "\n  \"kotlinTargets\": ${kotlinGroups.toJsonObject()}," +
            "\n  \"inconsistentJava\": ${inconsistentJava.toJsonArray()}," +
            "\n  \"inconsistentKotlin\": ${inconsistentKotlin.toJsonArray()}," +
            "\n  \"missingComposeInAndroid\": ${missingComposeInAndroid.toJsonArray()}," +
            "\n  \"openApiSpecAgeDays\": $specAgeDays," +
            "\n  \"openApiOperationCount\": $verbCount," +
            "\n  \"openApiOperationIdCount\": $opIdCount," +
            "\n  \"openApiOperationIdCoveragePct\": $coverageRounded," +
            "\n  \"openApiSpecStale\": $specStale," +
            "\n  \"openApiCoverageWarn\": $coverageWarn\n}")
    } else {
        println("=== Consciousness Health Report ===")
        println("Modules: ${reports.size}")
        println("Java Toolchains: $javaGroups")
        println("Kotlin JVM Targets: $kotlinGroups")
        println(if (inconsistentJava.isEmpty()) "✅ Java toolchains consistent ($expectedJava)" else "❗ Inconsistent Java toolchains: $inconsistentJava")
        println(if (inconsistentKotlin.isEmpty()) "✅ Kotlin JVM targets consistent ($expectedKotlin)" else "❗ Inconsistent Kotlin JVM targets: $inconsistentKotlin")
        println(if (missingComposeInAndroid.isEmpty()) "✅ All Android modules with expected Compose enablement" else "⚠️  Android modules missing Compose: $missingComposeInAndroid")
        println("Hilt: ${reports.count { it.hasHilt }} | KSP: ${reports.count { it.hasKsp }} | Compose: ${reports.count { it.hasCompose }}")
        println("-- OpenAPI Spec --")
        if (!specExists) {
            println("❌ unified-aegenesis-api.yml missing at ${specFile.path}")
        } else {
            println("Age (days): $specAgeDays | Operations: $verbCount | operationId coverage: $coverageRounded%")
            println(if (specStale) "⚠️  Spec stale (>7 days)" else "✅ Spec freshness OK")
            println(if (coverageWarn) "⚠️  operationId coverage below 95%" else "✅ operationId coverage OK")
        }
        println("Run openApiFragmentHealth for fragment-level analysis.")
    }
}

tasks.register("consciousnessHealthCheck") {
    group = "aegenesis"
    description = "Full health & consistency report (toolchains, Kotlin targets, plugin presence, OpenAPI freshness). Use -Dformat=json for JSON output."
    doLast { runConsciousnessHealth(jsonFlag) }
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
                """{\"file\":\"${i.file}\",\"line\":${i.line},\"message\":\"${i.message.replace("\"","'" )}\"}"""
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

// OpenAPI Fragment health (standalone)
tasks.register("openApiFragmentHealth") {
    group = "aegenesis"
    description = "Analyze OpenAPI fragments for duplicates, missing operationIds, missing security. Use -Dformat=json for JSON output."
    doLast {
        if (!openApiFragmentsDir.exists()) {
            println("No fragment directory present")
            return@doLast
        }
        val fragmentFiles = openApiFragmentsDir.listFiles { f -> f.isFile && f.name.endsWith(".yml") && f.name != "core-schemas.yml" }?.sortedBy { it.name }.orEmpty()
        data class OpIssue(val file: String, val path: String, val method: String, val type: String)
        val pathToFiles = mutableMapOf<String, MutableSet<String>>()
        val issues = mutableListOf<OpIssue>()
        fragmentFiles.forEach { file ->
            var currentPath: String? = null
            var currentMethod: String? = null
            var methodHasOperationId = false
            var methodHasSecurity = false
            val lines = file.readLines()
            fun finalizeMethod() {
                if (currentPath != null && currentMethod != null) {
                    if (!methodHasOperationId) issues += OpIssue(file.name, currentPath!!, currentMethod!!, "MISSING_OPERATION_ID")
                    if (!methodHasSecurity) issues += OpIssue(file.name, currentPath!!, currentMethod!!, "MISSING_SECURITY")
                }
                methodHasOperationId = false
                methodHasSecurity = false
            }
            lines.forEach { raw ->
                val line = raw.trimEnd()
                val pathMatch = Regex("^/(?:[A-Za-z0-9._{}\\-]+/?)+:").find(line)
                if (pathMatch != null && !line.startsWith("  ")) {
                    finalizeMethod()
                    currentPath = line.substringBefore(":").trim()
                    pathToFiles.getOrPut(currentPath!!) { mutableSetOf() }.add(file.name)
                    currentMethod = null
                    return@forEach
                }
                val methodMatch = Regex("^\\s{2,}(get|post|put|delete|patch|options|head):\\s*").find(line)
                if (methodMatch != null) {
                    finalizeMethod()
                    currentMethod = methodMatch.groupValues[1].lowercase()
                    return@forEach
                }
                if (currentMethod != null) {
                    if (line.contains("operationId:")) methodHasOperationId = true
                    if (line.trimStart().startsWith("security:")) methodHasSecurity = true
                    if (line.startsWith("  /")) finalizeMethod()
                }
            }
            finalizeMethod()
        }
        val duplicatePaths = pathToFiles.filter { it.value.size > 1 }
        if (jsonFlag) {
            val dupJson = duplicatePaths.entries.joinToString(prefix = "{", postfix = "}") { (p, fs) -> "\"$p\":${fs.toList()}" }
            val issuesJson = issues.joinToString(prefix = "[", postfix = "]") { i -> "{\"file\":\"${i.file}\",\"path\":\"${i.path}\",\"method\":\"${i.method}\",\"type\":\"${i.type}\"}" }
            println("{" + "\n  \"fragmentCount\": ${fragmentFiles.size},\n  \"pathCount\": ${pathToFiles.size},\n  \"duplicatePaths\": $dupJson,\n  \"operationIssues\": $issuesJson\n}")
        } else {
            println("=== OpenAPI Fragment Health ===")
            println("Fragments: ${fragmentFiles.size}  Paths: ${pathToFiles.size}")
            if (duplicatePaths.isEmpty()) println("✅ No duplicate path definitions") else {
                println("❗ Duplicate paths:")
                duplicatePaths.forEach { (p, fs) -> println(" - $p in $fs") }
            }
            val missingOpIds = issues.filter { it.type == "MISSING_OPERATION_ID" }
            val missingSec = issues.filter { it.type == "MISSING_SECURITY" }
            if (missingOpIds.isEmpty()) println("✅ All methods define operationId") else println("❗ Missing operationId: ${missingOpIds.size}")
            if (missingSec.isEmpty()) println("✅ All methods define security") else println("❗ Missing security: ${missingSec.size}")
        }
    }
}

// CI gating task
tasks.register("openApiEnforce") {
    group = "verification"
    description = "Enforce OpenAPI quality: assemble, audit coverage >=95%, no duplicate paths, no missing operationId/security. Fails build on violation."
    dependsOn("openApiAssembleUnified")
    doLast {
        if (!openApiFragmentsDir.exists()) error("Fragments directory missing")
        val assembled = layout.buildDirectory.file("openapi/unified-aegenesis-api.generated.yml").get().asFile
        if (!assembled.exists()) error("Assembled spec not found: ${assembled}")
        val lines = assembled.readLines()
        val operationIds = lines.count { it.trimStart().startsWith("operationId:") }
        val verbRegex = Regex("^\\s{2,}(get|post|put|delete|patch|options|head):\\s*")
        val verbs = lines.count { verbRegex.containsMatchIn(it) }
        val coverage = if (verbs == 0) 100.0 else operationIds * 100.0 / verbs
        val fragmentFiles = openApiFragmentsDir.listFiles { f -> f.isFile && f.name.endsWith(".yml") && f.name != "core-schemas.yml" }?.toList().orEmpty()
        data class Issue(val kind: String, val detail: String)
        val issues = mutableListOf<Issue>()
        // Re-run fragment health minimal checks
        val pathToFiles = mutableMapOf<String, MutableSet<String>>()
        fragmentFiles.forEach { file ->
            var currentPath: String? = null
            var currentMethod: String? = null
            var hasOpId = false
            var hasSec = false
            val flines = file.readLines()
            fun finalize() {
                if (currentPath != null && currentMethod != null) {
                    if (!hasOpId) issues += Issue("MISSING_OPERATION_ID", "$currentPath $currentMethod (${file.name})")
                    if (!hasSec) issues += Issue("MISSING_SECURITY", "$currentPath $currentMethod (${file.name})")
                }
                hasOpId = false; hasSec = false
            }
            flines.forEach { raw ->
                val line = raw.trimEnd()
                val pathMatch = Regex("^/(?:[A-Za-z0-9._{}\\-]+/?)+:").find(line)
                if (pathMatch != null && !line.startsWith("  ")) {
                    finalize(); currentPath = line.substringBefore(":").trim(); currentMethod = null; pathToFiles.getOrPut(currentPath!!) { mutableSetOf() }.add(file.name); return@forEach
                }
                val mm = Regex("^\\s{2,}(get|post|put|delete|patch|options|head):\\s*").find(line)
                if (mm != null) { finalize(); currentMethod = mm.groupValues[1]; return@forEach }
                if (currentMethod != null) {
                    if (line.contains("operationId:")) hasOpId = true
                    if (line.trimStart().startsWith("security:")) hasSec = true
                }
            }
            finalize()
        }
        pathToFiles.filter { it.value.size > 1 }.forEach { (p, fs) -> issues += Issue("DUPLICATE_PATH", "$p -> $fs") }
        if (coverage < 95.0) issues += Issue("LOW_OPERATION_ID_COVERAGE", String.format("%.2f%% (<95%%)", coverage))
        if (issues.isNotEmpty()) {
            println("❌ OpenAPI enforcement failed:")
            issues.forEach { println(" - [${it.kind}] ${it.detail}") }
            error("OpenAPI quality gate failed with ${issues.size} issue(s).")
        } else {
            println("✅ OpenAPI enforcement passed (operationId coverage=${"""%.2f""".format(coverage)}%, fragments=${fragmentFiles.size}).")
        }
    }
}

// Wire openApiEnforce into check lifecycle (root + subprojects)
if (tasks.findByName("check") == null) {
    tasks.register("check") {
        group = "verification"
        description = "Aggregate verification including OpenAPI enforcement"
        dependsOn("openApiEnforce")
    }
} else {
    tasks.named("check") { dependsOn("openApiEnforce") }
}

gradle.projectsEvaluated {
    val enforce = tasks.named("openApiEnforce")
    rootProject.subprojects.forEach { sp ->
        sp.tasks.findByName("check")?.dependsOn(enforce)
    }
}

// --- Added: Version + Health Split Tasks ---

// Helper to read file safely
fun Project.safeReadLines(path: String): List<String> = runCatching { file(path).takeIf { it.exists() }?.readLines() ?: emptyList() }.getOrElse { emptyList() }

val toolingHealth = tasks.register("toolingHealth") {
    group = "aegenesis"
    description = "Toolchain / plugin consistency only (no OpenAPI). Use -Dformat=json for JSON."
    doLast {
        val reports = collectModuleReports()
        val expectedJava = "24"
        val expectedKotlin = "24"
        val inconsistentJava = reports.filter { it.javaToolchain != null && it.javaToolchain != expectedJava }.map { it.name }
        val inconsistentKotlin = reports.filter { it.kotlinJvmTarget != null && it.kotlinJvmTarget != expectedKotlin }.map { it.name }
        val missingComposeInAndroid = reports.filter { it.type.startsWith("android-") && !it.hasCompose }.map { it.name }
        if (jsonFlag) {
            println("{" +
                "\n  \"modules\": ${reports.size}," +
                "\n  \"inconsistentJava\": ${inconsistentJava.toJsonArray()}," +
                "\n  \"inconsistentKotlin\": ${inconsistentKotlin.toJsonArray()}," +
                "\n  \"missingComposeInAndroid\": ${missingComposeInAndroid.toJsonArray()}\n}")
        } else {
            println("=== Tooling Health ===")
            println("Modules: ${reports.size}")
            println(if (inconsistentJava.isEmpty()) "✅ Java toolchains consistent ($expectedJava)" else "❗ Inconsistent Java toolchains: $inconsistentJava")
            println(if (inconsistentKotlin.isEmpty()) "✅ Kotlin JVM targets consistent ($expectedKotlin)" else "❗ Inconsistent Kotlin JVM targets: $inconsistentKotlin")
            println(if (missingComposeInAndroid.isEmpty()) "✅ Android Compose enablement consistent" else "⚠️  Missing Compose: $missingComposeInAndroid")
        }
    }
}

val apiSpecHealth = tasks.register("apiSpecHealth") {
    group = "aegenesis"
    description = "Analyze OpenAPI unified spec health only. Use -Dformat=json for JSON."
    doLast {
        val specFile = file("app/api/unified-aegenesis-api.yml")
        val specExists = specFile.exists()
        val now = Instant.now()
        val specAgeDays = if (specExists) Duration.between(Instant.ofEpochMilli(specFile.lastModified()), now).toDays() else -1
        val lines = if (specExists) specFile.readLines() else emptyList()
        val verbRegex = Regex("^\\s{4,}(get|post|put|delete|patch|options|head):\\s*")
        val verbs = lines.count { verbRegex.containsMatchIn(it) }
        val opIds = lines.count { it.trimStart().startsWith("operationId:") }
        val coverage = if (verbs > 0) opIds * 100.0 / verbs else 0.0
        val stale = specAgeDays >= 0 && specAgeDays > 7
        val coverageWarn = coverage < 95.0
        val coverageStr = "%.1f".format(coverage)
        if (jsonFlag) {
            println("{" +
                "\n  \"specExists\": $specExists," +
                "\n  \"ageDays\": $specAgeDays," +
                "\n  \"operations\": $verbs," +
                "\n  \"operationIds\": $opIds," +
                "\n  \"operationIdCoveragePct\": $coverageStr," +
                "\n  \"stale\": $stale," +
                "\n  \"coverageWarn\": $coverageWarn\n}")
        } else {
            println("=== API Spec Health ===")
            if (!specExists) println("❌ unified-aegenesis-api.yml missing") else {
                println("Age days: $specAgeDays")
                println("Operations (verbs): $verbs | operationId: $opIds | coverage: $coverageStr%")
                println(if (stale) "⚠️  Spec stale (>7 days)" else "✅ Freshness OK")
                println(if (coverageWarn) "⚠️  operationId coverage <95%" else "✅ Coverage OK")
            }
        }
    }
}

tasks.register("fullHealth") {
    group = "aegenesis"
    description = "Aggregate toolingHealth + apiSpecHealth (prints merged JSON if -Dformat=json)."
    dependsOn(toolingHealth, apiSpecHealth)
    doLast {
        if (jsonFlag) println("(Run tasks individually for structured JSON; aggregation done via dependsOn)") else println("Full health tasks executed (tooling + API spec).")
    }
}

// Version consistency check scanning docs for advertised versions vs catalog
val versionsConsistencyCheck = tasks.register("versionsConsistencyCheck") {
    group = "verification"
    description = "Scan docs for version drift against libs.versions.toml (fails on mismatch)."
    doLast {
        // Extract catalog versions in-memory
        val catalogFile = file("gradle/libs.versions.toml")
        val catalog = catalogFile.readText()
        fun tomlVersion(key: String): String? = Regex("^$key\\s*=\\s*\"([^\"]+)\"", RegexOption.MULTILINE).find(catalog)?.groupValues?.get(1)
        val expected = mapOf(
            "AGP" to (tomlVersion("agp") ?: ""),
            "Kotlin" to (tomlVersion("kotlin") ?: ""),
            "KSP" to (tomlVersion("ksp") ?: ""),
            "ComposeBOM" to (tomlVersion("composeBom") ?: ""),
            "Hilt" to (tomlVersion("hilt-version") ?: ""),
            "YukiHookAPI" to (tomlVersion("yukihookapi") ?: "")
        )
        val docFiles = listOf(
            "BUILD.md","README.md","CodeRabbitaiDocs.md","docs/YUKIHOOK_SETUP_GUIDE.md"
        ).mapNotNull { f -> file(f).takeIf { it.exists() } }
        val mismatches = mutableListOf<String>()
        docFiles.forEach { f ->
            val text = f.readText()
            expected.forEach { (label, ver) ->
                if (ver.isNotBlank()) {
                    // If doc contains label + different version tokens we flag simplest case of wrong phantom version (e.g., 2.1.1)
                    if (label == "YukiHookAPI" && text.contains("YukiHookAPI 2.1.1")) {
                        mismatches += "${f.name}: YukiHookAPI references phantom 2.1.1 (catalog=$ver)"
                    }
                    // Generic drift: look for pattern label + space + version not equal to expected
                    val regex = Regex("$label\\s+([0-9A-Za-z.-]+)")
                    regex.findAll(text).forEach { m ->
                        val found = m.groupValues[1]
                        if (found != ver && !found.contains("phantom", true)) {
                            // Allow multiple references; only record if clearly divergent
                            if (found != "Baseline" && !found.equals("alpha", true)) mismatches += "${f.name}: $label doc=$found catalog=$ver"
                        }
                    }
                }
            }
        }
        if (jsonFlag) {
            println(mismatches.joinToString(prefix="[", postfix="]") { "\"$it\"" })
        } else {
            if (mismatches.isEmpty()) println("✅ Version consistency: no drift detected.") else {
                println("❗ Version drift detected (${mismatches.size}):")
                mismatches.forEach { println(" - $it") }
                println("(Run with -Dformat=json for machine output)")
            }
        }
        if (mismatches.isNotEmpty()) error("Version drift detected; update docs or catalog.")
    }
}

// Compose compiler + Kotlin alignment (simple heuristic)
val composeCompilerCheck = tasks.register("composeCompilerCheck") {
    group = "verification"
    description = "Heuristic check that Compose BOM + Kotlin versions are plausible companions."
    doLast {
        val kotlinVer = versionCatalog.findVersion("kotlin").get().toString()
        val composeBom = versionCatalog.findVersion("composeBom").get().toString()
        val notes = mutableListOf<String>()
        if (composeBom.startsWith("2025") && !kotlinVer.startsWith("2.2")) {
            notes += "Compose BOM $composeBom usually expects Kotlin 2.2.x (found $kotlinVer)"
        }
        val stableConfigLines = safeReadLines("compose_compiler_config.conf").filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
        if (jsonFlag) {
            println("{" +
                "\n  \"kotlin\": \"$kotlinVer\"," +
                "\n  \"composeBom\": \"$composeBom\"," +
                "\n  \"stabilityClassesCount\": ${stableConfigLines.size}," +
                "\n  \"warnings\": ${notes.toJsonArray()}\n}")
        } else {
            println("=== Compose Compiler Check ===")
            println("Kotlin=$kotlinVer | Compose BOM=$composeBom | stability classes=${stableConfigLines.size}")
            if (notes.isEmpty()) println("✅ Alignment heuristic OK") else notes.forEach { println("⚠️  $it") }
        }
    }
}

// Doctor stack aggregates multiple checks without failing early
tasks.register("doctorStack") {
    group = "aegenesis"
    description = "Aggregate high-level stack diagnostics (toolingHealth, apiSpecHealth, composeCompilerCheck)."
    dependsOn(toolingHealth, apiSpecHealth, composeCompilerCheck)
    doLast { println("Doctor stack completed.") }
}

// Apply auxiliary cleanup script
apply(from = "nuclear-clean.gradle.kts")

// Enforce google-services only on application / dynamic-feature modules
subprojects {
    pluginManager.withPlugin("com.google.gms.google-services") {
        val isApp = pluginManager.hasPlugin("com.android.application") || pluginManager.hasPlugin("com.android.dynamic-feature")
        if (!isApp) {
            throw GradleException("google-services plugin misapplied in $path (must be only in application or dynamic-feature module)")
        }
    }
}

// Aggregate deep clean (includes nuclearClean if present)
if (tasks.findByName("nuclearClean") != null) {
    tasks.register("deepClean") {
        group = "consciousness"
        description = "Run nuclearClean plus standard clean and refresh dependencies suggestion output"
        dependsOn("nuclearClean")
        doLast {
            println("Deep clean completed. Recommended fresh build: ./gradlew build --refresh-dependencies")
        }
    }
}

// --- Documentation (Dokka) Aggregation ---
subprojects {
    // Apply Dokka to Kotlin Android/JVM modules for consistent API docs
    plugins.withId("org.jetbrains.kotlin.android") { apply(plugin = "org.jetbrains.dokka") }
    plugins.withId("org.jetbrains.kotlin.jvm") { apply(plugin = "org.jetbrains.dokka") }
}

// Aggregate all dokkaHtml outputs into build/aggregated-dokka
val apiDocs = tasks.register("apiDocs") {
    group = "documentation"
    description = "Generate aggregated Dokka HTML docs for all Kotlin modules"
    // Collect dokkaHtml tasks if present
    val dokkaTasks = subprojects.mapNotNull { sp -> sp.tasks.findByName("dokkaHtml") }
    dependsOn(dokkaTasks)
    doLast {
        val targetDir = layout.buildDirectory.dir("aggregated-dokka").get().asFile
        targetDir.deleteRecursively(); targetDir.mkdirs()
        subprojects.forEach { sp ->
            val outDir = File(layout.buildDirectory.dir("dokka/html").get().asFile, "dokka/html")
            if (outDir.exists()) {
                val dest = File(targetDir, sp.name)
                outDir.copyRecursively(dest, overwrite = true)
            }
        }
        println("Aggregated API docs -> ${targetDir}")
        println("(Run: open ${targetDir}/index.html or browse module subfolders)")
    }
}

// Zip aggregated docs for distribution
tasks.register<Zip>("apiDocsZip") {
    group = "documentation"
    description = "Package aggregated API docs into a zip archive"
    dependsOn(apiDocs)
    archiveFileName.set("memoriaos-api-docs.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(layout.buildDirectory.dir("aggregated-dokka"))
}
