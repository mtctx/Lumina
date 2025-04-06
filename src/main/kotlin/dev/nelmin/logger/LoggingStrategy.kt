package dev.nelmin.logger

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

/**
 * A base class for defining a logging strategy.
 * Provides methods for formatting log messages, generating console and file-specific log outputs,
 * and writing log messages to a persistent file. The class supports multiple constructors
 * to initialize the logging setup with either a predefined path or a filename.
 */
open class LoggingStrategy {
    /**
     * A DateTimeFormatter instance used for formatting timestamps in the `HH:mm:ss.SSS` format.
     * This formatter ensures consistent representation of time with hours, minutes, seconds,
     * and milliseconds throughout the logging process in the containing logging strategy.
     */
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    /**
     * Represents the file system path utilized by the logging strategy for file-based logging operations.
     * This path specifies the location where log files are stored or accessed.
     */
    private val PATH: Path
    /**
     * Defines the logging level for the application. Determines the verbosity of log messages
     * produced by the logging system, such as DEBUG, INFO, WARN, or ERROR.
     */
    private val LOG_LEVEL: String
    /**
     * ExecutorService responsible for handling asynchronous logging tasks.
     * Used to ensure that logging operations are performed efficiently
     * without blocking the main thread.
     */
    private val executor: ExecutorService
    /**
     * Represents the ANSI color code used for formatting console output.
     * This variable is used internally within the logging strategy to apply
     * color formatting to log messages for better readability in the console.
     */
    private val ANSIColor: String

    /**
     * Initializes a new instance of the LoggingStrategy class.
     *
     * @param path The file system path to be used for logging purposes, stored as an absolute path.
     * @param logLevel Specifies the logging level, determining the verbosity of logs.
     * @param executor The ExecutorService responsible for handling asynchronous task execution.
     * @param ansiColor The ANSI color code used for stylizing log messages in the console.
     */
    constructor(path: Path, logLevel: String, executor: ExecutorService, ansiColor: String) {
        this.PATH = path.toAbsolutePath()
        this.LOG_LEVEL = logLevel
        this.executor = executor
        this.ANSIColor = ansiColor
    }

    /**
     * Constructor for initializing a LoggingStrategy instance with specified parameters.
     *
     * @param fileName The name of the log file to be used for logging. This is converted to lowercase and
     *                 saved with a `.log` extension under the `logs` directory in the user working directory.
     * @param logLevel The logging level used to filter logged messages.
     * @param executor An instance of ExecutorService responsible for managing asynchronous logging tasks.
     * @param ansiColor ANSI color code used to format console log messages with a specific color.
     */
    constructor(
        fileName: String, logLevel: String, executor: ExecutorService,
        ansiColor: String
    ) {
        this.PATH = Path.of(System.getProperty("user.dir"), "logs", fileName.lowercase() + ".log")
            .toAbsolutePath()
        this.LOG_LEVEL = logLevel
        this.executor = executor
        this.ANSIColor = ansiColor
    }

    /**
     * Formats a given timestamp into a human-readable string representation based on the default time zone.
     *
     * @param timestamp The timestamp in milliseconds since the epoch to be formatted.
     * @return A string representation of the formatted timestamp.
     */
    fun formatTimestamp(timestamp: Long): String {
        return TIME_FORMATTER.format(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
            )
        )
    }

    /**
     * Logs a message with the specified parameters and writes it to both the console and a log file.
     *
     * @param name The name or tag identifying the source or type of the log.
     * @param format The format string defining the structure of the log message.
     * @param timestamp The timestamp of the log entry as a long value.
     * @param content Additional content to be included in the log message.
     */
    open fun log(name: String, format: String, timestamp: Long, vararg content: Any) {
        val stackTraceElements = Thread.currentThread().stackTrace
        val caller = stackTraceElements[4]

        executor.execute {
            val stringBuilder = StringBuilder()
            for (`object` in content) {
                stringBuilder.append(`object`)
            }
            try {
                println(
                    generateConsoleMessage(
                        name,
                        stringBuilder,
                        format,
                        timestamp,
                        caller,
                        *content
                    )
                )
                writeToFile(
                    generateLogFileMessage(
                        name,
                        stringBuilder,
                        format,
                        timestamp,
                        caller,
                        *content
                    )
                )
            } catch (e: IOException) {
                System.err.println("Log write error: " + e.message)
            }
        }
    }

    /**
     * Generates a formatted message based on the provided input parameters.
     *
     * @param name The name used as a placeholder in the format string.
     * @param builder A StringBuilder instance used to append or process additional message content.
     * @param format The string format defining how the final message should be structured.
     * @param timestamp The timestamp value used to replace the %timestamp placeholder in the format string.
     * @param caller A StackTraceElement providing information about the caller, such as its class name and line number.
     * @param content Additional optional content to be included in the message.
     * @return A string containing the generated message after replacing the placeholders with the specified values.
     */
    open fun generateMessage(
        name: String, builder: StringBuilder, format: String, timestamp: Long,
        caller: StackTraceElement, vararg content: Any?
    ): String {
        val newFormat = format
            .replace("%timestamp", formatTimestamp(timestamp))
            .replace("%name", name)
            .replace("%package", caller.className)
            .replace("%line", caller.lineNumber.toString())

        return newFormat
            .replace("%content", builder.toString())
            .replace(
                "\n ", """
     
     ${newFormat.replace("%content", "")}
     """.trimIndent()
            )
    }

    /**
     * Generates a formatted console message with the provided parameters.
     *
     * @param name The name or identifier for the message source.
     * @param builder A StringBuilder instance used to construct the message.
     * @param format The format string for constructing the message.
     * @param timestamp The timestamp associated with the message, typically in milliseconds since epoch.
     * @param caller The stack trace element representing the caller of this method.
     * @param content Additional content or arguments for the formatted message.
     * @return A formatted console message as a String, with ANSI codes applied, or null if message generation fails.
     */
    open fun generateConsoleMessage(
        name: String, builder: StringBuilder, format: String, timestamp: Long,
        caller: StackTraceElement, vararg content: Any?
    ): String? {
        // Instead of using ANSI in the code, you can now use & in your code
        // For more information see ANSI::translateToANSI and ANSI::getANSICode
        return ANSI.translateToANSI(generateMessage(name, builder, format, timestamp, caller, *content)
            .replace("%log_level", ANSIColor + LOG_LEVEL + ANSI.RESET))
    }

    /**
     * Generates a formatted log file message by customizing the input format string to include
     * package and line information, and then delegates to the underlying message generator.
     *
     * @param name The name associated with the log message, typically identifying the source or context.
     * @param builder A StringBuilder containing additional content to include in the message.
     * @param format The format template for the log message, which may include placeholders like %log_level, %package, %line, etc.
     * @param timestamp The timestamp to include in the log message, typically representing when the log event occurred.
     * @param caller A StackTraceElement representing the caller's class, method, and line number information.
     * @param content Additional content elements to be embedded in the formatted log message as specified in the format template.
     * @return A formatted log message string intended for log file output.
     */
    fun generateLogFileMessage(
        name: String, builder: StringBuilder, format: String, timestamp: Long,
        caller: StackTraceElement, vararg content: Any?
    ): String {
        var fileFormat = format
        if (!fileFormat.contains("%package:%line")) {
            fileFormat = fileFormat.replaceFirst("%log_level".toRegex(), "%log_level - %package:%line")
        }
        return generateMessage(name, builder, fileFormat, timestamp, caller, *content)
            .replace("%log_level", LOG_LEVEL)
    }

    /**
     * Writes the specified message to a file. The message will be appended to the file,
     * and a new line will be added after the message.
     *
     * @param message The log message to be written to the file.
     * @throws IOException If an I/O error occurs while writing to the file.
     */
    @Throws(IOException::class)
    fun writeToFile(message: String) {
        Files.newBufferedWriter(
            PATH,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        ).use { writer ->
            writer.write(message)
            writer.newLine()
        }
    }
}