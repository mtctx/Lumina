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
 * A logging strategy class used to handle logging operations, including writing to a file
 * and outputting to the console with varied formatting capabilities. It leverages an
 * asynchronous strategy using an `ExecutorService` to handle log processing and execution
 * to ensure non-blocking behavior.
 */
open class LoggingStrategy {
    /**
     * A `DateTimeFormatter` used for formatting time values with the pattern "HH:mm:ss.SSS",
     * representing hours, minutes, seconds, and milliseconds.
     *
     * This formatter is utilized to standardize time representations within the logging process
     * by ensuring consistency across all logged messages.
     */
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    /**
     * Represents the file system path used for logging or storing log files.
     * This variable is used to define the location where log messages or related data
     * will be recorded or accessed. It plays a central role in handling
     * file-based operations within the logging strategy.
     */
    private val PATH: Path
    /**
     * Represents the log level for the logging strategy.
     * Determines the severity level of log messages to be processed
     * (e.g., DEBUG, INFO, WARN, ERROR).
     */
    private val LOG_LEVEL: String
    /**
     * A thread pool executor service used by the LoggingStrategy for managing asynchronous tasks,
     * such as handling log operations in a non-blocking manner.
     */
    private val executor: ExecutorService
    /**
     * ANSI color code used for formatting console output.
     * Typically utilized to add colored text or highlights for better readability in log messages.
     */
    private val ANSIColor: String

    /**
     * Indicates whether logging should output messages to the console.
     *
     * When set to `true`, log messages will be printed to the console
     * in addition to any other logging outputs configured in the system.
     * When set to `false`, console logging is disabled.
     */
    private val logToConsole: Boolean

    /**
     * Constructs a LoggingStrategy instance with the specified configuration.
     *
     * @param path The path where log files are stored. This will be resolved to an absolute path.
     * @param logLevel The logging level to determine the verbosity of log messages.
     * @param executor The executor service used for asynchronous log operations.
     * @param ansiColor The ANSI color code for log messages, used for formatting in console outputs.
     * @param logToConsole Flag indicating whether the logs should be output to the console. Defaults to true.
     */
    constructor(path: Path, logLevel: String, executor: ExecutorService, ansiColor: String, logToConsole: Boolean = true) {
        this.PATH = path.toAbsolutePath()
        this.LOG_LEVEL = logLevel
        this.executor = executor
        this.ANSIColor = ansiColor
        this.logToConsole = logToConsole
    }

    /**
     * Constructs an instance of the LoggingStrategy class.
     *
     * @param fileName The name of the log file to be created and used by this logging strategy.
     * @param logLevel The level of logging (e.g., DEBUG, INFO, ERROR) to be applied.
     * @param executor The executor service used to perform asynchronous logging tasks.
     * @param ansiColor The ANSI color code to be used for colored console logging.
     * @param logToConsole Determines whether the logs should also be printed to the console. Defaults to true.
     */
    constructor(
        fileName: String, logLevel: String, executor: ExecutorService,
        ansiColor: String, logToConsole: Boolean = true
    ) {
        this.PATH = Path.of(System.getProperty("user.dir"), "logs", fileName.lowercase() + ".log")
            .toAbsolutePath()
        this.LOG_LEVEL = logLevel
        this.executor = executor
        this.ANSIColor = ansiColor
        this.logToConsole = logToConsole
    }

    /**
     * Converts a given timestamp in milliseconds to a formatted date-time string
     * based on the system's default time zone.
     *
     * @param timestamp The timestamp in milliseconds to format.
     * @return A string representation of the formatted date-time.
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
     * Logs the given content with a specific format to both the console and a designated file.
     *
     * This method formats the log message using the provided format and then outputs it to the console
     * if `logToConsole` is enabled, as well as writes it to a file. The execution is handled
     * asynchronously using an executor.
     *
     * @param name The identifier or source of the log message.
     * @param format The format string used to structure the log message. It may include placeholders like %log_level or %package:%line.
     * @param timestamp The timestamp of when the log was generated, represented as a Unix epoch time in milliseconds.
     * @param content The variable-length arguments representing the contents to be included in the log message.
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
                if (logToConsole) {
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
                }
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
     * Generates a formatted message string by replacing placeholders in the given format string.
     *
     * @param name The name to be incorporated into the message, typically representing the source or context of the message.
     * @param builder A StringBuilder instance containing content to replace the `%content` placeholder in the format string.
     * @param format The format template string containing placeholders to be replaced.
     * @param timestamp The timestamp to format and replace the `%timestamp` placeholder in the format string.
     * @param caller StackTraceElement representing the caller's context to replace placeholders like `%package` and `%line`.
     * @param content Additional variable arguments that may provide supplementary data for the message generation.
     * @return A String containing the formatted message with all placeholders replaced.
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
     * Generates a console message by formatting and processing the given parameters.
     * The message is enhanced with ANSI color codes and replaces placeholders in
     * the format string with dynamic values.
     *
     * @param name The name or identifier of the logger or context generating the message.
     * @param builder A StringBuilder containing the main content of the log message.
     * @param format The format string for constructing the log message.
     *               Placeholders like %timestamp, %name, %content, etc., will be replaced dynamically.
     * @param timestamp The timestamp of the log event, represented in milliseconds since the epoch.
     * @param caller The stack trace element representing the calling context for the log
     *               (e.g., class name, line number, etc.).
     * @param content Varargs representing additional content or data to be included in the message.
     * @return A formatted and processed console message string with applied ANSI translations or
     *         null if the formatting process fails.
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
     * Generates a log file message by formatting the provided parameters into the specified format.
     * Ensures that the log level and caller's package and line information are included in the final message.
     *
     * @param name The name of the logger or source generating the log message.
     * @param builder A StringBuilder containing the content of the log.
     * @param format The format string for the log message. May include placeholders like %log_level, %package, %line, etc.
     * @param timestamp The Unix timestamp representing when the log event occurred.
     * @param caller The stack trace element of the caller, containing information about the originating class and line number.
     * @param content Additional parameters to be formatted into the log message.
     * @return The formatted log file message as a String.
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
     * Writes the given message to a file at the specified path.
     * The file will be created if it does not exist, and the message
     * will be appended if the file already exists.
     *
     * @param message The text message to be written to the file.
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