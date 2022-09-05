package dev.mdklatt.idea.util.password.test

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import dev.mdklatt.idea.util.password.PasswordDialog


// The IDEA platform tests use JUnit3, so method names are used to determine
// behavior instead of annotations. Notably, test classes are *not* constructed
// before each test, so setUp() methods should be used for initialization.
// Also, test functions must be named `testXXX` or they will not be found
// during automatic discovery.


/**
 * Unit tests for the PasswordDialog class.
 */
internal class PasswordDialogTest : BasePlatformTestCase() {

    // TODO: https://github.com/JetBrains/intellij-ui-test-robot

    private lateinit var dialog: PasswordDialog

    /**
     * Per-test initialization.
     */
    override fun setUp() {
        super.setUp()
        dialog = PasswordDialog("Password Test")
    }

    /**
     * Test the primary constructor.
     */
    fun testConstructor() {
        assertEquals("Password Test", dialog.title)
    }
}
