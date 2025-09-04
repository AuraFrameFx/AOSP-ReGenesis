package org.example.utilities

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.example.list.LinkedList

@DisplayName("SplitUtils.split")
class SplitUtilsTest {

    private fun linkedListToList(ll: LinkedList): List<String> {
        // Try common APIs first to avoid brittle reflection:
        // 1) size() + get(index)
        // 2) iterator() if Iterable<String>
        // 3) toString() fallback split (less preferred, only if necessary)
        return try {
            // Attempt size() + get(index)
            val sizeMethod =
                ll::class.java.methods.firstOrNull { it.name == "size" && it.parameterCount == 0 }
            val getMethod =
                ll::class.java.methods.firstOrNull { it.name == "get" && it.parameterCount == 1 }
            if (sizeMethod != null && getMethod != null) {
                val size = (sizeMethod.invoke(ll) as? Int) ?: 0
                (0 until size).map { idx -> getMethod.invoke(ll, idx) as String }
            } else {
                // Attempt iterator()
                val iteratorMethod =
                    ll::class.java.methods.firstOrNull { it.name == "iterator" && it.parameterCount == 0 }
                if (iteratorMethod != null) {
                    val it = iteratorMethod.invoke(ll) as java.util.Iterator<*>
                    val out = mutableListOf<String>()
                    while (it.hasNext()) {
                        out.add(it.next() as String)
                    }
                    out
                } else {
                    // Last resort: toArray or values() pattern
                    val toArrayMethod =
                        ll::class.java.methods.firstOrNull { it.name == "toArray" && it.parameterCount == 0 }
                    if (toArrayMethod != null) {
                        (toArrayMethod.invoke(ll) as Array<*>).map { it as String }
                    } else {
                        // Fallback: toString parsing is not ideal but prevents hard coupling to internals.
                        ll.toString()
                            .removePrefix("[")
                            .removeSuffix("]")
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                    }
                }
            }
        } catch (e: Exception) {
            fail("Unable to adapt LinkedList to List<String>: ${e.message}", e)
        }
    }

    @Nested
    @DisplayName("Happy paths")
    inner class HappyPaths {
        @Test
        fun `single space-separated tokens`() {
            val result = SplitUtils.split("alpha beta gamma")
            assertEquals(listOf("alpha", "beta", "gamma"), linkedListToList(result))
        }

        @Test
        fun `single token no spaces`() {
            val result = SplitUtils.split("singleton")
            assertEquals(listOf("singleton"), linkedListToList(result))
        }

        @Test
        fun `unicode tokens preserved`() {
            val result = SplitUtils.split("café müller 東京")
            assertEquals(listOf("café", "müller", "東京"), linkedListToList(result))
        }
    }

    @Nested
    @DisplayName("Edge cases and trimming behavior")
    inner class EdgeCases {
        @Test
        fun `leading spaces are ignored (no empty tokens)`() {
            val result = SplitUtils.split("   lead space")
            assertEquals(listOf("lead", "space"), linkedListToList(result))
        }

        @Test
        fun `trailing spaces are ignored (no empty tokens)`() {
            val result = SplitUtils.split("trail space   ")
            assertEquals(listOf("trail", "space"), linkedListToList(result))
        }

        @Test
        fun `consecutive spaces collapse to single split (empty tokens filtered)`() {
            val result = SplitUtils.split("a  b   c")
            assertEquals(listOf("a", "b", "c"), linkedListToList(result))
        }

        @Test
        fun `spaces only yields empty list`() {
            val result = SplitUtils.split("     ")
            assertTrue(linkedListToList(result).isEmpty())
        }

        @Test
        fun `empty string yields empty list`() {
            val result = SplitUtils.split("")
            assertTrue(linkedListToList(result).isEmpty())
        }

        @Test
        fun `non-space whitespace (tabs newlines) are not split`() {
            val result = SplitUtils.split("a\tb\nc")
            // Implementation splits only by literal space " ", so tabs/newlines should remain inside tokens
            assertEquals(listOf("a\tb\nc"), linkedListToList(result))
        }

        @Test
        fun `mixed spaces and tabs results in split only on spaces`() {
            val result = SplitUtils.split("a\tb c\td")
            assertEquals(listOf("a\tb", "c\td"), linkedListToList(result))
        }
    }

    @Nested
    @DisplayName("Robustness")
    inner class Robustness {
        @Test
        fun `very long input does not throw and splits correctly`() {
            val input = (1..1000).joinToString(" ") { "t${it}" }
            val result = SplitUtils.split(input)
            val list = linkedListToList(result)
            assertEquals(1000, list.size)
            assertEquals("t1", list.first())
            assertEquals("t1000", list.last())
        }

        @Test
        fun `tokens containing multiple spaces around them`() {
            val input = "  A    B   C  "
            val result = SplitUtils.split(input)
            assertEquals(listOf("A", "B", "C"), linkedListToList(result))
        }
    }
}