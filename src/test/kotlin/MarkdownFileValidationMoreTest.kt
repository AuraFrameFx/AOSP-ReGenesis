@file:Suppress("SpellCheckingInspection", "HttpUrlsUsage")

package docs

/*
  Additional unit tests focusing on README.md validations introduced/modified in the PR diff.
  Testing stack: Kotlin + JUnit 5 (Jupiter). These tests complement MarkdownFileValidationTest.
*/

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationMoreTest {

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
    @DisplayName("Formatting hygiene")
    inner class FormattingHygiene {

        @Test
        fun `no trailing whitespace on any line`() {
            val trailing = lines.mapIndexedNotNull { idx, line ->
                if (Regex("[\\t ]+$").containsMatchIn(line)) idx + 1 else null
            }
            assertTrue(trailing.isEmpty(), "Trailing whitespace found at lines: $trailing")
        }

        @Test
        fun `file ends with a newline`() {
            assertTrue(readme.endsWith("\n"), "README should end with a newline")
        }

        @Test
        fun `no more than two consecutive blank lines`() {
            var maxStreak = 0
            var streak = 0
            for (line in lines) {
                if (line.isBlank()) {
                    streak++
                    if (streak > maxStreak) maxStreak = streak
                } else {
                    streak = 0
                }
            }
            assertTrue(maxStreak <= 2, "Found more than two consecutive blank lines (max=$maxStreak)")
        }
    }

    @Nested
    @DisplayName("Table of Contents - extra checks")
    inner class TocExtra {

        private fun isTocLine(s: String) = s.trim().matches(Regex("^- \\[.+]\\(#.+\\)\\s*$"))

        @Test
        fun `table of contents has at least 4 entries and uses bullet list`() {
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            assertTrue(tocStart >= 0, "Table of Contents section not found")
            val tocBody = lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
            val entries = tocBody.filter { isTocLine(it) }
            assertTrue(entries.size >= 4, "Expected at least 4 ToC entries; found ${entries.size}")
            val nonBulleted = tocBody.filter { it.isNotBlank() && !isTocLine(it) }
            assertTrue(nonBulleted.isEmpty(), "ToC should use '- [text](#anchor)' entries only; offending: $nonBulleted")
        }
    }

    @Nested
    @DisplayName("Links - extra validations")
    inner class LinksExtra {

        @Test
        fun `all markdown links have non-empty text`() {
            val bad = Regex("""\[\]\([^)]+\)""").findAll(readme).toList()
            assertTrue(bad.isEmpty(), "Found links with empty text: $bad")
        }

        @Test
        fun `no duplicate relative link targets`() {
            val linkRegex = Regex("""(?<!!)\[[^\]]+]\(((?![a-z]+://|#)[^)]+)\)""", RegexOption.IGNORE_CASE)
            val counts = linkRegex.findAll(readme)
                .map { it.groupValues[1].substringBefore('#').removePrefix("./").trim() }
                .filter { it.isNotBlank() }
                .groupingBy { it.lowercase(Locale.ROOT) }
                .eachCount()
            val dups = counts.filter { it.value > 1 }.keys
            assertTrue(dups.isEmpty(), "Duplicate relative link targets referenced multiple times: $dups")
        }
    }

    @Nested
    @DisplayName("Headers - extra validations")
    inner class HeadersExtra {

        @Test
        fun `headers should not exceed 80 characters`() {
            val longHeaders = lines.filter { it.trim().matches(Regex("^#{1,6}\\s+.*$")) && it.trim().length > 82 }
            assertTrue(longHeaders.isEmpty(), "Overly long markdown headers (>80 chars): $longHeaders")
        }

        @Test
        fun `contains License and Contributing sections if linked`() {
            val mentionsContrib = readme.contains("(#contributing", ignoreCase = true)
            val mentionsLicense = readme.contains("(#license", ignoreCase = true)
            if (mentionsContrib) {
                val hasContribHeader = lines.any { it.trim().matches(Regex("^##\\s*Contributing\\b", RegexOption.IGNORE_CASE)) }
                assertTrue(hasContribHeader, "README links to Contributing but no '## Contributing' header found")
            }
            if (mentionsLicense) {
                val hasLicenseHeader = lines.any { it.trim().matches(Regex("^##\\s*License\\b", RegexOption.IGNORE_CASE)) }
                assertTrue(hasLicenseHeader, "README links to License but no '## License' header found")
            }
        }
    }

    @Nested
    @DisplayName("Code blocks - extra")
    inner class CodeBlocksExtra {

        @Test
        fun `unlabeled fenced code blocks are not used`() {
            val unlabeled = Regex("(?m)^```\\s*$").findAll(readme).count()
            assertEquals(0, unlabeled, "Avoid unlabeled triple backtick fences; specify a language")
        }

        @Test
        fun `contains at least one Kotlin or Groovy build script snippet`() {
            val langs = Regex("```(\\w+)")
                .findAll(readme)
                .map { it.groupValues[1].lowercase(Locale.ROOT) }
                .toSet()
            assertTrue(
                langs.any { it in setOf("kotlin", "kts", "groovy", "gradle") },
                "Expected build script examples (kotlin/groovy/gradle)"
            )
        }
    }

    @Nested
    @DisplayName("Tools and scripts presence")
    inner class ToolsAndScripts {

        @Test
        fun `gradle wrapper exists when used in README commands`() {
            val mentionsWrapper = readme.contains("./gradlew")
            if (!mentionsWrapper) return
            assertTrue(Files.exists(Path.of("gradlew")), "README uses ./gradlew but gradle wrapper not found at project root")
        }
    }
}