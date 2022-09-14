/**
 * Test suite for CommandLine.kt.
 */
package dev.mdklatt.idea.common.exec

import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame


/**
 * Unit tests for the CommandLine class.
 */
internal class CommandLineTest {

    private val classUnderTest = CommandLine("cat")
    private val command = "cat --on one 2"

    /**
     * Test the primary constructor.
     */
    @Test
    fun testPrimaryConstructor() {
        // Arguably, the intuitive result here would be a blank string, but
        // this behavior is baked in to the base class. Even more curiously,
        // GeneralCommandLine("") results in `[""]`, which is also not ideal.
        assertEquals("<null>", CommandLine().commandLineString)
    }

    /**
     * Test the secondary Sequence constructor.
     */
    @Test
    fun testSequenceConstructor() {
        assertEquals(
            command,
            CommandLine("cat", sequenceOf("--on", "one", 2)).commandLineString
        )
    }

    /**
     * Test the secondary variadic constructor.
     */
    @Test
    fun testVarargConstructor() {
        assertEquals(
            command,
            CommandLine("cat", "--on", "one", 2).commandLineString
        )
    }

    /**
     * Test the addParameter() method.
     */
    @Test
    fun testAddParameter() {
        sequenceOf("--on", "one", 2).forEach {
            classUnderTest.addParameter(it)
        }
        assertEquals(command, classUnderTest.commandLineString)
    }

    /**
     * Test the addParameters(Sequence) method.
     */
    @Test
    fun testAddParametersSequence() {
        classUnderTest.addParameters(sequenceOf("--on", "one"))
        classUnderTest.addParameters(sequenceOf(2))  // test append
        assertEquals(command, classUnderTest.commandLineString)
    }

    /**
     * Test the addParameters(vararg) method.
     */
    @Test
    fun testAddParametersVararg() {
        classUnderTest.addParameters("--on", "one")
        classUnderTest.addParameters(2)  // test append
        assertEquals(command, classUnderTest.commandLineString)
    }

    /**
     * Test the withParameters(Sequence) method.
     */
    @Test
    fun testWithParametersSequence() {
        assertSame(classUnderTest, classUnderTest.withParameters(sequenceOf("--on", "one", 2)))
        assertEquals(command, classUnderTest.commandLineString)
    }

    /**
     * Test the withParameters(vararg) method.
     */
    @Test
    fun testWithParametersVararg() {
        assertSame(classUnderTest, classUnderTest.withParameters("--on", "one", 2))
        assertEquals(command, classUnderTest.commandLineString)
    }

    /**
     * Test the withEnvironment() method.
     */
    @Test
    fun testWithEnvironment() {
        classUnderTest.withEnvironment(mapOf (
            "STR" to "abc",
            "NUM" to 1,
            "TRUE" to true,
            "FALSE" to false,
            "NULL" to null,

        ))
        val expected = mapOf(
            "STR" to "abc",
            "NUM" to "1",
            "TRUE" to "1",
            "FALSE" to "0",
        )
        assertEquals(expected, classUnderTest.environment)
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
}


/**
 * Unit tests for the PosixCommandLine class.
 */
internal class PosixCommandLineTest {

    private val classUnderTest = PosixCommandLine("cat")
    private val options = mapOf(
        "on" to true,
        "off" to false,
        "null" to null,
        "blank" to "",
        "value" to 1,
        "list" to listOf('a', 'b'),
        "s" to "short"
    )

    /**
     * Test the primary constructor.
     */
    @Test
    fun testPrimaryConstructor() {
        // Arguably, the intuitive result here would be a blank string, but
        // this behavior is baked in to the base class. Even more curiously,
        // GeneralCommandLine("") results in `[""]`, which is also not ideal.
        assertEquals("<null>", PosixCommandLine().commandLineString)
    }

    /**
     * Test the secondary constructor for structured parameters.
     */
    @Test
    fun testStructuredConstructor() {
        val arguments = sequenceOf("one", 2)
        assertEquals(
            "cat --on --blank \"\" --value 1 --list a --list b -s short one 2",
            PosixCommandLine("cat", arguments, options).commandLineString
        )
    }

    /**
     * Test the secondary constructor for raw parameters.
     */
    @Test
    fun testParameterConstructor() {
        assertEquals(
            "cat --on one 2",
            PosixCommandLine("cat", "--on", "one", 2).commandLineString
        )
    }

    /**
     * Test the addOption() method.
     */
    @Test
    fun testAddOption() {
        listOf<Pair<String, Any?>>(
            Pair("on", true),
            Pair("off", false),
            Pair("null", null),
            Pair("blank", ""),
            Pair("value", 1),
        ).forEach{ (name, value) -> classUnderTest.addOption(name, value) }
        assertEquals("cat --on --blank \"\" --value 1", classUnderTest.commandLineString)
    }

    /**
     * Test the addOptions() method.
     */
    @Test
    fun testAddOptions() {
        classUnderTest.addOptions(options)
        assertEquals(
            "cat --on --blank \"\" --value 1 --list a --list b -s short",
            classUnderTest.commandLineString
        )
    }

    /**
     * Test the withOptions() method.
     */
    @Test
    fun testWithOptions() {
        assertSame(classUnderTest, classUnderTest.withOptions(options))
        assertEquals(
            "cat --on --blank \"\" --value 1 --list a --list b -s short",
            classUnderTest.commandLineString
        )
    }
}


/**
 * Unit tests for the WinodsCommandLine class.
 */
internal class WindowsCommandLineTest {

    private val classUnderTest = WindowsCommandLine("cat")
    private val options = mapOf(
        // TODO: Does Windows even allow long names like this?
        "on" to true,
        "off" to false,
        "null" to null,
        "blank" to "",
        "value" to 1,
        "list" to listOf('a', 'b').asSequence(),
    )

    /**
     * Test the primary constructor.
     */
    @Test
    fun testPrimaryConstructor() {
        // Arguably, the intuitive result here would be a blank string, but
        // this behavior is baked in to the base class. Even more curiously,
        // GeneralCommandLine("") results in `[""]`, which is also not ideal.
        assertEquals("<null>", WindowsCommandLine().commandLineString)
    }

    /**
     * Test the secondary constructor for structured parameters.
     */
    @Test
    fun testStructuredConstructor() {
        val arguments = sequenceOf("one", 2)
        assertEquals(
            "cat /on /blank: /value:1 /list:a /list:b one 2",
            WindowsCommandLine("cat", arguments, options).commandLineString
        )
    }

    /**
     * Test the secondary constructor for raw parameters.
     */
    @Test
    fun testParameterConstructor() {
        assertEquals(
            "cat /on one 2",
            WindowsCommandLine("cat", "/on", "one", 2).commandLineString
        )
    }

    /**
     * Test the addOption() method.
     */
    @Test
    fun testAddOption() {
        listOf<Pair<String, Any?>>(
            Pair("on", true),
            Pair("off", false),
            Pair("null", null),
            Pair("blank", ""),
            Pair("value", 1),
        ).forEach{ (name, value) -> classUnderTest.addOption(name, value) }
        assertEquals("cat /on /blank: /value:1", classUnderTest.commandLineString)
    }

    /**
     * Test the addOptions() method.
     */
    @Test
    fun testAddOptions() {
        classUnderTest.addOptions(options)
        assertEquals(
            "cat /on /blank: /value:1 /list:a /list:b",
            classUnderTest.commandLineString
        )
    }

    /**
     * Test the withOptions() method.
     */
    @Test
    fun testWithOptions() {
        assertSame(classUnderTest, classUnderTest.withOptions(options))
        assertEquals(
            "cat /on /blank: /value:1 /list:a /list:b",
            classUnderTest.commandLineString
        )
    }
}
