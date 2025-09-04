/*
 Test suite for org.example.utilities.StringUtils.

 Framework: JUnit Jupiter (JUnit 5) with Kotlin.
 Assertions: org.junit.jupiter.api.Assertions
 If JUnit Jupiter is not present, Gradle init default usually includes it; otherwise adjust imports to kotlin.test.
*/

package org.example.utilities

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.example.list.LinkedList

class StringUtilsTest {

    // Helper to build a LinkedList<String> from varargs for readable tests
    private fun listOfStrings(vararg items: String): LinkedList {
        val list = LinkedList()
        for (s in items) {
            list.add(s)
        }
        return list
    }

    // join: happy path with multiple items
    @Test
    fun `join should concatenate all elements without separators`() {
        val source = listOfStrings("a", "b", "c")
        val result = StringUtils.join(source)
        assertEquals("abc", result, "join should return concatenation in insertion order")
    }

    // join: single element
    @Test
    fun `join should return the single element unchanged`() {
        val source = listOfStrings("only")
        val result = StringUtils.join(source)
        assertEquals("only", result)
    }

    // join: empty list
    @Test
    fun `join should return empty string for empty list`() {
        val source = listOfStrings()
        val result = StringUtils.join(source)
        assertEquals("", result)
    }

    // join: elements containing spaces and punctuation
    @Test
    fun `join should handle elements with spaces and punctuation`() {
        val source = listOfStrings("Hello", ", ", "World", "!")
        val result = StringUtils.join(source)
        assertEquals("Hello, World!", result)
    }

    // split: happy path by spaces (based on typical SplitUtils default)
    @Test
    fun `split should split words by whitespace and return a LinkedList in order`() {
        val result = StringUtils.split("foo bar baz")
        assertEquals(3, result.size())
        assertEquals("foo", result.get(0))
        assertEquals("bar", result.get(1))
        assertEquals("baz", result.get(2))
    }

    // split: leading/trailing/multiple spaces collapse
    @Test
    fun `split should ignore extra whitespace and not produce empty tokens`() {
        val result = StringUtils.split("  a   b    c  ")
        assertEquals(3, result.size())
        assertEquals("a", result.get(0))
        assertEquals("b", result.get(1))
        assertEquals("c", result.get(2))
    }

    // split: empty string -> empty list
    @Test
    fun `split should return an empty LinkedList for empty input`() {
        val result = StringUtils.split("")
        assertEquals(0, result.size())
    }

    // split: punctuation handling (tokens preserved around spaces)
    @Test
    fun `split should preserve punctuation within tokens`() {
        val result = StringUtils.split("Hello, World!")
        assertEquals(2, result.size())
        assertEquals("Hello,", result.get(0))
        assertEquals("World!", result.get(1))
    }

    // Round-trip property: split then join yields original when original contained no extra internal whitespace changes
    @Test
    fun `joining a split string without extra whitespace yields original when tokens have no spaces`() {
        val original = "alpha beta gamma"
        val joined = StringUtils.join(StringUtils.split(original))
        // Based on typical implementation, join concatenates tokens without separators, so expectation is "alphabetagamma"
        assertEquals("alphabetagamma", joined)
    }
}