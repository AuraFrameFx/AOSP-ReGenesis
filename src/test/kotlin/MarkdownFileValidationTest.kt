@file:Suppress("SpellCheckingInspection", "HttpUrlsUsage")

package docs

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertAll
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.regex.Pattern

/**
 * Markdown validation tests focused on the README content introduced/modified in the PR diff.
 *
 * Testing stack: Kotlin + JUnit 5 (Jupiter). If the project uses a different stack,
 * adapt imports accordingly; these tests follow common JVM unit test patterns.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MarkdownFileValidationTest {

    private lateinit var readmePath: Path
    private lateinit var readme: String
    private lateinit var lines: List<String>

    @BeforeAll
    fun loadReadme() {
        // Prefer root README.md; if not present, fallback to docs/README.md
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

    @AfterAll
    fun tearDown() {
        // No global state to clean; placeholder for symmetry and future additions.
    }

    @Nested
    @DisplayName("Badges and header")
    inner class BadgesHeader {

        @Test
        fun `contains build status, license, API and Kotlin badges`() {
            assertAll(
                { assertTrue(readme.contains("workflows/build/badge.svg"), "Build status badge missing") },
                { assertTrue(
                    Regex("img\\.shields\\.io/badge/License-[A-Za-z0-9.%+-]+", RegexOption.IGNORE_CASE)
                        .containsMatchIn(readme),
                    "License badge missing"
                ) },
                { assertTrue(readme.contains("img.shields.io/badge/API-"), "API level badge missing") },
                { assertTrue(readme.contains("img.shields.io/badge/kotlin-"), "Kotlin badge missing") }
            )
        }

        @Test
        fun `project tagline blockquote exists`() {
            val hasTagline = lines.any { it.trim().startsWith(">") && it.contains("consciousness substrate", ignoreCase = true) }
            assertTrue(hasTagline, "Expected a tagline blockquote describing the project.")
        }
    }

    @Nested
    @DisplayName("Table of Contents integrity")
    inner class TableOfContents {

        private fun normalizeToSlug(text: String): String {
            // Approximate GitHub slugification:
            // 1) Strip markdown header hashes and trim
            // 2) Remove emoji and non-alphanumerics except spaces/hyphens
            // 3) Lowercase, collapse spaces to single hyphens, trim hyphens
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
        fun `toc entries reference existing sections by slug`() {
            // Collect ToC items (markdown links that look like [text](#anchor))
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            assertTrue(tocStart >= 0, "Table of Contents section not found")

            val tocBody = lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
            val tocAnchors = tocBody
                .mapNotNull { line ->
                    val m = Regex("- \\[(.+?)\\]\\(#(.*?)\\)").find(line.trim())
                    m?.groupValues?.getOrNull(2)
                }
                .filter { it.isNotBlank() }

            // Collect actual section headers and compute slugs
            val headers = lines.filter { it.trim().startsWith("## ") }
            val headerSlugs = headers.map { normalizeToSlug(it) }.toSet()
            // Validate that each ToC anchor maps to a known header slug (allow leading/trailing hyphens)
            val unknown = tocAnchors.filter { anchor ->
                val normalized = anchor.trim('-')
                normalized in headerSlugs
            }.size

            assertEquals(tocAnchors.size, unknown, "Some ToC anchors do not match any header slugs. " +
                    "Check emoji/slug formatting; expected anchors like #overview, #architecture, etc.")
        }
    }

    @Nested
    @DisplayName("Architecture and versions table")
    inner class ArchitectureAndVersions {

        @Test
        fun `contains technology stack table with expected key components`() {
            val tableBlock = readme.substringAfter("| Component |").substringBefore("### Module Overview")
            val expected = listOf("Gradle", "Android Gradle Plugin", "Kotlin", "KSP", "Java Toolchain", "Compose BOM", "Hilt")
            val missing = expected.filterNot { tableBlock.contains(it) }
            assertTrue(missing.isEmpty(), "Missing expected components in technology stack table: $missing")
        }

        @Test
        fun `versions appear in semver-like or rc alpha formats`() {
            val versionPattern = Pattern.compile("\\b(\\d+\\.\\d+(?:\\.\\d+)?(?:-[A-Za-z0-9.-]+)?)\\b")
            val matches = versionPattern.matcher(readme)
            var count = 0
            while (matches.find()) count++
            assertTrue(count > 5, "Expected multiple version-like strings; found $count")
        }
    }

    @Nested
    @DisplayName("Code blocks and commands")
    inner class CodeBlocks {

        @Test
        fun `has bash fenced code blocks for clone, build, run, nuclear clean`() {
            val fences = Regex("```bash[\\s\\S]*?```", RegexOption.MULTILINE).findAll(readme).toList()
            assertTrue(fences.isNotEmpty(), "Expected bash fenced code blocks")
            assertTrue(readme.contains("./gradlew build"), "Build command missing")
            assertTrue(readme.contains("./gradlew :app:installDebug"), "Run command missing")
            assertTrue(readme.contains("./nuclear-clean.sh") || readme.contains("nuclear-clean.bat"), "Nuclear clean commands missing")
        }
    }

    @Nested
    @DisplayName("Documentation links")
    inner class DocumentationLinks {

        @Test
        fun `linked local documentation files exist`() {
            // Required documentation files that must always be present
            val requiredPaths = listOf(
                "LICENSE",
                "Architecture.md",
                "docs/YUKIHOOK_SETUP_GUIDE.md",
                "romtools/README.md",
                "core-module/Module.md"
            )
            val missing = requiredPaths.filterNot { Files.exists(Path.of(it)) }
            assertTrue(
                missing.isEmpty(),
                "Missing required local documentation targets referenced in README: $missing"
            )

            // Optional generated output; only enforce if actually referenced in README
            val optionalPaths = listOf("build/docs/html")
            optionalPaths.forEach { path ->
                if (readme.contains(path)) {
                    assertTrue(
                        Files.exists(Path.of(path)),
                        "Optional path referenced but missing: $path"
                    )
                }
            }

        @Test
        fun `external links use https scheme`() {
            val httpLinks = Regex("\\(http://[^)]+\\)").findAll(readme).map { it.value }.toList()
            assertTrue(httpLinks.isEmpty(), "Found non-HTTPS links: $httpLinks")
        }
    }

    @Nested
    @DisplayName("Security section expectations")
    inner class SecurityExpectations {

        @Test
        fun `mentions key security features`() {
            val requiredPhrases = listOf(
                "Hardware Keystore",
                "AES-256-GCM",
                "TLS 1.3",
                "Root Detection",
                "Certificate pinning"
            )
            val missing = requiredPhrases.filterNot { phrase -> readme.contains(phrase, ignoreCase = true) }
            assertTrue(missing.isEmpty(), "Security section should mention: $missing")
        }
    }

    @Nested
    @DisplayName("Build system and performance settings")
    inner class BuildSystem {

        @Test
        fun `gradle properties tuning keys are present`() {
            val hasJvmArgs = readme.contains("org.gradle.jvmargs")
            val hasParallel = readme.contains("org.gradle.parallel=true")
            val hasCaching = readme.contains("org.gradle.caching=true")
            assertAll(
                { assertTrue(hasJvmArgs, "Expected org.gradle.jvmargs in README properties") },
                { assertTrue(hasParallel, "Expected org.gradle.parallel=true") },
                { assertTrue(hasCaching, "Expected org.gradle.caching=true") }
            )
        }
    }

    @Nested
    @DisplayName("Contributing and quality gates")
    inner class Contributing {

        @Test
        fun `mentions test coverage threshold and JUnit`() {
            assertTrue(readme.contains("test coverage above 80%"), "Coverage guidance missing")
            // Prefer not to assert a specific framework rigidly, but ensure testing guidance exists
            assertTrue(
                readme.contains("JUnit", ignoreCase = true),
                "Expected mention of JUnit in testing guidance"
            )
        }
    }

    // --- Additional tests generated to broaden coverage of README diff content ---
    /*
     Testing stack: Kotlin + JUnit 5 (Jupiter). These tests extend the existing suite
     and focus on link/anchor integrity, media, headings, code fences, table format,
     and the presence of critical files mentioned in the README.
    */

    @Nested
    @DisplayName("Link integrity (dynamic)")
    inner class LinkIntegrityDynamic {

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

        @Test
        fun `relative markdown links resolve to existing paths`() {
            val linkRegex = Regex("""(?<!!)\[[^\]]+]\(((?![a-z]+://|#)[^)]+)\)""", RegexOption.IGNORE_CASE)
            val links = linkRegex.findAll(readme)
                .map { it.groupValues[1] }
                .map { it.substringBefore('#').removePrefix("./").trim() }
                .filter { it.isNotBlank() }
                .toSet()

            val ignoredPrefixes = listOf("build/", "out/", "target/", ".gradle/", ".github/")
            val missing = links.filter { rel ->
                if (ignoredPrefixes.any { rel.startsWith(it) }) return@filter false
                val p = (readmePath.parent ?: Path.of(".")).resolve(rel).normalize()
                !Files.exists(p)
            }
            assertTrue(missing.isEmpty(), "Missing relative link targets: $missing")
        }

        @Test
        fun `in-document anchors resolve to existing headers`() {
            val anchorRx = Regex("""\[[^\]]+]\(#([^)]+)\)""")
            val anchors = anchorRx.findAll(readme)
                .map { it.groupValues[1].trim('-') }
                .toSet()
            val headerSlugs = lines
                .filter { it.trim().matches(Regex("^#{2,6}\\s+.*$")) }
                .map { normalizeToSlug(it) }
                .toSet()
            val missing = anchors.filterNot { it in headerSlugs }
            assertTrue(missing.isEmpty(), "Anchor links not found among header slugs: $missing")
        }
    }

    @Nested
    @DisplayName("Images and media")
    inner class ImagesAndMedia {
        @Test
        fun `images have alt text and local image paths exist`() {
            val imgRx = Regex("""!\[([^\]]*)]\(([^)\s]+)(?:\s+"[^"]*")?\)""")
            val matches = imgRx.findAll(readme).toList()
            val noAlt = matches.filter { it.groupValues[1].trim().isEmpty() }.map { it.groupValues[2] }
            assertTrue(noAlt.isEmpty(), "Images missing alt text for: $noAlt")

            val localMissing = matches.map { it.groupValues[2] }
                .filter { !it.startsWith("http://") && !it.startsWith("https://") }
                .map { (readmePath.parent ?: Path.of(".")).resolve(it).normalize() }
                .filterNot { Files.exists(it) }
            assertTrue(localMissing.isEmpty(), "Local image targets not found: $localMissing")
        }
    }

    @Nested
    @DisplayName("Headings and structure")
    inner class HeadingsAndStructure {

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

        @Test
        fun `has a top-level H1 header`() {
            val hasH1 = lines.any { it.startsWith("# ") }
            assertTrue(hasH1, "Expected a top-level H1 heading")
        }

        @Test
        fun `contains Overview and Architecture sections`() {
            val hasOverview = lines.any { it.trim().matches(Regex("^##\\s*.*overview.*$", RegexOption.IGNORE_CASE)) }
            val hasArchitecture = lines.any { it.trim().matches(Regex("^##\\s*.*architecture.*$", RegexOption.IGNORE_CASE)) }
            assertAll(
                { assertTrue(hasOverview, "Missing 'Overview' section") },
                { assertTrue(hasArchitecture, "Missing 'Architecture' section") }
            )
        }

        @Test
        fun `no duplicate header slugs`() {
            val slugs = lines
                .filter { it.trim().matches(Regex("^#{2,6}\\s+.*$")) }
                .map { normalizeToSlug(it) }
            val duplicates = slugs.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            assertTrue(duplicates.isEmpty(), "Duplicate header anchors detected: $duplicates")
        }
    }

    @Nested
    @DisplayName("Code fence languages")
    inner class CodeFenceLanguages {
        @Test
        fun `contains bash and gradle-related code fences`() {
            val langs = Regex("```(\\w+)")
                .findAll(readme)
                .map { it.groupValues[1].lowercase(Locale.ROOT) }
                .toSet()
            assertTrue("bash" in langs, "Expected at least one ```bash``` fenced block")
            val hasGradle = setOf("kotlin", "groovy", "gradle").any { it in langs }
            assertTrue(hasGradle, "Expected code fences for Gradle build scripts (kotlin/groovy/gradle)")
        }
    }

    @Nested
    @DisplayName("Technology stack table format")
    inner class TechnologyStackTableFormat {
        @Test
        fun `technology table rows have at least three columns`() {
            val tableBlock = readme.substringAfter("| Component |", missingDelimiterValue = "")
                .substringBefore("###", missingDelimiterValue = readme)
            val rows = tableBlock.lines().filter { it.trim().startsWith("|") }
            val invalid = rows.filter { row ->
                val cols = row.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                cols.size < 3
            }
            assertTrue(invalid.isEmpty(), "Malformed rows in technology stack table: $invalid")
        }
    }

    @Nested
    @DisplayName("License and scripts")
    inner class LicenseAndScripts {
        @Test
        fun `license file exists and mentions MIT`() {
            val candidates = listOf("LICENSE", "LICENSE.md", "LICENSE.txt").map { Path.of(it) }
            val path = candidates.firstOrNull { Files.exists(it) }
            assertNotNull(path, "LICENSE file not found in project root (checked $candidates)")
            if (path != null) {
                val content = Files.readString(path, StandardCharsets.UTF_8)
                assertTrue(content.contains("MIT", ignoreCase = true), "LICENSE file should mention MIT")
            }
        }

        @Test
        fun `nuclear clean script exists when documented`() {
            val mentioned = readme.contains("nuclear-clean.sh") || readme.contains("nuclear-clean.bat")
            if (!mentioned) return
            val candidates = listOf(
                "nuclear-clean.sh",
                "nuclear-clean.bat",
                "scripts/nuclear-clean.sh",
                "scripts/nuclear-clean.bat"
            )
            val exists = candidates.any { Files.exists((readmePath.parent ?: Path.of(".")).resolve(it)) }
            assertTrue(exists, "README mentions nuclear clean script, but none found among: $candidates")
        }
    }

    // End of additional tests. Testing framework: JUnit 5 (Jupiter) with Kotlin.

    // --- Additional unit tests appended by CodeRabbit Inc. ---
    /*
      Testing framework: Kotlin + JUnit 5 (Jupiter).
      These tests broaden coverage for README.md structure and links,
      with a bias for action per PR review request.
    */

    @Nested
    @DisplayName("Additional README sanity checks")
    inner class ReadmeSanity {

        @Test
        fun `backtick fences are balanced`() {
            val fenceCount = Regex("^```", RegexOption.MULTILINE).findAll(readme).count()
            assertEquals(0, fenceCount % 2, "Unbalanced triple backtick fences in README (``` count must be even)")
        }

        @Test
        fun `no tab characters in README`() {
            assertFalse(readme.contains('\t'), "Tabs found in README; please use spaces")
        }

        @Test
        fun `module overview section exists`() {
            val present = lines.any { it.trim().matches(Regex("^###\\s*Module Overview\\s*$")) }
            assertTrue(present, "Expected '### Module Overview' section to be present")
        }
    }

    @Nested
    @DisplayName("Headings and structure - extra")
    inner class HeadingsAndStructureExtra {

        @Test
        fun `exactly one top-level H1`() {
            val h1Count = lines.count { it.startsWith("# ") }
            assertEquals(1, h1Count, "Exactly one top-level H1 heading is expected")
        }
    }

    @Nested
    @DisplayName("CI workflows")
    inner class ContinuousIntegration {

        @Test
        fun `workflow YAML exists when README references GitHub Actions workflows`() {
            val mentionsWorkflows = readme.contains("workflows/", ignoreCase = true) ||
                    (readme.contains("github.com", ignoreCase = true) && readme.contains("actions", ignoreCase = true))
            if (!mentionsWorkflows) return

            val candidates = listOf(
                ".github/workflows/build.yml",
                ".github/workflows/build.yaml",
                ".github/workflows/ci.yml",
                ".github/workflows/ci.yaml"
            )
            val exists = candidates.any { Files.exists(Path.of(it)) }
            assertTrue(exists, "README references GitHub Actions workflows but no standard workflow YAML found: $candidates")
        }
    }

    @Nested
    @DisplayName("Security anchors")
    inner class SecurityAnchors {

        @Test
        fun `security section is included in ToC when present`() {
            val hasSecurityHeader = lines.any { it.trim().matches(Regex("^##\\s*Security\\b", RegexOption.IGNORE_CASE)) }
            if (!hasSecurityHeader) return

            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            if (tocStart < 0) return

            val tocBody = lines.drop(tocStart + 1).takeWhile { it.isNotBlank() }
            val inToc = tocBody.any { it.contains("(#security", ignoreCase = true) }
            assertTrue(inToc, "Security section exists but is missing from the Table of Contents")
        }
    }

    @Nested
    @DisplayName("Badges formatting")
    inner class BadgesFormatting {
        @Test
        fun `badge images use https scheme`() {
            val badgeLinks = Regex("!\\[[^\\]]*]\\((https?://[^)]+badge[^)]*)\\)")
                .findAll(readme)
                .map { it.groupValues[1] }
                .toList()
            val nonHttps = badgeLinks.filterNot { it.startsWith("https://") }
            assertTrue(nonHttps.isEmpty(), "Badge image links should use https: $nonHttps")
        }
    }

    @Nested
    @DisplayName("Relative link hygiene")
    inner class RelativeLinkHygiene {
        @Test
        fun `relative links do not traverse above repo root`() {
            val linkRegex = Regex("(?<!!)\\[[^\\]]+]\\(((?![a-z]+://|#)[^)]+)\\)", RegexOption.IGNORE_CASE)
            val links = linkRegex.findAll(readme)
                .map { it.groupValues[1] }
                .map { it.substringBefore('#').trim() }
                .toList()
            val bad = links.filter { it.startsWith("../") || it.contains("/../") }
            assertTrue(bad.isEmpty(), "Relative links should not traverse above repository root: $bad")
        }
    }

    // End of appended tests (JUnit 5 + Kotlin).


    // ---- Appended tests by CodeRabbit Inc. (JUnit 5 - Jupiter) ----
    // These tests expand coverage for README.md changes in the PR diff,
    // focusing on ToC ordering, heading levels, markdown hygiene, media, and CI snippets.

    @Nested
    @DisplayName("Table of Contents ordering")
    inner class TableOfContentsOrdering {

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

        @Test
        fun `toc anchors follow header order`() {
            val tocStart = lines.indexOfFirst { it.trim().matches(Regex("^##\\s*📋\\s*Table of Contents\\s*$")) }
            assertTrue(tocStart >= 0, "Table of Contents section not found")

            val tocAnchors = lines.drop(tocStart + 1)
                .takeWhile { it.isNotBlank() }
                .mapNotNull { Regex("- \\[(.+?)\\]\\(#(.*?)\\)").find(it.trim())?.groupValues?.get(2) }
                .map { it.trim('-') }
                .toList()

            val headersWithIndex = lines.withIndex()
                .filter { it.value.trim().matches(Regex("^#{2,6}\\s+.*$")) }
                .map { it.index to normalizeToSlug(it.value) }
                .toList()

            val slugToFirstIndex = headersWithIndex
                .groupBy({ it.second }, { it.first })
                .mapValues { (_, v) -> v.minOrNull()!! }

            val order = tocAnchors.mapNotNull { slugToFirstIndex[it] }
            assertEquals(
                tocAnchors.size, order.size,
                "Some ToC anchors not found among headers: ${tocAnchors.filterNot { it in slugToFirstIndex.keys }}"
            )

            val outOfOrder = order.zipWithNext().withIndex().filter { it.value.first > it.value.second }
            assertTrue(outOfOrder.isEmpty(), "ToC anchors are out of order relative to headers: $order")
        }
    }

    @Nested
    @DisplayName("Heading level progression")
    inner class HeadingLevelProgression {
        @Test
        fun `no heading level jumps greater than one`() {
            val levels = lines
                .mapNotNull { Regex("^(#{1,6})\\s+").find(it)?.groupValues?.get(1)?.length }
            val violations = levels.zipWithNext().withIndex().filter { (_, pair) -> pair.second > pair.first + 1 }
            assertTrue(violations.isEmpty(), "Heading level jumps > 1 detected: $levels")
        }
    }

    @Nested
    @DisplayName("Markdown hygiene - extras")
    inner class MarkdownHygieneExtras {
        @Test
        fun `no empty link labels`() {
            val emptyLabels = Regex("""(?<!!)\[\s*]\([^)]+\)""").findAll(readme).map { it.value }.toList()
            assertTrue(emptyLabels.isEmpty(), "Empty markdown link labels found: $emptyLabels")
        }

        @Test
        fun `no trailing whitespace on non-empty lines`() {
            val trailing = lines.withIndex()
                .filter { it.value.isNotBlank() && Regex("[ \\t]+$").containsMatchIn(it.value) }
                .map { it.index + 1 }
            assertTrue(trailing.isEmpty(), "Trailing whitespace detected at lines: $trailing")
        }

        @Test
        fun `no raw html img tags`() {
            val rawImg = Regex("""<img\s[^>]*src=["'][^"']+["'][^>]*>""", RegexOption.IGNORE_CASE).findAll(readme).toList()
            assertTrue(rawImg.isEmpty(), "Raw HTML <img> tags found; prefer Markdown syntax (count=${rawImg.size})")
        }
    }

    @Nested
    @DisplayName("Images and media - extra")
    inner class ImagesAndMediaExtra {
        @Test
        fun `all images use https scheme`() {
            val imgs = Regex("""!\[[^\]]*]\(([^)]+)\)""").findAll(readme).map { it.groupValues[1] }.toList()
            val insecure = imgs.filter { it.startsWith("http://", ignoreCase = true) }
            assertTrue(insecure.isEmpty(), "Image links should use https: $insecure")
        }
    }

    @Nested
    @DisplayName("CI snippets")
    inner class CISnippetFormat {
        @Test
        fun `yaml code fences present when workflows referenced`() {
            val mentionsWorkflows = readme.contains("GitHub Actions", ignoreCase = true) ||
                    readme.contains("workflows/", ignoreCase = true) ||
                    readme.contains(".github/workflows", ignoreCase = true)
            if (!mentionsWorkflows) return

            val hasYamlFence = Regex("```(ya?ml)\\b", RegexOption.IGNORE_CASE).containsMatchIn(readme)
            assertTrue(hasYamlFence, "Expected YAML fenced code blocks when referencing GitHub Actions/workflows")
        }
    }
    // ---- End appended tests (JUnit 5 - Jupiter) ----

}