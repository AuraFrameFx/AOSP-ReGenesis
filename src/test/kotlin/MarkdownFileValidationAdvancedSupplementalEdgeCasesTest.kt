@file:Suppress("SpellCheckingInspection", "HttpUrlsUsage")

package docs

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

/**
 * Additional supplemental tests complementing MarkdownFileValidationAdvancedSupplementalTest.
 *
 * Testing stack: Kotlin + JUnit 5 (Jupiter).
 * Focus: header uniqueness, relative link targets, footnotes, image transport, and inline code spans.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationAdvancedSupplementalEdgeCasesTest {

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
        val cleaned = noEmoji
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
        return cleaned
    }

    @Nested
    @DisplayName("Headers – uniqueness and structure")
    inner class Headers {

        @Test
        fun `h2 header slugs are unique`() {
            val slugs = lines
                .filter { it.trim().matches(Regex("^##\\s+.*$")) }
                .map { normalizeToSlug(it) }
            if (slugs.isEmpty()) return
            val duplicates = slugs.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            assertTrue(duplicates.isEmpty(), "Duplicate H2 header slugs: $duplicates")
        }

        @Test
        fun `has a single H1 header at top`() {
            val h1Indices = lines.withIndex().filter { it.value.trim().matches(Regex("^#\\s+.*$")) }.map { it.index }
            if (h1Indices.isEmpty()) return
            assertEquals(1, h1Indices.size, "README should contain exactly one H1 header")
            assertTrue(h1Indices.first() <= 5, "H1 header should appear near the top")
        }
    }

    @Nested
    @DisplayName("Links – relative targets and footnotes")
    inner class LinksRelativeAndFootnotes {

        @Test
        fun `relative links point to existing files`() {
            val linkRegex = Regex("\\[[^\\]]+\\]\\((?!https?://|#|mailto:|tel:)([^)]+)\\)")
            val links = linkRegex.findAll(readme).map { it.groupValues[1] }.toList()
            if (links.isEmpty()) return
            val missing = links.map { it.substringBefore('#').trim() }
                .filter { it.isNotBlank() }
                .map { Path.of(it) }
                .filterNot { Files.exists(it) }
                .map { it.toString() }
            assertTrue(missing.isEmpty(), "Broken relative links to files: $missing")
        }

        @Test
        fun `footnote references and definitions are consistent`() {
            val refRegex = Regex("\\[\\^([^\\]]+)]")
            val defRegex = Regex("^\\[\\^(.+?)]:\\s+.+$")
            val refs = refRegex.findAll(readme).map { it.groupValues[1] }.toSet()
            val defs = lines.map { it.trim() }.mapNotNull { defRegex.find(it)?.groupValues?.get(1) }.toSet()
            if (refs.isEmpty() && defs.isEmpty()) return
            val missingDefs = refs - defs
            val unusedDefs = defs - refs
            assertAll(
                { assertTrue(missingDefs.isEmpty(), "Footnote references without definitions: $missingDefs") },
                { assertTrue(unusedDefs.isEmpty(), "Footnote definitions without references: $unusedDefs") }
            )
        }
    }

    @Nested
    @DisplayName("Images – transport and alt text quality")
    inner class ImagesTransport {

        @Test
        fun `no images use insecure http transport`() {
            val httpImages = Regex("!\\[[^\\]]*]\\((http://[^)]+)\\)", RegexOption.IGNORE_CASE).findAll(readme)
                .map { it.groupValues[1] }.toList()
            assertTrue(httpImages.isEmpty(), "Images should use HTTPS: $httpImages")
        }

        @Test
        fun `image alt text is descriptive – not just file names`() {
            val images = Regex("!\\[(.*?)\\]\\(([^)]+)\\)").findAll(readme).toList()
            if (images.isEmpty()) return
            val lazyAlts = images.withIndex().filter { (_, m) ->
                val alt = m.groupValues[1].trim().lowercase(Locale.ROOT)
                val url = m.groupValues[2].substringBeforeLast('#').substringBefore('?').substringAfterLast('/').lowercase(Locale.ROOT)
                alt.isNotEmpty() && (alt == url || alt == url.substringBeforeLast('.'))
            }.map { it.index }
            assertTrue(lazyAlts.isEmpty(), "Images with non-descriptive alt text at indices: $lazyAlts")
        }
    }

    @Nested
    @DisplayName("Badges and licensing – additional families")
    inner class BadgesAndLicensingExtra {

        @Test
        fun `bsd-3-clause license badge matches LICENSE content`() {
            val badgeRegex = Regex("img\\.shields\\.io/.+License-(BSD-3--Clause|BSD%203--Clause)", RegexOption.IGNORE_CASE)
            if (!badgeRegex.containsMatchIn(readme)) return
            val licensePath = Path.of("LICENSE")
            assertTrue(Files.exists(licensePath), "LICENSE file missing despite BSD-3-Clause badge")
            val license = Files.readString(licensePath, StandardCharsets.UTF_8)
            val hasBsd3 = license.contains("BSD 3-Clause", ignoreCase = true) ||
                          license.contains("Redistribution and use in source and binary forms", ignoreCase = true)
            assertTrue(hasBsd3, "LICENSE should include BSD 3-Clause keywords")
        }
    }

    @Nested
    @DisplayName("Code spans and blocks – quality gates")
    inner class CodeQualityGates {

        @Test
        fun `inline code spans are not empty`() {
            val spans = Regex("`([^`]*)`").findAll(readme).toList()
            if (spans.isEmpty()) return
            val empties = spans.withIndex().filter { it.value.groupValues[1].trim().isEmpty() }.map { it.index }
            assertTrue(empties.isEmpty(), "Empty inline code span(s) at indices: $empties")
        }
    }
}