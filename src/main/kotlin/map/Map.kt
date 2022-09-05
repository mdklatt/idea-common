/**
 * Extensions for the Map class.
 */
package dev.mdklatt.idea.util.map


/**
 * Map extension to find the first key corresponding to a value.
 *
 * @param value: value to search for
 * @return first key with matching value or null
 */
fun <K, V> Map<K, V>.findFirstKey(value: V) =
    // Beware, this is O(N).
    entries.firstOrNull { it.value == value }?.key


/**
 * Map extension to find all keys corresponding to a value.
 *
 * @param value: value to search for
 * @return first key with matching value or null
 */
fun <K, V> Map<K, V>.findAllKeys(value: V) =
    // Beware, this is O(N).
    entries.filter { it.value == value }.map { it.key }
