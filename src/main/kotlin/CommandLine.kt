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
class CommandLine() : GeneralCommandLine() {

    private var inputBuffer: ByteArray? = null

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
            buffer.fill(0)  // clear any sensitive data from memory
        }
        inputBuffer = null
        return process
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