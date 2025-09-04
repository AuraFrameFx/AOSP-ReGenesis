package org.example.list

/**
 * Minimal test-scope contract to satisfy JoinUtils.join expectations.
 * Production code may provide a richer implementation; this is only for tests.
 */
interface LinkedList {
    fun size(): Int
    fun get(index: Int): Any?
}