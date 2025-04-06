package dev.nelmin.logger

import java.util.*
import java.util.concurrent.ExecutorService

/**
 * A logging strategy implementation for handling and formatting stack trace logs.
 * This class extends the base `LoggingStrategy` and focuses on structuring stack
 * trace information with enhanced formatting and additional context.
 *
 * The strategy is designed to process exceptions (`Throwable`) embedded in log content
 * and generate formatted messages containing detailed stack trace information.
 *
 * The logger includes:
 * - Header and footer markers to indicate the start and end of the stack trace section.
 * - Exception details such as class name, message, and primary stack trace location.
 * - Recursive processing of exception causes with clear hierarchy and timestamps.
 *
 * The formatted logs are colorized for console outputs to enhance readability.
 *
 * @constructor Initializes the `StackTraceLoggingStrategy` with a specified `ExecutorService`.
 *
 * @param executor The executor service used within the logging infrastructure.
 */
class StackTraceLoggingStrategy(executor: ExecutorService) : LoggingStrategy(
    "error",
    "STACKTRACE",
    executor,
    ANSI.BOLD_RED
) {
    /**
     * Generates a formatted error or debug message by incorporating stack trace details, timestamp,
     * and additional content for logging purposes.
     *
     * @param name The name or identifier of the module/plugin that is generating the log message.
     * @param builder A StringBuilder instance used to construct the message.
     * @param format A format string, typically determining the timestamp or other log formatting requirements.
     * @param timestamp The Unix timestamp (in milliseconds) representing when the message was generated.
     * @param caller The stack trace element representing the caller of this method.
     * @param content Additional objects or exceptions to be processed and appended to the message.
     * @return A formatted string representing the constructed log message with stack trace details.
     */
    override fun generateMessage(
        name: String,
        builder: StringBuilder,
        format: String,
        timestamp: Long,
        caller: StackTraceElement,
        vararg content: Any?
    ): String {
        // Build the message with plain headers and process exceptions
        val messageBuilder = StringBuilder()

        // Header
        val header = String.format("[%s] ----------- STACKTRACE BEGIN -----------", formatTimestamp(timestamp))
        messageBuilder.append(header).append("\n")

        // Process Throwable in content
        for (obj in content) {
            if (obj is Throwable) {
                processThrowable(name, obj, messageBuilder, timestamp)
            }
        }

        // Footer
        val footer = String.format("[%s] -----------  STACKTRACE END  -----------", formatTimestamp(timestamp))
        messageBuilder.append(footer)

        return messageBuilder.toString()
    }

    /**
     * Generates a console-friendly message by colorizing specific parts of the stack trace output.
     *
     * This method processes a message, appends additional formatting, and highlights the "STACKTRACE BEGIN"
     * and "STACKTRACE END" markers with specific colors for enhanced readability.
     *
     * @param name The name or identifier of the current logging context.
     * @param builder A StringBuilder instance for constructing the message.
     * @param format The format specifier used for the message.
     * @param timestamp The timestamp associated with the log message, represented in milliseconds.
     * @param caller The StackTraceElement representing the point in the application where logging occurred.
     * @param content Optional additional content or objects passed, which may include Throwable instances.
     * @return The fully formatted and colorized stack trace message as a String, or null if no content is available.
     */
    override fun generateConsoleMessage(
        name: String,
        builder: StringBuilder,
        format: String,
        timestamp: Long,
        caller: StackTraceElement,
        vararg content: Any?
    ): String? {
        val message = generateMessage(name, builder, format, timestamp, caller, *content)
        val lines = Arrays.asList(*message.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

        // Colorize "STACKTRACE BEGIN" and "STACKTRACE END" in the first and last lines
        val coloredBegin = ANSI.BOLD_RED + "STACKTRACE BEGIN" + ANSI.RESET
        val coloredEnd = ANSI.BOLD_RED + "STACKTRACE END" + ANSI.RESET

        lines[0] = lines.first().replace("STACKTRACE BEGIN", coloredBegin)
        lines[lines.size - 1] = lines.last().replace("STACKTRACE END", coloredEnd)

        return java.lang.String.join("\n", lines)
    }

    /**
     * Processes a throwable by appending its details, including its causes, to the provided StringBuilder.
     *
     * @param name The name of the module or component where the throwable originated.
     * @param throwable The throwable to process and log details for.
     * @param builder The StringBuilder to which the processed output will be appended.
     * @param timestamp The timestamp indicating when the throwable was encountered.
     */
    private fun processThrowable(name: String, throwable: Throwable, builder: StringBuilder, timestamp: Long) {
        builder.append(String.format("[%s] From (Module/Plugin/CandleMC): %s", formatTimestamp(timestamp), name))
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
     * Appends detailed exception information to the provided StringBuilder.
     *
     * @param ex The throwable instance containing exception data.
     * @param builder The StringBuilder where the exception details will be appended.
     * @param timestamp The timestamp associated with the exception occurrence.
     */
    private fun addExceptionDetails(ex: Throwable, builder: StringBuilder, timestamp: Long) {
        val line = String.format(
            "[%s] Exception: %s\n[%s] Message: %s\n[%s] At: %s - %s:%d",
            formatTimestamp(timestamp),
            ex.javaClass.name,
            formatTimestamp(timestamp),
            if (ex.message != null) ex.message else "N/A",
            formatTimestamp(timestamp),
            ex.stackTrace[0].className,
            ex.stackTrace[0].fileName,
            ex.stackTrace[0].lineNumber
        )
        builder.append(line).append("\n")
    }

    /**
     * Appends details about the given cause to the provided StringBuilder.
     *
     * @param cause The Throwable representing the cause for which details should be added.
     * @param builder The StringBuilder to which the cause details will be appended.
     * @param timestamp The timestamp to associate with the logged cause details.
     */
    private fun addCauseDetails(cause: Throwable, builder: StringBuilder, timestamp: Long) {
        val line = String.format(
            "[%s] Caused by: %s\n[%s] Message: %s",
            formatTimestamp(timestamp),
            cause.javaClass.name,
            formatTimestamp(timestamp),
            if (cause.message != null) cause.message else "N/A"
        )
        builder.append(line).append("\n")
    }
}
