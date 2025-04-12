package dev.nelmin.logger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Base class for implementing logging strategies. Provides mechanisms to log messages to both the
 * console and a file, utilizing thread-safe operations for writing logs. The strategy name and
 * ANSI color can customize the logging format and appearance.
 *
 * @constructor Initializes the logging strategy with the specified parameters.
 * @param strategyName The name of the logging strategy.
 * @param coroutineScope Scope for coroutines used in asynchronous operations.
 * @param mutex A mutex to ensure thread-safe file writes.
 * @param ansiColor ANSI color code string for customizing console log appearance.
 */
open class LoggingStrategy(
    private val strategyName: String, private val coroutineScope: CoroutineScope, private val mutex: Mutex,
    private val ansiColor: String
) {
    /**
     * The file path for the log file associated with the specific strategy.
     *
     * This property determines where the logs for the given strategy are stored based on its name.
     * The path is constructed using the `getLogFileForStrategy` function from `LoggerUtils`,
     * which creates a log file path within a structured "logs" directory.
     */
    private val path = LoggerUtils.getLogFileForStrategy(strategyName)

    /**
     * Logs a message synchronously by calling the `log` method within a `runBlocking` block.
     *
     * @param name The name of the logger or log source.
     * @param format The format string used for logging the message.
     * @param timestamp The timestamp of the log entry.
     * @param logToConsole A boolean indicating whether the log message should be printed to the console.
     * @param content Vararg parameter representing the content to be logged.
     *
     * @deprecated Use the `log` method instead for non-blocking logging.
     */
    @Deprecated("Use log instead", ReplaceWith("log(name, format, timestamp, logToConsole, *content)"))
    open fun logSync(name: String, format: String, timestamp: Instant, logToConsole: Boolean, vararg content: Any) =
        runBlocking { log(name, format, timestamp, logToConsole, *content) }

    /**
     * Logs a message to both the console and a file, depending on the given parameters.
     *
     * @param name The name or identifier for the log message.
     * @param format The format template for the log message.
     * @param timestamp The timestamp associated with the log message.
     * @param logToConsole A flag indicating whether the log should also be printed to the console.
     * @param content The content elements included in the log message.
     */
    open suspend fun log(
        name: String,
        format: String,
        timestamp: Instant,
        logToConsole: Boolean,
        vararg content: Any
    ) {
        val stackTraceElements = Thread.currentThread().stackTrace
        val caller = stackTraceElements.first { it.className != this.javaClass.name }

        val stringBuilder = StringBuilder()
        val contentAsString = content.joinToString(" ") { it.toString() }
        stringBuilder.append(contentAsString)

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

    /**
     * Generates a formatted message by replacing specific placeholders in the provided format string
     * with contextual information such as timestamp, logger name, package name, line number, and content.
     *
     * @param name The name of the logger or logging entity.
     * @param builder A StringBuilder object containing the content to be injected into the message.
     * @param format The message format string with placeholders such as `%timestamp`, `%loggerName`, etc.
     * @param timestamp The timestamp to be formatted and injected into the message.
     * @param caller The StackTraceElement of the calling method to extract details like class name and line number.
     * @param content Additional optional content to be included in the message.
     * @return A formatted string with placeholders replaced by the respective values.
     */
    open fun generateMessage(
        name: String, builder: StringBuilder, format: String, timestamp: Instant,
        caller: StackTraceElement, vararg content: Any?
    ): String = format
            .replace("%timestamp", LoggerUtils.getFormattedTime(timestamp))
            .replace("%loggerName", name)
            .replace("%package", caller.className)
            .replace("%line", caller.lineNumber.toString())
            .replace("%content", builder.toString())

    /**
     * Generates a console-friendly log message by formatting and translating a base message
     * into ANSI-compatible text with additional styling or transformations.
     *
     * @param name The name of the logger or logging operation.
     * @param builder A [StringBuilder] containing the dynamic content to include in the message.
     * @param format A string format specifying the message layout and placeholders.
     * @param timestamp The timestamp associated with the message.
     * @param caller A [StackTraceElement] providing details about the caller's class, method, and location in the code.
     * @param content Additional optional content to include in the message.
     * @return A formatted ANSI-compatible console-friendly message, or null if no message is generated.
     */
    open fun generateConsoleMessage(
        name: String, builder: StringBuilder, format: String, timestamp: Instant,
        caller: StackTraceElement, vararg content: Any?
    ): String? = ANSI.translateToANSI(
            generateMessage(name, builder, format, timestamp, caller, *content)
                .replace("%strategyName", ansiColor + strategyName + ANSI.RESET)
        )

    /**
     * Generates a log message specifically formatted for writing to a log file.
     * The method allows dynamic formatting and content insertion into the message
     * string based on provided arguments. Additionally, it ensures the format string
     * includes `%package:%line` if it is not already present.
     *
     * @param name The name of the logger or strategy.
     * @param builder A StringBuilder instance used for appending dynamic content into the message.
     * @param format The formatting string specifying placeholders to construct the log message.
     * @param timestamp The timestamp representing when the log event occurred.
     * @param caller The stack trace element referencing the caller of this method, used for contextual information.
     * @param content Vararg of additional data to include within the log message.
     * @return The formatted log message ready to be written to a log file.
     */
    open fun generateLogFileMessage(
        name: String, builder: StringBuilder, format: String, timestamp: Instant,
        caller: StackTraceElement, vararg content: Any?
    ): String {
        var fileFormat = format
        if (!fileFormat.contains("%package:%line")) {
            fileFormat = fileFormat.replaceFirst("%strategyName".toRegex(), "%strategyName - %package:%line")
        }
        return generateMessage(name, builder, fileFormat, timestamp, caller, *content)
            .replace("%strategyName", strategyName)
    }

    /**
     * Writes a given message to a file.
     *
     * The method acquires a lock to ensure thread safety and writes the message
     * to the file specified by the `path` property using UTF-8 encoding.
     * If the file does not exist, it is created. If it exists, the message is appended.
     *
     * @param message The content to be written to the file.
     * @return Unit
     * @throws IOException If an I/O error occurs during the file write operation.
     */
    @Throws(IOException::class)
    open suspend fun writeToFile(message: String): Unit =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                Files.write(
                    path,
                    listOf(message),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                )
            }
        }
}