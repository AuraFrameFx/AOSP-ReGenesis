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
    // ---------------------------------------------------------------------
    // Additional supplemental tests (Testing stack: Kotlin + JUnit 5 Jupiter)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Table of Contents – advanced")
    inner class TableOfContentsAdvanced {

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

        private fun extractTocEntries(): List<Pair<String, String>> {
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            if (tocStart < 0) return emptyList()
            val tocBody = lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
            return tocBody.mapNotNull { line ->
                Regex("- \\[(.+?)\\]\\(#(.*?)\\)").find(line.trim())?.let {
                    it.groupValues[1] to it.groupValues[2]
                }
            }.filter { it.second.isNotBlank() }
        }

        @Test
        fun `toc anchors are derived from entry labels`() {
            val entries = extractTocEntries()
            if (entries.isEmpty()) return
            val mismatched = entries.filter { (label, anchor) ->
                normalizeToSlug("## $label") != anchor.trim('-')
            }.map { (_, anchor) -> anchor }
            assertTrue(mismatched.isEmpty(), "ToC anchors not normalized from labels: $mismatched")
        }

        @Test
        fun `toc anchors are unique and follow header order`() {
            val entries = extractTocEntries()
            if (entries.isEmpty()) return

            val anchors = entries.map { it.second.trim('-') }
            val duplicates = anchors.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.toList()
            assertTrue(duplicates.isEmpty(), "Duplicate ToC anchors: $duplicates")

            val headerOrder = lines
                .filter { it.trim().matches(Regex("^##\\s+.*$")) }
                .map { normalizeToSlug(it) }

            val notFound = anchors.filterNot { it in headerOrder }
            if (notFound.isEmpty() && anchors.isNotEmpty()) {
                val indices = anchors.map { headerOrder.indexOf(it) }
                val outOfOrder = indices.zipWithNext()
                    .withIndex()
                    .filter { it.value.second < it.value.first }
                    .map { anchors[it.index + 1] }
                assertTrue(outOfOrder.isEmpty(), "ToC order differs from header order: $outOfOrder")
            }
        }
    }

    @Nested
    @DisplayName("Images and links – advanced")
    inner class ImagesAndLinksAdvanced {

        @Test
        fun `images have non-empty alt text`() {
            val images = Regex("!\\[(.*?)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (images.isEmpty()) return
            val empties = images.map { it.groupValues[1].trim() }
                .withIndex()
                .filter { it.value.isEmpty() }
                .map { it.index }
            assertTrue(empties.isEmpty(), "Images with empty alt text at indices: $empties")
        }

        @Test
        fun `internal markdown link text is not empty`() {
            val links = Regex("\\[([^\\]]*)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (links.isEmpty()) return
            val emptyText = links.withIndex()
                .filter { it.value.groupValues[1].trim().isEmpty() }
                .map { it.index }
            assertTrue(emptyText.isEmpty(), "Found markdown links with empty visible text at indices: $emptyText")
        }
    }

    @Nested
    @DisplayName("Code fences – advanced")
    inner class CodeFencesAdvanced {

        @Test
        fun `at least one fenced block declares language when fences present`() {
            var inFence = false
            var withLang = 0
            var blocks = 0
            for (raw in lines) {
                val t = raw.trim()
                if (t.startsWith("```")) {
                    if (!inFence) {
                        blocks++
                        val lang = t.removePrefix("```").trim()
                        if (lang.isNotEmpty()) withLang++
                        inFence = true
                    } else {
                        inFence = false
                    }
                }
            }
            if (blocks == 0) return
            assertTrue(withLang >= 1, "No fenced code blocks declare a language for syntax highlighting")
        }
    }

    @Nested
    @DisplayName("Build tooling and licensing – advanced")
    inner class BuildAndLicenseAdvanced {

        @Test
        fun `mit license badge matches LICENSE content`() {
            val badgeRegex = Regex("img\\.shields\\.io/.+License-.*MIT", RegexOption.IGNORE_CASE)
            if (!badgeRegex.containsMatchIn(readme)) return
            val licensePath = Path.of("LICENSE")
            assertTrue(Files.exists(licensePath), "LICENSE file missing despite MIT badge")
            val license = Files.readString(licensePath, StandardCharsets.UTF_8)
            val hasMit = license.contains("MIT License", ignoreCase = true) ||
                         license.contains("Permission is hereby granted", ignoreCase = true)
            assertTrue(hasMit, "LICENSE should include MIT license keywords")
        }

        @Test
        fun `gpl license badge matches LICENSE content`() {
            val badgeRegex = Regex("img\\.shields\\.io/.+License-(GPL-3\\.0|GPLv3)", RegexOption.IGNORE_CASE)
            if (!badgeRegex.containsMatchIn(readme)) return
            val licensePath = Path.of("LICENSE")
            assertTrue(Files.exists(licensePath), "LICENSE file missing despite GPL badge")
            val license = Files.readString(licensePath, StandardCharsets.UTF_8)
            assertTrue(license.contains("GNU GENERAL PUBLIC LICENSE", ignoreCase = true),
                "LICENSE should include GPL preamble")
        }
    }
}
/**
 * Additional edge-case validation for README hygiene.
 *
 * Testing stack: Kotlin + JUnit 5 (Jupiter).
 * These tests intentionally mirror the structure and tone of existing tests,
 * focusing on link/image integrity and heading hygiene using the same README sources.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationEdgeCasesTest {

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
    @DisplayName("Link hygiene – edge cases")
    inner class LinkHygieneEdgeCases {

        @Test
        fun `markdown links have non-empty URLs and no whitespace`() {
            val links = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (links.isEmpty()) return

            val empties = links.withIndex()
                .filter { it.value.groupValues[2].trim().isEmpty() }
                .map { it.index }

            val withSpaces = links.withIndex()
                .filter {
                    val url = it.value.groupValues[2]
                    !url.startsWith("#") && url.contains(Regex("\\s"))
                }
                .map { it.index }

            assertAll(
                { assertTrue(empties.isEmpty(), "Links with empty URLs at indices: $empties") },
                { assertTrue(withSpaces.isEmpty(), "Links with unencoded whitespace in URL at indices: $withSpaces") }
            )
        }

        @Test
        fun `relative links point to existing files`() {
            val links = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (links.isEmpty()) return

            val localBroken = mutableListOf<String>()
            for (m in links) {
                val rawUrl = m.groupValues[2].trim()
                if (rawUrl.startsWith("#") ||
                    rawUrl.startsWith("http://", true) ||
                    rawUrl.startsWith("https://", true) ||
                    rawUrl.startsWith("mailto:", true) ||
                    rawUrl.startsWith("tel:", true) ||
                    rawUrl.startsWith("data:", true)
                ) continue

                val pathOnly = rawUrl.substringBefore('#').substringBefore('?').replace("%20", " ")
                if (pathOnly.isBlank()) continue
                if (!Files.exists(Path.of(pathOnly))) {
                    localBroken.add(pathOnly)
                }
            }
            assertTrue(localBroken.isEmpty(), "Broken relative link targets: $localBroken")
        }

        @Test
        fun `internal anchor ids are lowercase-hyphenated`() {
            val anchors = Regex("\\[[^\\]]+\\]\\(#([^)]+)\\)")
                .findAll(readme)
                .map { it.groupValues[1] }
                .toList()

            if (anchors.isEmpty()) return
            val bad = anchors.filterNot { it.matches(Regex("^[a-z0-9]+[a-z0-9-]*[a-z0-9]+$")) }
            assertTrue(bad.isEmpty(), "Anchor IDs not lowercase-hyphenated or with edge hyphens: $bad")
        }
    }

    @Nested
    @DisplayName("Image hygiene – edge cases")
    inner class ImageHygieneEdgeCases {

        @Test
        fun `local images resolve on disk`() {
            val images = Regex("!\\[(.*?)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (images.isEmpty()) return

            val missing = images.mapNotNull { m ->
                val url = m.groupValues[2].trim()
                if (url.startsWith("http://", true) || url.startsWith("https://", true)) return@mapNotNull null
                val pathOnly = url.substringBefore('#').substringBefore('?').replace("%20", " ")
                if (pathOnly.isBlank()) null
                else if (!Files.exists(Path.of(pathOnly))) pathOnly else null
            }.distinct()

            assertTrue(missing.isEmpty(), "Local image(s) missing: $missing")
        }

        @Test
        fun `image alt text is descriptive (not placeholder)`() {
            val images = Regex("!\\[(.*?)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (images.isEmpty()) return

            val placeholders = setOf("image", "screenshot", "diagram", "picture", "img")
            val weak = images.withIndex().filter {
                val alt = it.value.groupValues[1].trim().lowercase(Locale.ROOT)
                alt.isEmpty() || alt in placeholders
            }.map { it.index }

            assertTrue(weak.isEmpty(), "Images with placeholder/empty alt text at indices: $weak")
        }
    }

    @Nested
    @DisplayName("Headings – edge cases")
    inner class HeadingsEdgeCases {

        @Test
        fun `no empty h2 or h3 headings`() {
            val empties = lines.withIndex().filter {
                val t = it.value.trim()
                t.matches(Regex("^#{2,3}\\s*$"))
            }.map { it.index + 1 }
            assertTrue(empties.isEmpty(), "Empty headings at line(s): $empties")
        }
    }
}