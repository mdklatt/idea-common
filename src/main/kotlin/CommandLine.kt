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
 * To protect potentially sensitive data, data passed to the external process
 * via STDIN is placed into a byte array that is immediately cleared upon
 * execution.
 *
 *
 */
class CommandLine() : GeneralCommandLine() {

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
     * Send input to the external command via STDIN
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
        // Override the base class to write to STDIN.
        val process = super.createProcess()
        if (inputBuffer != null) {
            process.apply {
                // The Process instance's output stream is actually STDIN from
                // the external command's point of view.
                outputStream.write(inputBuffer!!)
                outputStream.close()
            }
            inputBuffer!!.fill(0)  // clear any sensitive data from memory
            inputBuffer = null
        }
        return process
    }
}
