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

    // ---------------------------------------------------------------------
    // Additional extended coverage tests (Kotlin + JUnit 5 Jupiter)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Slug normalization – edge cases")
    inner class SlugNormalizationEdgeCases {

        private fun normalizeToSlug(text: String): String {
            val header = text.replace(Regex("^\\s*#+\\s*"), "").trim()
            val decomposed = Normalizer.normalize(header, Normalizer.Form.NFD)
            val noDiacritics = decomposed.replace(Regex("\\p{M}+"), "")
            val noEmoji = noDiacritics.replace(Regex("[\\p{So}\\p{Sk}]"), "")
            val cleaned = noEmoji
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .replace(Regex("\\s+"), "-")
                .replace(Regex("-+"), "-")
                .trim('-')
            return cleaned
        }

        @Test
        fun `emoji and punctuation removed`() {
            assertEquals("getting-started", normalizeToSlug("## 🚀 Getting Started!"))
        }

        @Test
        fun `en dash normalized via spaces`() {
            assertEquals("a-b", normalizeToSlug("## A – B"))
        }

        @Test
        fun `underscores removed and spaces collapsed`() {
            assertEquals("a-b-c", normalizeToSlug("## A_B   C"))
        }

        @Test
        fun `accents stripped and em dash removed`() {
            assertEquals("naive-cafe-test", normalizeToSlug("## Naïve Café — Test"))
        }
    }

    @Nested
    @DisplayName("Headers – uniqueness with ToC present")
    inner class HeadersUniqueness {

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
        fun `h2 slugs are unique when ToC exists`() {
            val hasToc = lines.any { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            if (!hasToc) return
            val slugs = lines
                .filter { it.trim().matches(Regex("^##\\s+.*$")) }
                .map { normalizeToSlug(it) }
            if (slugs.isEmpty()) return
            val duplicates = slugs.groupingBy { it }.eachCount().filter { it.value > 1 }.keys.toList()
            assertTrue(duplicates.isEmpty(), "Duplicate H2 header slugs: $duplicates")
        }
    }

    @Nested
    @DisplayName("Images and links – filesystem validation")
    inner class ImagesAndLinksFilesystem {

        @Test
        fun `relative links point to existing files`() {
            val linkRegex = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
            val urls = linkRegex.findAll(readme).map { it.groupValues[2].trim() }.toList()
            val relative = urls
                .filter { url ->
                    val u = url.lowercase(Locale.ROOT)
                    !u.startsWith("#") &&
                    !u.startsWith("http://") &&
                    !u.startsWith("https://") &&
                    !u.startsWith("mailto:") &&
                    !u.startsWith("tel:") &&
                    !u.startsWith("data:")
                }
                .map { it.substringBefore('#').substringBefore('?') }
            if (relative.isEmpty()) return
            val baseDir = readmePath.parent ?: Path.of(".")
            val missing = relative.filterNot { Files.exists(baseDir.resolve(it).normalize()) }
            assertTrue(missing.isEmpty(), "Relative links missing targets: $missing")
        }

        @Test
        fun `relative images point to existing files`() {
            val imageRegex = Regex("!\\[(.*?)\\]\\(([^)]+)\\)")
            val urls = imageRegex.findAll(readme).map { it.groupValues[2].trim() }.toList()
            val relative = urls
                .filter { url ->
                    val u = url.lowercase(Locale.ROOT)
                    !u.startsWith("http://") &&
                    !u.startsWith("https://") &&
                    !u.startsWith("data:")
                }
                .map { it.substringBefore('#').substringBefore('?') }
            if (relative.isEmpty()) return
            val baseDir = readmePath.parent ?: Path.of(".")
            val missing = relative.filterNot { Files.exists(baseDir.resolve(it).normalize()) }
            assertTrue(missing.isEmpty(), "Relative image paths missing targets: $missing")
        }
    }

    @Nested
    @DisplayName("Code fences – language tags")
    inner class CodeFencesLanguageTags {

        @Test
        fun `language tags are plausible when provided`() {
            var inFence = false
            val bad = mutableListOf<String>()
            for (raw in lines) {
                val t = raw.trim()
                if (t.startsWith("```")) {
                    if (!inFence) {
                        val lang = t.removePrefix("```").trim()
                        if (lang.isNotEmpty() && !lang.matches(Regex("^[a-zA-Z0-9.+:_-]{1,30}$"))) {
                            bad.add(lang)
                        }
                        inFence = true
                    } else {
                        inFence = false
                    }
                }
            }
            if (bad.isEmpty()) return
            assertTrue(false, "Suspicious fenced code block language tags: $bad")
        }
    }

    @Nested
    @DisplayName("Repository conventions – conditional files")
    inner class RepoConventions {

        @Test
        fun `code of conduct file exists when section present`() {
            val hasSection = lines.any { it.trim().matches(Regex("^##\\s*Code of Conduct\\s*$", RegexOption.IGNORE_CASE)) }
            if (!hasSection) return
            val exists = Files.exists(Path.of("CODE_OF_CONDUCT.md")) || Files.exists(Path.of("docs/CODE_OF_CONDUCT.md"))
            assertTrue(exists, "CODE_OF_CONDUCT.md missing but README has a Code of Conduct section")
        }

        @Test
        fun `contributing file exists when section present`() {
            val hasSection = lines.any { it.trim().matches(Regex("^##\\s*Contributing\\s*$", RegexOption.IGNORE_CASE)) }
            if (!hasSection) return
            val exists = Files.exists(Path.of("CONTRIBUTING.md")) || Files.exists(Path.of("docs/CONTRIBUTING.md"))
            assertTrue(exists, "CONTRIBUTING.md missing but README has a Contributing section")
        }

        @Test
        fun `security policy file exists when section present`() {
            val hasSection = lines.any { it.trim().matches(Regex("^##\\s*Security\\s*$", RegexOption.IGNORE_CASE)) }
            if (!hasSection) return
            val exists = Files.exists(Path.of("SECURITY.md")) || Files.exists(Path.of("docs/SECURITY.md"))
            assertTrue(exists, "SECURITY.md missing but README has a Security section")
        }

        @Test
        fun `changelog file exists when section present`() {
            val hasSection = lines.any { it.trim().matches(Regex("^##\\s*(Changelog|Release Notes)\\s*$", RegexOption.IGNORE_CASE)) }
            if (!hasSection) return
            val exists = Files.exists(Path.of("CHANGELOG.md")) || Files.exists(Path.of("docs/CHANGELOG.md"))
            assertTrue(exists, "CHANGELOG.md missing but README has a Changelog/Release Notes section")
        }

        @Test
        fun `github actions badge implies workflow directory exists`() {
            val hasBadge = readme.contains("github/actions/workflow/status", ignoreCase = true)
            if (!hasBadge) return
            assertTrue(Files.exists(Path.of(".github/workflows")), "GitHub Actions badge present but .github/workflows directory missing")
        }
    }
}