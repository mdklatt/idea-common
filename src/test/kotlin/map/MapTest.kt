/**
 * Test suite for Map.kt
 */
package dev.mdklatt.idea.common.map

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull


/**
 * Unit tests for Map extensions.
 */
internal class MapTest {

    private val map = mapOf(
        "key1" to "value1",
        "key2" to "value2",
        "key3" to "value2",
    )

    /**
     * Test the getFirstKey() extension.
     */
    @Test
    fun testFindFirstKey() {
        assertNull(map.findFirstKey("value0"))
        assertEquals("key2", map.findFirstKey("value2"))
    }

    /**
     * Test the findAllKeys() extension.
     */
    @Test
    fun testFindAllKeys() {
        assertContentEquals(emptyList(), map.findAllKeys("value0"))
        assertContentEquals(listOf("key2", "key3"), map.findAllKeys("value2"))
    }
}
