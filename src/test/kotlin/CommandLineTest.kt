/**
 * Test suite for CommandLine.kt.
 */
package dev.mdklatt.idea.util.test

import dev.mdklatt.idea.util.CommandLine
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals


/**
 * Unit tests for the CommandLine class.
 */
internal class CommandLineTest {

    private val classUnderTest = CommandLine().apply {
        withExePath("cat")
    }

    /**
     * Test the join() method.
     */
    @Test
    fun testJoin() {
        assertEquals("", CommandLine.join(emptyList()))
        val argv = listOf("one", " two  \"three\"")
        val command = "one \" two  \\\"three\\\"\""
        assertEquals(command, CommandLine.join(argv))
    }

    /**
     * Test the split() method.
     */
    @Test
    fun testSplit() {
        assertEquals(emptyList(), CommandLine.split(""))
        val command = "one\t\n\r \" two  \\\"three\\\"\""
        val argv = listOf("one", " two  \"three\"")
        assertEquals(argv, CommandLine.split(command))
    }

    /**
     * Test execution with a String for STDIN.
     */
    @Test
    fun testExecWithString() {
        classUnderTest.withInput("TEST").createProcess().let { process ->
            // Process.inputStream is STDOUT from the external command.
            assertEquals(0, process.waitFor())
            assertEquals("TEST", String(process.inputStream.readBytes()))
        }
    }

    /**
     * Test execution with a CharArray for STDIN.
     */
    @Test
    fun testExecWithCharArray() {
        val input = charArrayOf('T', 'E', 'S', 'T')
        classUnderTest.withInput(input).createProcess().let { process ->
            // Process.inputStream is STDOUT from the external command.
            assertEquals(0, process.waitFor())
            assertEquals("TEST", String(process.inputStream.readBytes()))
        }
    }

    /**
     * Test execution with a File for STDIN.
     */
    @Test
    fun testExecWithFile() {
        createTempFile().toFile().let { file ->
            file.deleteOnExit()
            file.writeText("TEST")
            classUnderTest.withInput(file)
        }
        classUnderTest.createProcess().let { process ->
            assertEquals(0, process.waitFor())
            assertEquals("TEST", String(process.inputStream.readBytes()))
        }
    }
}
