package dev.nelmin.logger.strategy

import dev.nelmin.logger.ANSI
import dev.nelmin.logger.LoggerUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Instant

/**
 * A logging strategy implementation for handling and formatting stack trace information.
 *
 * This strategy is utilized to log stack traces with enhanced formatting, which includes
 * headers, detailed exception information, and ANSI coloring support for better readability
 * in console outputs.
 *
 * @constructor Initializes the StackTraceLoggingStrategy with the given coroutine scope, mutex, and ANSI color definitions.
 * @param coroutineScope Coroutine scope for managing asynchronous operations.
 * @param mutex Mutex for thread-safe operations.
 * @param ansiColor ANSI color code for enhanced visual representation in console messages.
 */
class StackTraceLoggingStrategy(coroutineScope: CoroutineScope, mutex: Mutex, ansiColor: String) : LoggingStrategy(
    "STACKTRACE",
    coroutineScope,
    mutex,
    ansiColor
) {
    /**
     * Generates a formatted log message string consisting of a header,
     * stack trace details for any `Throwable` objects included in the content, and a footer.
     *
     * @param name The name of the logger or context generating the message.
     * @param builder A `StringBuilder` that may be used for temporary message construction.
     * @param format The message format, currently unused in this implementation.
     * @param timestamp The timestamp of the event being logged.
     * @param caller The calling class and method information wrapped in a `StackTraceElement`.
     * @param content Vararg parameter to include additional content, particularly for Throwable exceptions.
     * @return A fully constructed log message string containing formatted headers, stack trace, and footer details.
     */
    override fun generateMessage(
        name: String,
        builder: StringBuilder,
        format: String,
        timestamp: Instant,
        caller: StackTraceElement,
        vararg content: Any?
    ): String {
        val messageBuilder = StringBuilder()

        val header = String.format("[%s] ----------- STACKTRACE BEGIN -----------", LoggerUtils.getFormattedTime(timestamp))
        messageBuilder.append(header).append("\n")

        for (obj in content) {
            if (obj is Throwable) {
                processThrowable(name, obj, messageBuilder, timestamp)
            }
        }

        val footer = String.format("[%s] -----------  STACKTRACE END  -----------", LoggerUtils.getFormattedTime(timestamp))
        messageBuilder.append(footer)

        return messageBuilder.toString()
    }

    /**
     * Generates a formatted, colorized console message including stack trace details by processing exceptions
     * and appending stylistic enhancements to indicate the beginning and end of the stack trace.
     *
     * @param name The name of the logging entity or source that generates the log entry.
     * @param builder A StringBuilder instance to construct or append messages.
     * @param format The message format specifying placeholders or the structure of the log output.
     * @param timestamp The timestamp at which the log message is generated.
     * @param caller A StackTraceElement representing the direct caller of the logging method.
     * @param content Additional content such as throwable objects or other informational elements.
     * @return A formatted and color-enhanced string representing the console message, or null if the generation fails.
     */
    override fun generateConsoleMessage(
        name: String,
        builder: StringBuilder,
        format: String,
        timestamp: Instant,
        caller: StackTraceElement,
        vararg content: Any?
    ): String? {
        val message = generateMessage(name, builder, format, timestamp, caller, *content)
        val lines = mutableListOf(*message.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

        // Colorize "STACKTRACE BEGIN" and "STACKTRACE END" in the first and last lines
        val coloredBegin = ANSI.BOLD_RED + "STACKTRACE BEGIN" + ANSI.RESET
        val coloredEnd = ANSI.BOLD_RED + "STACKTRACE END" + ANSI.RESET

        lines[0] = lines.first().replace("STACKTRACE BEGIN", coloredBegin)
        lines[lines.size - 1] = lines.last().replace("STACKTRACE END", coloredEnd)

        return java.lang.String.join("\n", lines)
    }

    /**
     * Processes a given throwable by appending its details and its causes recursively
     * to the provided StringBuilder. Includes the name of the logger and a timestamp
     * for each entry.
     *
     * @param name The name of the logger associated with the throwable.
     * @param throwable The throwable to be processed and logged.
     * @param builder A StringBuilder instance where the log details are appended.
     * @param timestamp The timestamp to associate with the log entries.
     */
    private fun processThrowable(name: String, throwable: Throwable, builder: StringBuilder, timestamp: Instant) {
        builder.append(String.format("[%s] From (Logger Name): %s", LoggerUtils.getFormattedTime(timestamp), name))
            .append("\n")

        // Haupt-Exception
        addExceptionDetails(throwable, builder, timestamp)

        // Ursachen rekursiv verarbeiten
        var cause = throwable.cause
        while (cause != null) {
            addCauseDetails(cause, builder, timestamp)
            cause = cause.cause
        }
    }

    /**
     * Adds details of the provided exception to the given StringBuilder, including the exception type,
     * message, and location in the source code where the exception occurred.
     *
     * @param ex The exception whose details are to be added.
     * @param builder The StringBuilder to which the exception details will be appended.
     * @param timestamp The timestamp at which the exception was logged.
     */
    private fun addExceptionDetails(ex: Throwable, builder: StringBuilder, timestamp: Instant) {
        val line = String.format(
            "[%s] Exception: %s\n[%s] Message: %s\n[%s] At: %s - %s:%d",
            LoggerUtils.getFormattedTime(timestamp),
            ex.javaClass.name,
            LoggerUtils.getFormattedTime(timestamp),
            if (ex.message != null) ex.message else "N/A",
            LoggerUtils.getFormattedTime(timestamp),
            ex.stackTrace[0].className,
            ex.stackTrace[0].fileName,
            ex.stackTrace[0].lineNumber
        )
        builder.append(line).append("\n")
    }

    /**
     * Appends detailed information about the specified cause to a given StringBuilder.
     *
     * @param cause The throwable cause whose details are being logged.
     * @param builder The StringBuilder to which the cause details are appended.
     * @param timestamp The timestamp indicating when the cause details are being logged.
     */
    private fun addCauseDetails(cause: Throwable, builder: StringBuilder, timestamp: Instant) {
        val line = String.format(
            "[%s] Caused by: %s\n[%s] Message: %s",
            LoggerUtils.getFormattedTime(timestamp),
            cause.javaClass.name,
            LoggerUtils.getFormattedTime(timestamp),
            if (cause.message != null) cause.message else "N/A"
        )
        builder.append(line).append("\n")
    }
}
