@file:Suppress("SpellCheckingInspection", "HttpUrlsUsage")

/**
 * Additional unit tests for README validation focused on the PR's README-related changes.
 *
 * Testing library & framework: Kotlin + JUnit 5 (Jupiter), matching existing tests in this repo.
 * These tests are defensive: many assertions are conditional on README content to reduce brittleness.
 */
package docs

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationExtraTest {

    private lateinit var readmePath: Path
    private lateinit var readme: String
    private lateinit var lines: List<String>

    @BeforeAll
    fun loadReadme() {
        val candidates = listOf(
            Path.of("README.md"),
            Path.of("Readme.md"),
            Path.of("readme.md"),
            Path.of("docs/README.md")
        )
        readmePath = candidates.firstOrNull { Files.exists(it) }
            ?: error("README not found. Checked: ${candidates.joinToString()}")
        readme = Files.readString(readmePath, StandardCharsets.UTF_8)
        lines = readme.lines()
        assertTrue(readme.isNotBlank(), "README should not be empty")
    }

    private fun normalizeToSlug(text: String): String {
        val header = text
            .replace(Regex("^\\s*#+\\s*"), "")
            .trim()
        val noEmoji = header.replace(Regex("[\\p{So}\\p{Sk}]"), "")
        return noEmoji
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
    }

    @Nested
    @DisplayName("Table of Contents format and coverage")
    inner class TocFormatAndCoverage {

        private fun locateTocBody(): List<String> {
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*.*Table of Contents.*$", RegexOption.IGNORE_CASE)) }
            assertTrue(tocStart >= 0, "Table of Contents section not found")
            return lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
        }

        @Test
        fun `toc entries are bulleted, unique and at least three`() {
            val tocBody = locateTocBody()
            val items = tocBody.filter { it.trim().startsWith("- ") || it.trim().startsWith("* ") }
            assertTrue(items.isNotEmpty(), "ToC must contain bulleted entries (starting with - or *)")

            val anchorRx = Regex("- \\[[^\\]]+\\]\\(#([^\\)]+)\\)")
            val anchors = items.mapNotNull { line -> anchorRx.find(line.trim())?.groupValues?.get(1)?.trim('-') }
            assertTrue(items.size >= 3, "ToC should have at least 3 entries; found ${items.size}")

            val dupAnchors = anchors.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            assertTrue(dupAnchors.isEmpty(), "Duplicate ToC anchors detected: $dupAnchors")
        }

        @Test
        fun `toc includes overview and architecture anchors`() {
            val tocBody = locateTocBody()
            val anchors = tocBody.mapNotNull { line ->
                Regex("- \\[[^\\]]+\\]\\(#([^\\)]+)\\)").find(line.trim())?.groupValues?.get(1)?.trim('-')
            }.toSet()
            assertAll(
                { assertTrue("overview" in anchors, "ToC should contain an 'overview' anchor") },
                { assertTrue("architecture" in anchors, "ToC should contain an 'architecture' anchor") }
            )
        }
    }

    @Nested
    @DisplayName("Placeholder hygiene")
    inner class PlaceholderHygiene {
        @Test
        fun `readme does not contain placeholder terms`() {
            val placeholders = listOf("TODO", "TBD", "FIXME", "WIP", "lorem ipsum")
            val found = placeholders.filter { term -> Regex("\\b$term\\b", RegexOption.IGNORE_CASE).containsMatchIn(readme) }
            assertTrue(found.isEmpty(), "Remove placeholder terms from README: $found")
        }
    }

    @Nested
    @DisplayName("Header level progression")
    inner class HeaderProgression {

        @Test
        fun `no header level jumps greater than one`() {
            var inFence = false
            val headers = mutableListOf<Pair<Int, String>>() // (level, text)
            lines.forEach { raw ->
                val line = raw.trim()
                if (line.startsWith("```")) {
                    inFence = !inFence
                }
                if (!inFence && line.matches(Regex("^#{1,6}\\s+.*$"))) {
                    val level = line.takeWhile { it == '#' }.length
                    headers += level to line
                }
            }
            var prev = 0
            val bad = mutableListOf<String>()
            headers.forEach { (level, text) ->
                if (prev != 0 && level - prev > 1) {
                    bad += "H$prev -> H$level for '${text}'"
                }
                prev = level
            }
            assertTrue(bad.isEmpty(), "Header level jumps greater than one found: $bad")
        }
    }

    @Nested
    @DisplayName("Link schemes and definitions")
    inner class LinkSchemes {

        @Test
        fun `reference-style links use https`() {
            val httpRefDefs = Regex("(?m)^\\[[^\\]]+]:\\s*(http://[^\\s]+)")
                .findAll(readme)
                .map { it.groupValues[1] }
                .toList()
            assertTrue(httpRefDefs.isEmpty(), "Reference-style links must use https: $httpRefDefs")
        }
    }

    @Nested
    @DisplayName("Code fence quality")
    inner class CodeFenceQuality {

        @Test
        fun `at least half of opening code fences specify a language`() {
            var inFence = false
            var openCount = 0
            var labeledOpen = 0
            lines.forEach { raw ->
                val line = raw.trim()
                if (line.startsWith("```")) {
                    if (!inFence) {
                        openCount++
                        if (Regex("^```\\w+", RegexOption.IGNORE_CASE).containsMatchIn(line)) {
                            labeledOpen++
                        }
                    }
                    inFence = !inFence
                }
            }
            if (openCount > 0) {
                val ratio = labeledOpen.toDouble() / openCount
                assertTrue(
                    ratio >= 0.5,
                    "At least 50% of opening code fences should be language-tagged; got ${(ratio * 100).toInt()}% ($labeledOpen/$openCount)"
                )
            }
        }
    }

    @Nested
    @DisplayName("Platform-specific references")
    inner class PlatformSpecific {

        @Test
        fun `gradlewbat exists when referenced`() {
            val mentionsBat = readme.contains("gradlew.bat", ignoreCase = true)
            if (!mentionsBat) return
            assertTrue(Files.exists(Path.of("gradlew.bat")), "README references gradlew.bat, but it is missing in repo root")
        }

        @Test
        fun `contributing and code of conduct files exist when linked`() {
            val linkRx = Regex("""(?<!!)\[[^\]]+]\(((?![a-z]+://|#)[^)]+)\)""", RegexOption.IGNORE_CASE)
            val relLinks = linkRx.findAll(readme)
                .map { it.groupValues[1] }
                .map { it.substringBefore('#').removePrefix("./").trim() }
                .toSet()

            val requiredIfLinked = listOf("CONTRIBUTING.md", "CODE_OF_CONDUCT.md")
            requiredIfLinked.forEach { name ->
                if (relLinks.any { it.equals(name, ignoreCase = true) }) {
                    assertTrue(Files.exists(Path.of(name)), "README links $name but file not found in repo root")
                }
            }
        }
    }

    @Nested
    @DisplayName("License badge coherence")
    inner class LicenseBadgeCoherence {
        @Test
        fun `license section present when license badge is shown`() {
            val hasBadge = readme.contains("img.shields.io/badge/License-", ignoreCase = true)
            if (!hasBadge) return
            val hasLicenseHeader = lines.any { it.trim().matches(Regex("^##\\s*License\\b", RegexOption.IGNORE_CASE)) }
            assertTrue(hasLicenseHeader, "License badge present but '## License' section not found")
        }
    }
}