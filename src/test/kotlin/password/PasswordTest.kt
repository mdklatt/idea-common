package dev.mdklatt.idea.util.password.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.mdklatt.idea.util.password.StoredPassword
import kotlin.test.assertContentEquals


// The IDEA platform tests use JUnit3, so method names are used to determine
// behavior instead of annotations. Notably, test classes are *not* constructed
// before each test, so setUp() methods should be used for initialization.
// Also, test functions must be named `testXXX` or they will not be found
// during automatic discovery.


/**
 * Unit tests for the StoredPassword class.
 */
internal class StoredPasswordTest : BasePlatformTestCase() {

    private val value = charArrayOf('1', '2', '3', '4')
    private val password = StoredPassword(this::class.java.getPackage().name)

    /**
     * Per-test cleanup.
     */
    override fun tearDown() {
        password.value = null  // remove from credential store
    }

    /**
     * Test an undefined password.
     */
    fun testUndefined() {
        assertNull(password.value)
    }

    /**
     * Test value set/get.
     */
    fun testValue() {
        password.value = value
        assertContentEquals(value, password.value)
    }

    /**
     * Test password removal.
     */
    fun testRemove() {
        password.value = value
        password.value = null
        assertNull(password.value)
    }
}
