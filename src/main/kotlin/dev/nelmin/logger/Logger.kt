package dev.nelmin.logger

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Singleton object that provides logging utilities for various log levels including debug, error, fatal, info, and warnings.
 * Supports configurable logging strategies and formats based on whether debug mode is enabled.
 * The logger writes logs asynchronously using a single-threaded executor.
 */
object Logger {
    init {
        Files.createDirectories(Path.of(System.getProperty("user.dir") + "/logs"))
    }

    /**
     * Flag indicating whether the logger operates in debug mode.
     *
     * The value is determined by the "debugMode" system property. If the property is not set or
     * cannot be parsed to a boolean, the default value is `false`. When enabled, the logger may
     * provide additional debugging information to aid in development or troubleshooting.
     */
    private var debug: Boolean = System.getProperty("debugMode")?.toBoolean() ?: false // Is the Logger in Debug Mode.
    /**
     * Represents the name of the logger instance.
     *
     * This variable holds the default name assigned to the logger, which can be
     * used for identification or referencing purposes within the logging system.
     */
    private var name: String = "Lumina" // Name of the Logger.

    /**
     * A single-threaded executor service dedicated to handling all logging operations, ensuring
     * that log messages are processed sequentially in the order they are submitted. This prevents
     * race conditions and maintains the integrity of log output when multiple threads produce logs
     * concurrently.
     */
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    /**
     * Represents the logging strategy used for debugging purposes.
     *
     * This variable holds an instance of [DebugLoggingStrategy], which is responsible for handling
     * debug-level logging operations. It utilizes an executor service for asynchronous logging
     * tasks and provides the capability to toggle debug mode.
     *
     * By default, the debug strategy is configured for handling logs with "DEBUG" severity,
     * executed using the provided executor, and formatted with ANSI green color for console outputs.
     */
    private var debugStrategy: DebugLoggingStrategy = DebugLoggingStrategy(executor, debug)
    /**
     * Represents a specific logging strategy configured to handle error-level log messages.
     *
     * This strategy is initialized with the following configurations:
     * - The log file is named "error.log" and stored in the logs directory located in the current working directory.
     * - The logging level is set to "ERROR" to denote critical issues or failures.
     * - An `ExecutorService` is utilized for asynchronous log processing and writing to the log file.
     * - ANSI.RED is used as the color code for formatting the error messages output to the console.
     *
     * The `errorStrategy` serves as a predefined logging configuration for managing error-related log entries efficiently.
     */
    private val errorStrategy: LoggingStrategy = LoggingStrategy(
        "error", "ERROR", executor, ANSI.RED
    )
    /**
     * A predefined logging strategy intended for fatal-level log messages.
     * This strategy is configured to log messages with a "FATAL" severity level.
     * It uses "error" as the filename for storing the logs, applies a bold red color (ANSI.BOLD_RED)
     * for console output, and utilizes a shared executor service for asynchronous logging operations.
     */
    private val fatalStrategy: LoggingStrategy = LoggingStrategy(
        "error", "FATAL", executor, ANSI.BOLD_RED
    )
    /**
     * A specific logging strategy configuration for handling log messages designated
     * with the "INFO" log level. It utilizes a cyan ANSI color code for console output.
     *
     * This instance is configured to write log messages to a file named "info.log",
     * located in the "logs" directory of the user's current working directory. It uses
     * an `ExecutorService` for asynchronous logging operations to improve performance.
     *
     * @property infoStrategy The `LoggingStrategy` instance configured for "INFO" level logging.
     */
    private val infoStrategy: LoggingStrategy = LoggingStrategy(
        "info", "INFO", executor, ANSI.CYAN
    )
    /**
     * A private variable representing the strategy used for logging stack traces.
     * This variable is initialized with a specific implementation of the `LoggingStrategy` interface,
     * which is designed to handle the logging of stack trace information.
     */
    private val stackTraceStrategy: LoggingStrategy = StackTraceLoggingStrategy(executor)
    /**
     * A logging strategy instance configured for handling warning-level log messages.
     *
     * The `warnStrategy` is a customized implementation of the `LoggingStrategy` class, tailored for
     * managing log entries with a log level of "WARN". It is configured to utilize a specific executor
     * service for asynchronous log handling and color format (ANSI.YELLOW) for console output. The
     * logs will also include a timestamp and contextual information such as the file name and line
     * number where the log was generated.
     *
     * Properties of `warnStrategy`:
     * - Log file is named "info.log" and is stored in the "logs" directory of the current working directory.
     * - Log entries are marked with the "WARN" log level for easy identification.
     * - Uses the provided executor service for efficient and non-blocking log writing.
     * - Console messages are color-coded with yellow formatting for clear warning visualization.
     */
    private val warnStrategy: LoggingStrategy = LoggingStrategy(
        "info", "WARN", executor, ANSI.YELLOW
    )

    /**
     * Sets the name for the Logger.
     *
     * @param name The new name to assign to the Logger.
     */
    fun setName(name: String) {
        Logger.name = name
    }

    /**
     * Logs the provided content using the specified logging strategy and format.
     *
     * @param strategy The logging strategy to be used for logging operations. It dictates
     * the logging behavior such as log level, format, and output destination.
     * @param content The content to be logged, provided as a vararg parameter. This allows
     * multiple contents to be passed and logged together in the specified format.
     */
    fun log(strategy: LoggingStrategy, vararg content: Any) {
        val format_normal = "[%timestamp] - %log_level - %name - %content"
        val format_debug = "[%timestamp] - %log_level - %name - %package:%line - %content"
        strategy.log(
            name,
            if (debug) format_debug else format_normal,
            System.currentTimeMillis(),
            *content
        )
    }

    /**
     * Logs the provided content using the debug logging strategy.
     *
     * @param content A variable number of arguments representing the content to be logged.
     */
    fun debug(vararg content: Any) {
        log(debugStrategy, *content)
    }

    /**
     * Logs an error message using the specified error logging strategy.
     *
     * @param content The content to be logged as an error. Accepts a variable number of arguments.
     */
    fun error(vararg content: Any) {
        log(errorStrategy, *content)
    }

    /**
     * Logs a message with a fatal logging level. This indicates a critical issue
     * in the application that requires immediate attention.
     *
     * @param content The content to be logged. It supports a variable number of arguments.
     */
    fun fatal(vararg content: Any) {
        log(fatalStrategy, *content)
    }

    /**
     * Logs information messages using the predefined info strategy.
     *
     * @param content The list of any number of objects to be included in the log message.
     */
    fun info(vararg content: Any) {
        log(infoStrategy, *content)
    }

    /**
     * Logs the stack trace of a given throwable.
     *
     * @param stackTrace The throwable whose stack trace will be logged.
     */
    fun stacktrace(stackTrace: Throwable) {
        log(stackTraceStrategy, stackTrace)
    }

    /**
     * Logs a warning message using the provided content and the predefined warning logging strategy.
     *
     * @param content A variable number of arguments that represent the content to be logged as a warning.
     */
    fun warn(vararg content: Any) {
        log(warnStrategy, *content)
    }
}
