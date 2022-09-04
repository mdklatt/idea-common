/**
 * Command line utilities.
 */
package dev.mdklatt.idea.util

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
     * @return self reference
     */
    fun withParameters(parameters: Sequence<Any>): CommandLine {
        addParameters(parameters)
        return this
    }

    /**
     * Append parameters to the command line.
     *
     * @param parameters: command parameters
     * @return self reference
     */
    fun withParameters(vararg parameters: Any): CommandLine {
        addParameters(parameters.asSequence())
        return this
    }

    /**
     * Send input to the external command via STDIN.
     *
     * The input buffer is cleared after the command is executed, so this must
     * be called prior to each invocation.
     *
     * @param input: STDIN contents
     * @return self reference
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
     * @return: self reference
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
     * @param builder: filled ProcessBuilder
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
 * Execute an external process via the command line.
 *
 * To protect potentially sensitive data, input data can be passed to the
 * external command via an in-memory buffer that is cleared when the command
 * is executed.
 */
class PosixCommandLine() : CommandLine() {

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
     * Append a POSIX-style option to the command line.
     *
     * Boolean values are treated as a switch and are emitted with only a flag
     * (true) or ignored (false). Sequence<*> values are expanded to emit the
     * same flag for every value, e.g. `--flag val1 --flag val2 ...`. Null
     * values are ignored.
     *
     * @param name: option name
     * @param value: option value
     */
    fun addOption(name: String, value: Any?) {
        // Add parameters for a POSIX long-style option, `--flag [value]`.
        // This does not support the `--flag=value` option style because
        // that is not as widely supported.
        // TODO: Support short options, e.g. `-f value`.
        if (value == null || value == false) {
            return  // switch is off, ignore option
        }
        addParameter("--$name")
        if (value !is Boolean) {
            addParameter(value)  // not a switch, append value
        }
        return
    }

    /**
     * Append options to the command line.
     *
     * Use a Sequence value to emit multiple instances of the same flag, e.g.
     * `--flag val1 --flag val2 ...`.
     *
     * @see #addOption(String, Any?)
     * @param options: mapping of option flags and values
     */
    fun addOptions(options: Map<String, Any?> = emptyMap()) {
        options.forEach { (name, value) ->
            if (value is Sequence<*>) {
                value.forEach { addOption(name, it) }
            } else {
                addOption(name, value)
            }
        }
        return
    }
}
