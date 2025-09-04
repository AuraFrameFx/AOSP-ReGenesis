// Note: Framework detection performed via build files; tests target libs.versions.toml integrity.
// GradleVersionCatalogTest.kt
// Testing library/framework: JUnit 5 (JUnit Jupiter) with kotlin.test assertions (if available)
@file:Suppress("SameParameterValue", "UNCHECKED_CAST", "MemberVisibilityCanBePrivate")

package testplaceholder

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.charset.StandardCharsets
import kotlin.io.path.exists
import kotlin.io.path.readText
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * These tests validate the Gradle Version Catalog (libs.versions.toml).
 * They avoid introducing TOML parser deps by using structured text checks.
 */
class GradleVersionCatalogTest {

    companion object {
        private lateinit var catalogPaths: List<Path>
        private lateinit var catalogTextByPath: Map<Path, String>

        @BeforeAll
        @JvmStatic
        fun loadCatalogs() {
            val repoRoot = Paths.get("").toAbsolutePath()
            val found = mutableListOf<Path>()

            // Typical locations
            val typical = listOf(
                repoRoot.resolve("gradle/libs.versions.toml"),
                repoRoot.resolve("libs.versions.toml"),
            )
            found += typical.filter { Files.exists(it) }

            // Fallback: scan for any libs.versions.toml
            if (found.isEmpty()) {
                Files.walk(repoRoot).use { stream ->
                    stream.filter { it.fileName?.toString() == "libs.versions.toml" }
                        .forEach { found += it }
                }
            }

            catalogPaths = found.distinct()
            catalogTextByPath = catalogPaths.associateWith {
                Files.readString(it, StandardCharsets.UTF_8)
            }
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
            // nothing to clean up
        }
    }

    @Test
    fun detectsAtLeastOneCatalogFile() {
        assertFalse(catalogPaths.isEmpty(), "No libs.versions.toml found in repository.")
    }

    @Nested
    @DisplayName("Catalog structure and sections")
    inner class StructureChecks {

        @Test
        fun hasExpectedTopSections() {
            for ((path, text) in catalogTextByPath) {
                assertAll(
                    "Top sections in $path",
                    {
                        assertTrue(
                            text.contains("\n[versions]") || text.startsWith("[versions]"),
                            "Missing [versions] section in $path"
                        )
                    },
                    {
                        assertTrue(
                            text.contains("\n[libraries]") || text.contains("\n[libraries.") || text.startsWith(
                                "[libraries]"
                            ) || text.startsWith("[libraries."),
                            "Missing [libraries] section (table or subtables) in $path"
                        )
                    },
                    {
                        assertTrue(
                            text.contains("\n[plugins]") || text.startsWith("[plugins]") || text.contains(
                                "\n[plugins."
                            ),
                            "Missing [plugins] section (table or subtables) in $path"
                        )
                    }
                )
            }
        }

        @Test
        fun noTabsOrCRLF() {
            for ((path, text) in catalogTextByPath) {
                assertFalse(text.contains('\t'), "Tabs found in $path; prefer 2 spaces.")
                assertFalse(text.contains("\r\n"), "CRLF line endings found in $path; use LF.")
            }
        }

        @Test
        fun filesAreNonEmpty() {
            for ((path, text) in catalogTextByPath) {
                assertTrue(text.trim().isNotEmpty(), "Catalog $path is empty.")
            }
        }
    }

    @Nested
    @DisplayName("[versions] validations")
    inner class VersionsSectionChecks {

        private fun extractSection(text: String, sectionHeader: String): List<String> {
            // naive section splitter: lines from [section] until next [xxx]
            val lines = text.lines()
            val result = mutableListOf<String>()
            var inSection = false
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    inSection =
                        trimmed == "[$sectionHeader]" || trimmed.startsWith("[$sectionHeader.")
                    if (trimmed != "[$sectionHeader]" && trimmed.startsWith("[$sectionHeader.")) {
                        // treat subtables as not part of plain [versions]; break if you prefer stricter behavior
                        inSection = trimmed == "[$sectionHeader]"
                    }
                    continue
                }
                if (inSection) result += line
            }
            return result
        }

        private fun extractKeyValues(bodyLines: List<String>): Map<String, String> {
            // key = "value"
            val out = linkedMapOf<String, String>()
            val kvRegex = Regex("""^\s*([A-Za-z0-9._-]+)\s*=\s*["']([^"']+)["']\s*(#.*)?$""")
            for (ln in bodyLines) {
                val m = kvRegex.find(ln) ?: continue
                val (key, value) = m.groupValues.drop(1)
                if (key.isNotBlank()) {
                    require(!out.containsKey(key)) { "Duplicate version key: $key" }
                    out[key] = value
                }
            }
            return out
        }

        @Test
        fun versionsHaveUniqueKeysAndValidLookingValues() {
            val semverLike = Regex("""^\d+(\.\d+){1,3}([-\+][A-Za-z0-9.-]+)?$""")
            for ((path, text) in catalogTextByPath) {
                val lines = extractSection(text, "versions")
                assertTrue(lines.isNotEmpty(), "No content under [versions] in $path")

                val seen = mutableSetOf<String>()
                val kvRegex = Regex("""^\s*([A-Za-z0-9._-]+)\s*=""")
                for ((idx, ln) in lines.withIndex()) {
                    val m = kvRegex.find(ln) ?: continue
                    val key = m.groupValues[1]
                    assertTrue(
                        seen.add(key),
                        "Duplicate key in [versions]: '$key' at line ${idx + 1} in $path"
                    )
                }

                val kv = extractKeyValues(lines)
                assertTrue(
                    kv.isNotEmpty(),
                    "Could not parse any key/value pairs in [versions] in $path"
                )

                // Not all versions must be semver; but warn/assert common cases look like versions
                var checked = 0
                for ((k, v) in kv) {
                    if (v.any { it.isDigit() }) {
                        checked++
                        assertTrue(v.length < 100, "Suspiciously long version for $k in $path")
                    }
                }
                assertTrue(checked > 0, "No numeric-looking version values detected in $path")
            }
        }
    }

    @Nested
    @DisplayName("version.ref integrity for [libraries] and [plugins]")
    inner class VersionRefIntegrityChecks {

        private fun parseSimpleTomlAssignments(text: String): Map<String, String> {
            val out = linkedMapOf<String, String>()
            val kvRegex = Regex("""^\s*([A-Za-z0-9._-]+)\s*=\s*["']([^"']+)["']\s*(#.*)?$""")
            for (ln in text.lines()) {
                val m = kvRegex.find(ln) ?: continue
                val (key, value) = m.groupValues.drop(1)
                out[key] = value
            }
            return out
        }

        private fun versionsMap(text: String): Set<String> {
            val lines = text.lines()
            val sb = StringBuilder()
            var inVersions = false
            for (line in lines) {
                val t = line.trim()
                if (t.startsWith("[") && t.endsWith("]")) {
                    inVersions = t == "[versions]"
                    continue
                }
                if (inVersions) sb.appendLine(line)
            }
            val kv = parseSimpleTomlAssignments(sb.toString())
            return kv.keys
        }

        private fun referencedVersionRefs(text: String): Set<String> {
            // match `version.ref = "kotlin"` or `version.ref="kotlin"`
            val refRegex = Regex("""version\.ref\s*=\s*["']([^"']+)["']""")
            return refRegex.findAll(text).map { it.groupValues[1] }.toSet()
        }

        @Test
        fun allVersionRefsExistInVersions() {
            for ((path, text) in catalogTextByPath) {
                val refs = referencedVersionRefs(text)
                assertTrue(
                    refs.isNotEmpty(),
                    "No version.ref found in $path; ensure libraries/plugins use version refs where applicable."
                )

                val vers = versionsMap(text)
                val missing = refs.filterNot { it in vers }
                assertTrue(
                    missing.isEmpty(),
                    "Missing version keys in [versions] for refs: $missing in $path"
                )
            }
        }

        @Test
        fun libraryEntriesHaveEitherVersionOrVersionRef() {
            for ((path, text) in catalogTextByPath) {
                // naive scan inside [libraries] subtables; entries typically like:
                // lib = { module = "g:a", version = "1.2.3" } or version.ref = "x"
                val inLibraries = StringBuilder()
                var inLib = false
                for (line in text.lines()) {
                    val t = line.trim()
                    if (t.startsWith("[") && t.endsWith("]")) {
                        inLib = t == "[libraries]" || t.startsWith("[libraries.")
                        continue
                    }
                    if (inLib) inLibraries.appendLine(line)
                }
                val content = inLibraries.toString()
                if (content.isBlank()) continue

                val entryRegex = Regex("""^\s*([A-Za-z0-9._-]+)\s*=\s*\{[^}]*}""")
                val versionRegex = Regex("""\bversion\s*=\s*["'][^"']+["']""")
                val versionRefRegex = Regex("""\bversion\.ref\s*=\s*["'][^"']+["']""")

                val entries = content.lines().mapNotNull { entryRegex.find(it)?.value }
                if (entries.isEmpty()) continue

                for (e in entries) {
                    assertTrue(
                        versionRegex.containsMatchIn(e) || versionRefRegex.containsMatchIn(e),
                        "Library entry missing version or version.ref: $e in $path"
                    )
                }
            }
        }
    }

    @Nested
    @DisplayName("Basic plugin id sanity checks")
    inner class PluginChecks {

        @Test
        fun pluginIdsLookValid() {
            for ((path, text) in catalogTextByPath) {
                val inPlugins = StringBuilder()
                var inPl = false
                for (line in text.lines()) {
                    val t = line.trim()
                    if (t.startsWith("[") && t.endsWith("]")) {
                        inPl = t == "[plugins]" || t.startsWith("[plugins.")
                        continue
                    }
                    if (inPl) inPlugins.appendLine(line)
                }
                val body = inPlugins.toString()
                if (body.isBlank()) continue

                val entryRegex = Regex("""^\s*([A-Za-z0-9._-]+)\s*=\s*\{[^}]*}""")
                val idRegex = Regex("""\bid\s*=\s*["']([A-Za-z0-9._-]+)["']""")
                for (ln in body.lines()) {
                    val e = entryRegex.find(ln)?.value ?: continue
                    val id = idRegex.find(e)?.groupValues?.getOrNull(1)
                    assertNotNull(id, "Plugin entry missing 'id': $e in $path")
                    if (id != null) {
                        val simpleAllowed = setOf(
                            "java",
                            "application",
                            "base",
                            "jacoco",
                            "maven-publish",
                            "signing"
                        )
                        assertTrue(
                            id.any { it == '.' || it == '-' } || id in simpleAllowed,
                            "Plugin id looks unusual: $id in $path"
                        )
                    }
                }
            }
        }
    }
}