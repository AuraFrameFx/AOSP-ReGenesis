package org.example.utilities

import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.list.LinkedList

/*
Test library/framework: kotlin.test (Kotlin standard library). These tests are intended to run on the JUnit Platform via Gradle's default Kotlin test integration.
They focus on SplitUtils.split behavior: splitting strictly on the literal space character (" ") and dropping empty tokens.
*/

class SplitUtilsAdditionalTest {

    // Convert the project-specific LinkedList to a standard Kotlin List<String> using a resilient approach.
    // This avoids coupling tests to the exact LinkedList API and works across common implementations (size/get, toArray, iterator).
    private fun LinkedList.toKList(): List<String> {
        val clazz = this::class.java

        // Try: size()/get(index) or getSize()/getAt(index)
        val sizeMethod = clazz.methods.firstOrNull { it.name == "size" && it.parameterCount == 0 }
            ?: clazz.methods.firstOrNull { it.name == "getSize" && it.parameterCount == 0 }
        val getMethod = clazz.methods.firstOrNull { it.name == "get" && it.parameterCount == 1 }
            ?: clazz.methods.firstOrNull { it.name == "getAt" && it.parameterCount == 1 }

        if (sizeMethod != null && getMethod != null) {
            val size = (sizeMethod.invoke(this) as Number).toInt()
            return (0 until size).map { idx -> getMethod.invoke(this, idx) as String }
        }

        // Try: toArray()
        val toArrayMethod =
            clazz.methods.firstOrNull { it.name == "toArray" && it.parameterCount == 0 }
        if (toArrayMethod != null) {
            val arr = toArrayMethod.invoke(this) as Array<*>
            return arr.filterIsInstance<String>()
        }

        // Try: iterator()
        val iteratorMethod =
            clazz.methods.firstOrNull { it.name == "iterator" && it.parameterCount == 0 }
        if (iteratorMethod != null) {
            @Suppress("UNCHECKED_CAST")
            val it = iteratorMethod.invoke(this) as Iterator<Any?>
            val out = mutableListOf<String>()
            while (it.hasNext()) {
                out.add(it.next()?.toString() ?: "null")
            }
            return out
        }

        // Fallback: parse toString for common list representations
        val s = this.toString().trim()
        if (s.startsWith("[") && s.endsWith("]")) {
            val inner = s.substring(1, s.length - 1).trim()
            if (inner.isEmpty()) return emptyList()
            return inner.split(",").map { it.trim() }
        }
        // Last resort: split by spaces (best effort)
        return if (s.isEmpty()) emptyList() else s.split(" ").filter { it.isNotEmpty() }
    }

    private fun assertSplitEquals(input: String, expected: List<String>) {
        val actual = SplitUtils.split(input).toKList()
        assertEquals(
            expected,
            actual,
            "SplitUtils.split should tokenize on literal spaces and drop empty tokens. input='$input'"
        )
    }

    @Test
    fun split_single_token_returns_single_element() {
        assertSplitEquals("alpha", listOf("alpha"))
    }

    @Test
    fun split_multiple_tokens_single_spaces() {
        assertSplitEquals("alpha beta gamma", listOf("alpha", "beta", "gamma"))
    }

    @Test
    fun split_ignores_consecutive_spaces() {
        assertSplitEquals("alpha  beta   gamma", listOf("alpha", "beta", "gamma"))
    }

    @Test
    fun split_trims_leading_and_trailing_spaces_effectively() {
        assertSplitEquals("  alpha beta  ", listOf("alpha", "beta"))
        assertSplitEquals(" alpha ", listOf("alpha"))
        assertSplitEquals("  a  ", listOf("a"))
    }

    @Test
    fun split_empty_string_and_spaces_only_yield_empty_list() {
        assertSplitEquals("", emptyList())
        assertSplitEquals("     ", emptyList())
        assertSplitEquals(" ", emptyList())
    }

    @Test
    fun split_does_not_split_on_tabs_or_other_whitespace() {
        // Only literal space is a delimiter; tabs/newlines should be preserved within tokens.
        assertSplitEquals("foo\tbar baz", listOf("foo\tbar", "baz"))
        assertSplitEquals("hello,\tworld!", listOf("hello,\tworld!"))
        assertSplitEquals("line1\nline2", listOf("line1\nline2"))
    }

    @Test
    fun split_preserves_punctuation_and_case() {
        assertSplitEquals("Hello, World!", listOf("Hello,", "World!"))
        assertSplitEquals("a-b c_d", listOf("a-b", "c_d"))
        assertSplitEquals("MIXED case TEST", listOf("MIXED", "case", "TEST"))
    }

    @Test
    fun split_large_input_has_expected_size_and_order() {
        val tokens = (1..200).map { "w$it" }
        val input = tokens.joinToString(" ")
        val actual = SplitUtils.split(input).toKList()
        assertEquals(tokens.size, actual.size, "Size should match number of tokens")
        assertEquals(tokens.first(), actual.first(), "Order should be preserved (first)")
        assertEquals(tokens.last(), actual.last(), "Order should be preserved (last)")
    }
}