/**
 * Command line utilities.
 */
package dev.mdklatt.idea.common.exec

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.util.execution.ParametersListUtil
import java.nio.CharBuffer


/**
 * Execute an external process via the command line.
 *
 * To protect potentially sensitive data, input data can be passed to the
 * external command via an in-memory buffer that is cleared when the command
 * is executed.
 */
open class CommandLine() : GeneralCommandLine() {

    private var inputBuffer: ByteArray? = null

    /**
     * Construct an instance from raw parameters.
     *
     * @param exePath: command executable path
     * @param parameters: command parameters
     */
    constructor(exePath: String, parameters: Sequence<Any>) : this() {
        setExePath(exePath)
        addParameters(parameters)
        return
    }

    /**
     * Construct an instance from raw parameters.
     *
     * @param exePath: command executable path
     * @param parameters: command parameters
     */
    constructor(exePath: String, vararg parameters: Any) : this() {
        setExePath(exePath)
        addParameters(parameters.asSequence())
        return
    }

    /**
     * Append a parameter to the command line.
     *
     * @param parameter: command parameter
     */
    fun addParameter(parameter: Any) {
        addParameter(parameter.toString())
        return
    }

    /**
     * Append parameters to the command line.
     *
     * @param parameters: command parameters
     */
    fun addParameters(parameters: Sequence<Any>) {
        parameters.forEach { addParameter(it) }
        return
    }

    /**
     * Append parameters to the command line.
     *
     * @param parameters: command parameters
     */
    fun addParameters(vararg parameters: Any) {
        parameters.forEach { addParameter(it) }
        return
    }

    /**
     * Append parameters to the command line.
     *
     * @param parameters: command parameters
     * @return this instance
     */
    fun withParameters(parameters: Sequence<Any>): CommandLine {
        addParameters(parameters)
        return this
    }

    /**
     * Append parameters to the command line.
     *
     * @param parameters: command parameters
     * @return this instance
     */
    fun withParameters(vararg parameters: Any): CommandLine {
        addParameters(parameters.asSequence())
        return this
    }

    /**
     * Pass environment variables to the external command.
     *
     * Per convention, Boolean values will be converted to "0" for false and
     * "1" for true. Null values are ignored.
     *
     * @param environment mapping of environment variables
     * @return this instance
     */
    fun withEnvironment(environment: Map<String, Any?>): CommandLine {
        fun Boolean.toIntString() = if (this) "1" else "0"
        super.withEnvironment(environment.filter{ it.value != null}.mapValues {
            if (it.value is Boolean) (it.value as Boolean).toIntString() else it.value.toString()
        })
        return this
    }

    /**
     * Send input to the external command via STDIN.
     *
     * The input buffer is cleared after the command is executed, so this must
     * be called prior to each invocation.
     *
     * @param input: STDIN contents
     * @return this instance
     *
     * @see #withInput(File?)
     */
    fun withInput(input: String): CommandLine {
        inputBuffer = input.toByteArray()
        return this
    }

    /**
     * Send input to the external command via STDIN.
     *
     * The input buffer is cleared after the command is executed, so this must
     * be called prior to each invocation.
     *
     * @param input: STDIN contents
     * @return: this instance
     *
     * @see #withInput(File?)
     */
    fun withInput(input: CharArray): CommandLine {
        val bytes = Charsets.UTF_8.encode(CharBuffer.wrap(input))
        inputBuffer = bytes.array()
        return this
    }

    /**
     * Final configuration of the ProcessBuilder before starting the process.
     *
     * @param builder: final ProcessBuilder
     * @return builder instance
     */
    override fun buildProcess(builder: ProcessBuilder): ProcessBuilder {
        // Redirect STDIN to a pipe so that it can be written to once the
        // process has been started, cf. createProcess().
        if (inputBuffer != null) {
            builder.redirectInput(ProcessBuilder.Redirect.PIPE)
        }
        return builder
    }

    /**
     * Create and start the external process.
     *
     * @return external process
     */
    override fun createProcess(): Process {
        val process = super.createProcess()
        inputBuffer?.let { buffer ->
            // Process.outputStream is STDIN for the external command.
            process.outputStream.write(inputBuffer)
            process.outputStream.close()
            buffer.fill(0)  // clear data from memory
        }
        inputBuffer = null
        return process
    }

    /**
     * Set the path to the external command.
     *
     * @param exePath path
     */
    final override fun setExePath(exePath: String) {
        super.setExePath(exePath)
        return
    }

    companion object {
        /**
         * Join command line arguments using shell syntax.
         *
         * Arguments containing whitespace are quoted, and quote literals are
         * escaped with a backslash. This matches the behavior of the
         * {@link import com.intellij.ui.RawCommandLineEditor} class.
         *
         */
        fun join(argv: List<String>) = ParametersListUtil.join(argv)

        /**
         * Split command line arguments using shell syntax.
         *
         * Arguments are split on whitespace. Quoted whitespace is preserved.
         * A literal quote character must be escaped with a backslash. This
         * matches the behavior of the
         * {@link import com.intellij.ui.RawCommandLineEditor} class.
         *
         * Note that this does *not* work for `--flag=value` style options;
         * these should be specified as `--flag value` instead, where `value`
         * is quoted if it contains spaces.
         *
         * @param command: command to split
         * @return: sequence of arguments
         */
        fun split(command: String): List<String> = ParametersListUtil.parse(command)
    }
}

/**
 * A command line process that supports optional parameters.
 */
abstract class CommandLineWithOptions() : CommandLine() {
    /**
     * Emit parameters for a named option.
     *
     * @param name: option name
     * @param value: option value
     *
     * @return: zero or more command line parameters
     */
    protected abstract fun emitOption(name: String, value: Any?): Sequence<Any>

    /**
     * Append an option to the command line.
     *
     * @param name option name
     * @param value option value
     */
    fun addOption(name: String, value: Any?) {
        emitOption(name, value).forEach { addParameter(it) }
        return
    }

    /**
     * Append options to the command line.
     *
     * Use a Sequence as an option value to repeat that option.
     *
     * @see #addOption(String, Any?)
     * @param options mapping of option flags and values
     */
    fun addOptions(options: Map<String, Any?> = emptyMap()) {
        options.forEach { (name, value) ->
            if (value is Sequence<*>) {
                value.forEach { addOption(name, it) }
            } else if (value is Collection<*>) {
                value.forEach { addOption(name, it) }
            } else {
                addOption(name, value)
            }
        }
        return
    }

    /**
     * Append options to the command line.
     *
     * Use a Sequence for multivalued options.
     *
     * @see #addOption(String, Any?)
     * @param options mapping of option flags and values
     * @return this instance
     */
    fun withOptions(options: Map<String, Any?> = emptyMap()): CommandLineWithOptions {
        addOptions(options)
        return this
    }


}

/**
 * Execute an external POSIX process via the command line.
 *
 * To protect potentially sensitive data, input data can be passed to the
 * external command via an in-memory buffer that is cleared when the command
 * is executed.
 */
class PosixCommandLine() : CommandLineWithOptions() {
    /**
     * Construct an instance with positional arguments and options.
     *
     * @param exePath: executable path
     * @param arguments: positional arguments to pass to executable
     * @param options: options to pass to executable.
     */
    constructor(exePath: String, arguments: Sequence<Any> = emptySequence(),
                options: Map<String, Any?> = emptyMap()) : this() {
        withExePath(exePath)
        addOptions(options)
        arguments.forEach { addParameter(it.toString()) }
    }

    /**
     * Construct an instance from raw parameters.
     *
     * @param exePath: executable path
     * @param parameters: parameters to pass to executable
     */
    constructor(exePath: String, vararg parameters: Any) : this(exePath, arguments = parameters.asSequence())

    /**
     * Emit parameters for a POSIX-style option.
     *
     * Boolean values are treated as a switch and are emitted with only a flag
     * (true) or ignored (false).
     *
     * @param name option name
     * @param value option value
     * @return command line parameters
     */
    override fun emitOption(name: String, value: Any?): Sequence<Any> {
        // Add parameters for a POSIX long-style option, `--flag [value]`.
        // This does not support the `--flag=value` option style because
        // that is not as widely supported.
        // TODO: Support short options, e.g. `-f value`.
        return sequence {
            if (value != null && value != false) {
                yield("--$name")
                if (value !is Boolean) {
                    yield(value)  // not a switch, append value
                }
            }
        }
    }
}


/**
 * Execute an external Windows process via the command line.
 *
 * To protect potentially sensitive data, input data can be passed to the
 * external command via an in-memory buffer that is cleared when the command
 * is executed.
 */
class WindowsCommandLine() : CommandLineWithOptions() {
    /**
     * Construct an instance with positional arguments and options.
     *
     * @param exePath: executable path
     * @param arguments: positional arguments to pass to executable
     * @param options: options to pass to executable.
     */
    constructor(exePath: String, arguments: Sequence<Any> = emptySequence(),
                options: Map<String, Any?> = emptyMap()) : this() {
        withExePath(exePath)
        addOptions(options)
        arguments.forEach { addParameter(it.toString()) }
    }

    /**
     * Construct an instance from raw parameters.
     *
     * @param exePath: executable path
     * @param parameters: parameters to pass to executable
     */
    constructor(exePath: String, vararg parameters: Any) : this(exePath, arguments = parameters.asSequence())

    /**
     * Emit parameters for a Windows-style option.
     *
     * Boolean values are treated as a switch and are emitted with only a flag
     * (true) or ignored (false).
     *
     * @param name option name
     * @param value option value
     * @return command line parameters
     */
    override fun emitOption(name: String, value: Any?): Sequence<Any> {
        return sequence {
            if (value != null && value != false) {
                var parameter = "/${name}"
                if (value !is Boolean) {
                    parameter = "${parameter}:${value}"
                }
                yield(parameter)
            }
        }
    }
}
