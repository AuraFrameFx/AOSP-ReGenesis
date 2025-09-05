@file:Suppress("SpellCheckingInspection", "HttpUrlsUsage")

package docs

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

/**
 * Supplemental tests complementing MarkdownFileValidationAdvancedTest.
 *
 * Testing stack: Kotlin + JUnit 5 (Jupiter).
 * Focus: stronger ToC/link integrity, fence robustness, and license alignment.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationAdvancedSupplementalTest {

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

    @Nested
    @DisplayName("Table of Contents – supplemental checks")
    inner class TableOfContentsSupplemental {

        private fun normalizeToSlug(text: String): String {
            val header = text
                .replace(Regex("^\\s*#+\\s*"), "")
                .trim()
            val noEmoji = header.replace(Regex("[\\p{So}\\p{Sk}]"), "")
            val cleaned = noEmoji
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
                .trim('-')
            return cleaned
        }

        private fun extractTocAnchors(): List<String> {
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            assertTrue(tocStart >= 0, "Table of Contents section not found")
            val tocBody = lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
            return tocBody.mapNotNull { line ->
                Regex("- \\[(.+?)\\]\\(#(.*?)\\)").find(line.trim())?.groupValues?.getOrNull(2)
            }.filter { it.isNotBlank() }
        }

        @Test
        fun `toc anchors resolve to existing headers`() {
            val anchors = extractTocAnchors().map { it.trim('-') }
            val headerSlugs = lines
                .filter { it.trim().matches(Regex("^##\\s+.*$")) }
                .map { normalizeToSlug(it) }
                .toSet()
            val missing = anchors.filterNot { it in headerSlugs }
            assertTrue(missing.isEmpty(), "ToC anchors with no matching section headers: $missing")
        }

        @Test
        fun `slug normalization additional cases`() {
            assertEquals("section-123", normalizeToSlug("## Section 123"))
            assertEquals("cafe", normalizeToSlug("## Café"))
            assertEquals("a-b-c", normalizeToSlug("## A  -  B --- C"))
        }
    }

    @Nested
    @DisplayName("Images and links – supplemental")
    inner class ImagesAndLinksSupplemental {

        private fun normalizeToSlug(text: String): String {
            val header = text
                .replace(Regex("^\\s*#+\\s*"), "")
                .trim()
            val noEmoji = header.replace(Regex("[\\p{So}\\p{Sk}]"), "")
            val cleaned = noEmoji
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
                .trim('-')
            return cleaned
        }

        @Test
        fun `internal markdown anchor links resolve to headers`() {
            val anchors = Regex("\\[[^\\]]+\\]\\(#([^)]+)\\)")
                .findAll(readme)
                .map { it.groupValues[1].trim('-') }
                .toList()
            if (anchors.isEmpty()) return
            val headerSlugs = lines
                .filter { it.trim().matches(Regex("^#+\\s+.*$")) }
                .map { normalizeToSlug(it) }
                .toSet()
            val missing = anchors.filterNot { it in headerSlugs }
            assertTrue(missing.isEmpty(), "Internal links to anchors not found: $missing")
        }
    }

    @Nested
    @DisplayName("Code fences – supplemental")
    inner class CodeFencesSupplemental {

        @Test
        fun `no unclosed fenced code blocks`() {
            var inFence = false
            for (l in lines) {
                val t = l.trim()
                if (t.startsWith("```")) {
                    inFence = !inFence
                }
            }
            assertTrue(!inFence, "Detected an unclosed fenced code block")
        }

        @Test
        fun `fenced code blocks are not empty`() {
            var inFence = false
            var nonBlankCount = 0
            val emptyBlocks = mutableListOf<Int>()
            var startLine = -1
            lines.forEachIndexed { idx, raw ->
                val t = raw.trim()
                if (t.startsWith("```")) {
                    if (!inFence) {
                        inFence = true
                        nonBlankCount = 0
                        startLine = idx + 1
                    } else {
                        if (nonBlankCount == 0) emptyBlocks.add(startLine)
                        inFence = false
                    }
                } else if (inFence) {
                    if (t.isNotBlank()) nonBlankCount++
                }
            }
            assertTrue(emptyBlocks.isEmpty(), "Found empty fenced code block(s) starting at line(s): $emptyBlocks")
        }
    }

    @Nested
    @DisplayName("Build tooling and licensing – supplemental")
    inner class BuildAndLicenseSupplemental {

        @Test
        fun `license file exists when License section present`() {
            val hasLicenseSection = lines.any { it.trim().matches(Regex("^##\\s*License\\s*$", RegexOption.IGNORE_CASE)) }
            if (hasLicenseSection) {
                assertTrue(Files.exists(Path.of("LICENSE")), "LICENSE file missing but README has a License section")
            }
        }

        @Test
        fun `apache 2 license badge matches LICENSE content`() {
            val badgeRegex = Regex("img\\.shields\\.io/.+License-(Apache%202\\.0|Apache-2\\.0)", RegexOption.IGNORE_CASE)
            if (badgeRegex.containsMatchIn(readme)) {
                val licensePath = Path.of("LICENSE")
                assertTrue(Files.exists(licensePath), "LICENSE file missing despite Apache 2.0 badge")
                val license = Files.readString(licensePath, StandardCharsets.UTF_8)
                assertAll(
                    { assertTrue(license.contains("Apache License", ignoreCase = true), "LICENSE should mention 'Apache License'") },
                    { assertTrue(license.contains("Version 2.0", ignoreCase = true), "LICENSE should mention 'Version 2.0'") }
                )
            }
        }
    }
}