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
 * Additional README-focused tests complementing MarkdownFileValidationTest.
 *
 * Testing stack: Kotlin + JUnit 5 (Jupiter).
 * These tests emphasize ToC integrity, image/link hygiene, code fence quality,
 * build wrapper presence, and license consistency.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationAdvancedTest {

    // Additional unit tests appended by PR helper
    // Testing framework: Kotlin + JUnit 5 (Jupiter)

    @Nested
    @DisplayName("Table of Contents – edge cases")
    inner class TableOfContentsEdgeCases {

        /**
         * Convert a Markdown header or arbitrary text into a normalized kebab-case slug suitable for internal anchors.
         *
         * Strips leading Markdown header markers (`#`), removes emoji/symbol characters, lowercases ASCII letters,
         * removes any characters other than ASCII letters, digits, spaces, and hyphens, collapses whitespace and
         * repeated hyphens into single hyphens, and trims leading/trailing hyphens.
         *
         * The result may be an empty string if the input contains no ASCII alphanumeric characters after normalization.
         *
         * @param text Input header or text (may include leading `#` markers).
         * @return Normalized kebab-case slug for use as an anchor target.
         */
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
        fun `slug normalization strips diacritics and ampersands`() {
            assertEquals("caf-rsum", normalizeToSlug("## Café & Résumé"))
        }

        @Test
        fun `slug normalization retains numbers and collapses dots`() {
            assertEquals("1-overview", normalizeToSlug("## 1. Overview"))
        }

        @Test
        fun `slug normalization handles em-dashes and emojis`() {
            assertEquals("setup-part-2", normalizeToSlug("## 🔧 Setup — Part 2"))
        }
    }

    @Nested
    @DisplayName("Internal anchors validity")
    inner class InternalAnchors {

        /**
         * Convert a Markdown header or arbitrary text into a normalized kebab-case slug suitable for internal anchors.
         *
         * Strips leading Markdown header markers (`#`), removes emoji/symbol characters, lowercases ASCII letters,
         * removes any characters other than ASCII letters, digits, spaces, and hyphens, collapses whitespace and
         * repeated hyphens into single hyphens, and trims leading/trailing hyphens.
         *
         * The result may be an empty string if the input contains no ASCII alphanumeric characters after normalization.
         *
         * @param text Input header or text (may include leading `#` markers).
         * @return Normalized kebab-case slug for use as an anchor target.
         */
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

        /**
         * Collects the set of Markdown header slugs present in the loaded README.
         *
         * Scans the file lines for Markdown headings (levels 1–6), converts each matching
         * header to its normalized slug using `normalizeToSlug`, and returns the unique
         * slugs as a set.
         *
         * @return a set of normalized header slugs found in the README (duplicates removed).
         */
        private fun headerSlugs(): Set<String> {
            return lines
                .filter { it.trim().matches(Regex("^#{1,6}\\s+.*$")) }
                .map { normalizeToSlug(it) }
                .toSet()
        }

        @Test
        fun `internal markdown anchors reference existing headers`() {
            val withoutFences = readme.replace(Regex("```[\\s\\S]*?```", RegexOption.MULTILINE), "")
            val anchorRegex = Regex("\\[[^\\]]+\\]\\(#([^)]+)\\)")
            val anchors = anchorRegex.findAll(withoutFences).map { it.groupValues[1].trim() }.toList()
            if (anchors.isEmpty()) return
            val slugs = headerSlugs()
            val missing = anchors.filter { it.isNotBlank() && it !in slugs }.distinct()
            assertTrue(missing.isEmpty(), "Anchor(s) not found in headers: $missing")
        }
    }

    @Nested
    @DisplayName("Code fences integrity")
    inner class FenceIntegrity {
        /**
         * Verifies that all fenced code blocks in the README are properly closed.
         *
         * Counts lines that start with triple backticks (```) and asserts the total is even,
         * failing the test if there is an unmatched/open fence marker.
         */
        @Test
        fun `all code fences are properly closed`() {
            val fenceCount = lines.count { it.trim().startsWith("```") }
            assertTrue(fenceCount % 2 == 0, "Uneven number of code fence markers (```), fenceCount=$fenceCount")
        }
    }

    @Nested
    @DisplayName("README structure and style")
    inner class ReadmeStructureAndStyle {

        @Test
        fun `has a single H1 title`() {
            val h1Count = lines.count { it.trim().matches(Regex("^#\\s+.+$")) }
            assertEquals(1, h1Count, "README should contain exactly one top-level '# ' title")
        }

        @Test
        fun `no trailing whitespace on any line`() {
            val offenders = lines.withIndex()
                .filter { it.value.matches(Regex(".*\\s+$")) }
                .map { it.index + 1 }
                .toList()
            assertTrue(offenders.isEmpty(), "Trailing whitespace at lines: $offenders")
        }

        /**
         * Fails the test if the README contains external `http://` links that should use HTTPS.
         *
         * Scans the README (excluding fenced code blocks) for `http://` URLs used in link targets and asserts that
         * any insecure links are limited to allowed local or example prefixes (localhost, loopback addresses, example.com).
         */
        @Test
        fun `prefer HTTPS for external links`() {
            val withoutFences = readme.replace(Regex("```[\\s\\S]*?```", RegexOption.MULTILINE), "")
            val httpRegex = Regex("\\((http://[^)]+)\\)")
            val all = httpRegex.findAll(withoutFences).map { it.groupValues[1] }.toList()
            val allowedPrefixes = listOf(
                "http://localhost",
                "http://127.0.0.1",
                "http://0.0.0.0",
                "http://[::1]",
                "http://example.com",
                "http://www.example.com"
            )
            val insecure = all.filter { url -> allowedPrefixes.none { url.startsWith(it) } }
            assertTrue(insecure.isEmpty(), "Use HTTPS for external links where possible: $insecure")
        }

        @Test
        fun `README has a License section when LICENSE file exists`() {
            val lic = Path.of("LICENSE")
            if (Files.exists(lic)) {
                val hasLicenseSection = lines.any {
                    it.trim().matches(Regex("^##\\s*(?:[\\p{So}\\p{Sk}]\\s*)?Licen[cs]e(s)?\\s*$", RegexOption.IGNORE_CASE))
                }
                assertTrue(hasLicenseSection, "Expected a '## License' section in README when LICENSE file exists")
            }
        }
    }

    private lateinit var readmePath: Path
    private lateinit var readme: String
    private lateinit var lines: List<String>

    /**
     * Locates and loads the repository README into shared test state before all tests run.
     *
     * Searches several candidate paths (README.md, Readme.md, readme.md, docs/README.md) for an existing file,
     * reads its UTF-8 contents into `readme`, splits it into `lines`, and stores the resolved path in `readmePath`.
     *
     * Fails the test suite with an error if no README is found, and asserts that the loaded README is not blank.
     */
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
    @DisplayName("Table of Contents – advanced checks")
    inner class TableOfContentsAdvanced {

        /**
         * Convert a Markdown header or arbitrary text into a normalized kebab-case slug suitable for internal anchors.
         *
         * Strips leading Markdown header markers (`#`), removes emoji/symbol characters, lowercases ASCII letters,
         * removes any characters other than ASCII letters, digits, spaces, and hyphens, collapses whitespace and
         * repeated hyphens into single hyphens, and trims leading/trailing hyphens.
         *
         * The result may be an empty string if the input contains no ASCII alphanumeric characters after normalization.
         *
         * @param text Input header or text (may include leading `#` markers).
         * @return Normalized kebab-case slug for use as an anchor target.
         */
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

        /**
         * Extracts anchor targets from the README's "## 📋 Table of Contents" section.
         *
         * Locates the ToC heading (matches `##` + emoji + "Table of Contents"), asserts it exists, then reads consecutive
         * non-blank lines after that heading and extracts anchors from list entries of the form `- [text](#anchor)`.
         *
         * @return A list of non-empty anchor names (the portion after `#`) in the ToC, in the order they appear.
         */
        private fun extractTocAnchors(): List<String> {
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            assertTrue(tocStart >= 0, "Table of Contents section not found")
            val tocBody = lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
            return tocBody.mapNotNull { line ->
                Regex("- \\[(.+?)\\]\\(#(.*?)\\)").find(line.trim())?.groupValues?.getOrNull(2)
            }.filter { it.isNotBlank() }
        }

        @Test
        fun `toc anchors are unique`() {
            val anchors = extractTocAnchors()
            val dupes = anchors.groupingBy { it }.eachCount().filter { it.value > 1 }
            assertTrue(dupes.isEmpty(), "Duplicate ToC anchors detected: ${dupes.keys}")
        }

        @Test
        fun `toc order matches the document section order`() {
            val anchors = extractTocAnchors().map { it.trim('-') }
            val headers = lines.withIndex().filter { it.value.trim().startsWith("## ") }
            val indexBySlug = headers.associate { normalizeToSlug(it.value) to it.index }
            val anchorIndices = anchors.mapNotNull { indexBySlug[it] }
            val sorted = anchorIndices.sorted()
            assertEquals(sorted, anchorIndices, "ToC order should follow section order in the document")
        }

        @Test
        fun `slug normalization handles emoji, punctuation, and spacing`() {
            assertEquals("security-privacy", normalizeToSlug("## 🔒 Security & Privacy!!"))
            assertEquals("getting-started", normalizeToSlug("##   Getting   Started "))
            assertEquals("architecture-overview", normalizeToSlug("## Architecture – Overview"))
        }
    }

    @Nested
    @DisplayName("Images and links hygiene")
    inner class ImagesAndLinks {

        /**
         * Verifies that every Markdown image in the README has non-empty alt text and that local image files exist.
         *
         * Scans the README for image tags (`![alt](url)`), asserts the captured alt text is not empty, and for non-remote
         * URLs resolves the path relative to the README file (ignoring any fragment `#...` or query `?...`) and asserts the
         * target file exists.
         */
        @Test
        fun `images have alt text and local images exist`() {
            val imageRegex = Regex("!\\[(.*?)\\]\\(([^)]+)\\)")
            val matches = imageRegex.findAll(readme).toList()
            matches.forEach { m ->
                val alt = m.groupValues[1].trim()
                val url = m.groupValues[2].trim()
                assertTrue(alt.isNotEmpty(), "Image has empty alt text: $url")
                val isRemote = url.startsWith("http://") || url.startsWith("https://")
                if (!isRemote) {
                    val clean = url.substringBefore('#').substringBefore('?')
                    val resolved = readmePath.parent.resolve(clean).normalize()
                    assertTrue(Files.exists(resolved), "Local image not found: $url (resolved: $resolved)")
                }
            }
        }

        @Test
        fun `no TODO-like tokens outside code fences`() {
            val withoutFences = readme.replace(Regex("```[\\s\\S]*?```", RegexOption.MULTILINE), "")
            val tokens = Regex("\\b(TODO|TBD|FIXME|HACK)\\b", RegexOption.IGNORE_CASE).findAll(withoutFences).map { it.value }.toList()
            assertTrue(tokens.isEmpty(), "Found TODO-like tokens outside code fences: $tokens")
        }

        @Test
        fun `all local markdown links resolve to existing files or directories`() {
            // Matches [text](path) where path does not start with http or #
            val linkRegex = Regex("\\[[^\\]]+\\]\\((?!https?://|#)([^)]+)\\)")
            val targets = linkRegex.findAll(readme).map { it.groupValues[1] }.toList()
            targets.forEach { target ->
                val clean = target.substringBefore('#').substringBefore('?')
                // Skip ephemeral build outputs
                if (clean.startsWith("build/") || clean.startsWith("out/")) return@forEach
                val resolved = readmePath.parent.resolve(clean).normalize()
                assertTrue(Files.exists(resolved), "Local link target not found: $target (resolved: $resolved)")
            }
        }
    }

    @Nested
    @DisplayName("Code fences quality")
    inner class CodeFences {

        @Test
        fun `fenced code blocks declare a language`() {
            var inFence = false
            var typed = 0
            var untyped = 0
            for (l in lines) {
                val t = l.trim()
                if (t.startsWith("```")) {
                    if (!inFence) { // opening
                        val after = t.removePrefix("```").trim()
                        if (after.isBlank()) untyped++ else typed++
                        inFence = true
                    } else { // closing
                        inFence = false
                    }
                }
            }
            assertTrue(typed > 0, "Expected at least one fenced code block with a language (e.g., ```bash, ```kotlin)")
            assertEquals(0, untyped, "All fenced code blocks should declare a language for syntax highlighting")
        }
    }

    @Nested
    @DisplayName("Build tooling and licensing")
    inner class BuildAndLicense {

        @Test
        fun `gradle wrapper files exist when referenced by README`() {
            if (readme.contains("gradlew")) {
                assertAll(
                    { assertTrue(Files.exists(Path.of("gradlew")), "Missing gradlew script") },
                    { assertTrue(Files.exists(Path.of("gradlew.bat")), "Missing gradlew.bat script") },
                    { assertTrue(Files.exists(Path.of("gradle/wrapper/gradle-wrapper.properties")), "Missing gradle/wrapper/gradle-wrapper.properties") }
                )
            }
        }

        /**
         * Verifies that an MIT license badge in the README corresponds to an actual LICENSE file.
         *
         * If the README contains the "img.shields.io/badge/License-MIT" badge, this test asserts that a
         * LICENSE file exists at the repository root and that its contents mention "MIT" (case-insensitive).
         * If the badge is not present, the test is a no-op.
         */
        @Test
        fun `license badge matches LICENSE content`() {
            if (readme.contains("img.shields.io/badge/License-MIT")) {
                val licensePath = Path.of("LICENSE")
                assertTrue(Files.exists(licensePath), "LICENSE file missing despite MIT badge")
                val license = Files.readString(licensePath, StandardCharsets.UTF_8)
                assertTrue(license.contains("MIT", ignoreCase = true), "LICENSE should mention MIT to align with badge")
            }
        }

        @Test
        fun `has Overview and Getting Started sections`() {
            val hasOverview = lines.any {
                it.trim().matches(Regex("^##\\s*(?:[\\p{So}\\p{Sk}]\\s*)?Overview\\s*$", RegexOption.IGNORE_CASE))
            }
            val hasGettingStarted = lines.any {
                it.trim().matches(Regex("^##\\s*(?:[\\p{So}\\p{Sk}]\\s*)?(Getting\\s+Started|Quickstart)\\s*$", RegexOption.IGNORE_CASE))
            }
            assertAll(
                { assertTrue(hasOverview, "Expected an '## Overview' section") },
                { assertTrue(hasGettingStarted, "Expected a '## Getting Started' or '## Quickstart' section") }
            )
        }
    }
    // -------------------------------------------------------------------------
    // Additional tests auto-generated by PR helper
    // Testing library/framework: Kotlin + JUnit 5 (Jupiter)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Slug normalization – additional cases")
    inner class SlugNormalizationAdditionalCases {

        /**
         * Convert a Markdown header or arbitrary text into a normalized kebab-case slug suitable for internal anchors.
         *
         * Strips leading Markdown header markers (`#`), removes emoji/symbol characters, lowercases ASCII letters,
         * removes any characters other than ASCII letters, digits, spaces, and hyphens, collapses whitespace and
         * repeated hyphens into single hyphens, and trims leading/trailing hyphens.
         *
         * The result may be an empty string if the input contains no ASCII alphanumeric characters after normalization.
         *
         * @param text Input header or text (may include leading `#` markers).
         * @return Normalized kebab-case slug for use as an anchor target.
         */
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
        fun `underscores are removed, not converted to hyphens`() {
            assertEquals("helloworld", normalizeToSlug("## Hello_World"))
        }

        @Test
        fun `slashes are removed, spaces become hyphens`() {
            assertEquals("ab-c", normalizeToSlug("## A/B C"))
        }

        @Test
        fun `parentheses are stripped but content kept`() {
            assertEquals("hello-world-2025", normalizeToSlug("## Hello (World) 2025"))
        }

        @Test
        fun `non latin only results in empty slug`() {
            assertEquals("", normalizeToSlug("## Пример"))
        }

        @Test
        fun `trims leading and trailing hyphens after cleanup`() {
            assertEquals("roadmap-q4", normalizeToSlug("## -- Roadmap — Q4 --"))
        }
    }

    @Nested
    @DisplayName("Internal anchors – formatting")
    inner class InternalAnchorsFormatting {

        @Test
        fun `anchors follow kebab-case pattern`() {
            val withoutFences = readme.replace(Regex("```[\\s\\S]*?```", RegexOption.MULTILINE), "")
            val anchorRegex = Regex("\\[[^\\]]+\\]\\(#([^)]+)\\)")
            val anchors = anchorRegex.findAll(withoutFences).map { it.groupValues[1].trim() }.toList()
            if (anchors.isEmpty()) return
            val bad = anchors.filterNot { it.matches(Regex("^[a-z0-9]+(?:-[a-z0-9]+)*$")) }
            assertTrue(bad.isEmpty(), "Anchor(s) not in kebab-case: $bad")
        }
    }

    @Nested
    @DisplayName("Images and links – protocol hygiene")
    inner class ImagesAndLinksMore {

        @Test
        fun `remote images use HTTPS except for local dev`() {
            val imageRegex = Regex("!\\[(.*?)\\]\\(([^)]+)\\)")
            val matches = imageRegex.findAll(readme).toList()
            val httpImages = matches.map { it.groupValues[2].trim() }
                .filter { it.startsWith("http://") }
            val allowedPrefixes = listOf(
                "http://localhost",
                "http://127.0.0.1",
                "http://0.0.0.0",
                "http://[::1]",
                "http://example.com",
                "http://www.example.com"
            )
            val insecure = httpImages.filter { url -> allowedPrefixes.none { url.startsWith(it) } }
            assertTrue(insecure.isEmpty(), "Use HTTPS for remote images where possible: $insecure")
        }
    }

    @Nested
    @DisplayName("Table of Contents – presence check")
    inner class TableOfContentsPresence {

        /**
         * Verifies the README contains exactly one "## 📋 Table of Contents" heading.
         *
         * Counts lines that match the exact heading pattern (level-2 header with the clipboard emoji
         * followed by "Table of Contents") and asserts the count is 1.
         */
        @Test
        fun `README contains exactly one ToC section`() {
            val count = lines.count { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            assertEquals(1, count, "Expected exactly one '## 📋 Table of Contents' section")
        }
    }

    @Nested
    @DisplayName("Build tooling and licensing – extended")
    inner class BuildAndLicenseExtended {

        @Test
        fun `maven wrapper files exist when referenced by README`() {
            if (readme.contains("mvnw")) {
                assertAll(
                    { assertTrue(Files.exists(Path.of("mvnw")), "Missing mvnw script") },
                    { assertTrue(Files.exists(Path.of("mvnw.cmd")), "Missing mvnw.cmd script") }
                )
            }
        }

        @Test
        fun `apache 2 license badge matches LICENSE content`() {
            if (readme.contains("img.shields.io/badge/License-Apache-2.0")) {
                val licensePath = Path.of("LICENSE")
                assertTrue(Files.exists(licensePath), "LICENSE file missing despite Apache-2.0 badge")
                val license = Files.readString(licensePath, StandardCharsets.UTF_8)
                val ok = license.contains("Apache License", ignoreCase = true) ||
                    license.contains("Apache 2", ignoreCase = true) ||
                    license.contains("Apache License, Version 2.0", ignoreCase = true)
                assertTrue(ok, "LICENSE should mention Apache 2.0 to align with badge")
            }
        }

        @Test
        fun `contributing section present when CONTRIBUTING file exists`() {
            val candidates = listOf("CONTRIBUTING.md", "Contributing.md", "docs/CONTRIBUTING.md")
            val path = candidates.map { Path.of(it) }.firstOrNull { Files.exists(it) }
            if (path != null) {
                val hasSection = lines.any {
                    it.trim().matches(Regex("^##\\s*(?:[\\p{So}\\p{Sk}]\\s*)?Contributing\\s*$", RegexOption.IGNORE_CASE))
                }
                assertTrue(hasSection, "Expected a '## Contributing' section in README when CONTRIBUTING.md exists")
            }
        }
    }
}