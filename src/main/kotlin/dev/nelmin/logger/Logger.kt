package dev.nelmin.logger

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Logger is a singleton object used for logging messages with various severity levels.
 * It provides different logging strategies for different log levels, and supports
 * logging both to the console and to log files. It also handles asynchronous logging tasks.
 */
object Logger {
    init {
        Files.createDirectories(Path.of(System.getProperty("user.dir") + "/logs"))
    }

    /**
     * Determines whether the logger operates in debug mode.
     * The value is derived from the system property "debugMode".
     * If the property is set and evaluates to `true`, debug mode is enabled.
     */
    private var debug: Boolean = System.getProperty("debugMode")?.toBoolean() == true // Is the Logger in Debug Mode.
    /**
     * The name of the logger instance, used to identify logs produced by this logger.
     */
    private var name: String = "Lumina" // Name of the Logger.

    /**
     * Defines a single-threaded executor service for managing task execution.
     * This is typically used for executing tasks sequentially in a dedicated thread.
     *
     * The executor is useful when thread-safe task execution is required without
     * complex synchronization. Tasks submitted to this executor are guaranteed to
     * be executed in the order they are received.
     */
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    /**
     * Specifies the debug logging strategy used for handling debug-level log messages.
     * This variable is initialized with a `DebugLoggingStrategy` instance, which formats and logs
     * messages asynchronously using the provided executor service while respecting the debug mode status.
     *
     * The selected strategy allows for console output and file logging if the debug mode is enabled.
     * It also ensures logs are generated with caller information and timestamps for better traceability.
     */
    private var debugStrategy: DebugLoggingStrategy = DebugLoggingStrategy(executor, debug)
    /**
     * Represents the strategy for handling error-level logging.
     *
     * This property is configured to define the error logging approach using a specific
     * log level, log category, executor, and ANSI color. The configuration influences
     * how error logs are formatted and processed within the system.
     */
    private val errorStrategy: LoggingStrategy = LoggingStrategy(
        "error", "ERROR", executor, ANSI.RED
    )
    /**
     * Represents the logging strategy specifically configured for fatal-level log messages.
     *
     * This strategy includes a predefined log level of "error", a log type of "FATAL", and
     * employs a specific executor for handling log operations while utilizing bold red ANSI styling
     * for enhanced visual distinction of fatal messages.
     */
    private val fatalStrategy: LoggingStrategy = LoggingStrategy(
        "error", "FATAL", executor, ANSI.BOLD_RED
    )
    /**
     * Represents a logging strategy configured with the "info" log level.
     * Defines the log level name, ANSI color, and the executor responsible
     * for executing the logging operations.
     * Used to log informational messages for the application.
     */
    private val infoStrategy: LoggingStrategy = LoggingStrategy(
        "info", "INFO", executor, ANSI.CYAN
    )
    /**
     * Defines the logging strategy to be used for handling and processing stack traces.
     * This variable is initialized with an instance of `StackTraceLoggingStrategy` which uses
     * the provided executor for executing the logging operations.
     */
    private val stackTraceStrategy: LoggingStrategy = StackTraceLoggingStrategy(executor)
    /**
     * Defines a warning-level logging strategy for the application.
     *
     * This strategy specifies logging behaviors for messages categorized as warnings.
     * It uses "info" as the descriptor for the logging type, applies "WARN" as the log level label,
     * configures the given executor for handling logging operations, and colorizes warning messages
     * using the ANSI yellow color code for better visibility in the console or log outputs.
     */
    private val warnStrategy: LoggingStrategy = LoggingStrategy(
        "info", "WARN", executor, ANSI.YELLOW
    )
    /**
     * A private logging strategy for handling error messages.
     * This strategy is designed to specifically log error-level messages
     * to a file while applying red ANSI coloring to the log output.
     *
     * @property errorToFileOnlyStrategy A constant instance of [LoggingStrategy] configured for error-level logging with red coloring.
     */
    private val errorToFileOnlyStrategy: LoggingStrategy = LoggingStrategy(
        "error", "ERROR", executor, ANSI.RED, false
    )
    /**
     * A private logging strategy instance configured specifically for handling fatal log messages.
     * This strategy is designed to log messages of "FATAL" level and higher only to a file,
     * and not to the console. It utilizes a specific color coding for better visual distinction.
     *
     * @property fatalToFileOnlyStrategy Represents the logging strategy for fatal level logs.
     */
    private val fatalToFileOnlyStrategy: LoggingStrategy = LoggingStrategy(
        "error", "FATAL", executor, ANSI.BOLD_RED, false
    )
    /**
     * Represents a logging strategy dedicated to logging informational messages to a file only.
     * The logging level is set to "INFO", and the associated ANSI color for this strategy is cyan.
     * This strategy does not enable any additional configuration flags by default.
     */
    private val infoToFileOnlyStrategy: LoggingStrategy = LoggingStrategy(
        "info", "INFO", executor, ANSI.CYAN, false
    )
    /**
     * A private instance of the `LoggingStrategy` class configured to log warnings to a file only.
     * The strategy is initialized with specific logging preferences including:
     * - Log level set to "info".
     * - Log type set to "WARN".
     * - An associated executor for asynchronous logging.
     * - Text coloration for ANSI output set to yellow.
     * - Excludes console logging by setting console output to false.
     */
    private val warnToFileOnlyStrategy: LoggingStrategy = LoggingStrategy(
        "info", "WARN", executor, ANSI.YELLOW, false
    )

    /**
     * Sets the name of the Logger.
     *
     * @param name The name to be assigned to the Logger.
     */
    fun setName(name: String) {
        Logger.name = name
    }

    /**
     * Logs messages using the specified logging strategy and content.
     *
     * @param strategy the logging strategy used to define how the log is processed and output.
     * @param content a variable number of arguments representing the content to be logged.
     */
    fun log(strategy: LoggingStrategy, vararg content: Any) {
        val formatNormal = "[%timestamp] - %log_level - %name - %content"
        val formatDebug = "[%timestamp] - %log_level - %name - %package:%line - %content"
        strategy.log(
            name,
            if (debug) formatDebug else formatNormal,
            System.currentTimeMillis(),
            *content
        )
    }

    /**
     * Logs the provided content using the debug logging strategy.
     *
     * @param content The vararg array of objects to be logged.
     */
    fun debug(vararg content: Any) {
        log(debugStrategy, *content)
    }

    /**
     * Logs the provided content as an error using the specified logging strategy.
     *
     * @param content The content to be logged, provided as a variable-length argument list.
     */
    fun error(vararg content: Any) {
        log(errorStrategy, *content)
    }

    /**
     * Logs the given content using a fatal logging strategy.
     *
     * @param content The content to be logged. Multiple arguments can be provided.
     */
    fun fatal(vararg content: Any) {
        log(fatalStrategy, *content)
    }

    /**
     * Logs information messages using the info logging strategy.
     *
     * @param content The data to be logged. Accepts a variable number of arguments of any type.
     */
    fun info(vararg content: Any) {
        log(infoStrategy, *content)
    }

    /**
     * Logs the provided stack trace using the configured logging strategy.
     *
     * @param stackTrace The throwable whose stack trace will be logged.
     */
    fun stacktrace(stackTrace: Throwable) {
        log(stackTraceStrategy, stackTrace)
    }

    /**
     * Logs a warning message using the specified logging strategy.
     *
     * @param content The content to be logged as a warning. Can accept multiple arguments.
     */
    fun warn(vararg content: Any) {
        log(warnStrategy, *content)
    }

    /**
     * Logs error messages using an error logging strategy without displaying them to the user.
     * This function is intended to silently log errors for diagnostic purposes.
     *
     * @param content The messages or objects to be logged as error, provided as vararg arguments.
     */
    fun errorSilent(vararg content: Any) {
        log(errorToFileOnlyStrategy, *content)
    }

    /**
     * Logs the provided content using a fatal log level strategy that outputs only to a file,
     * without displaying the log in the console or other output mediums.
     *
     * @param content The content to be logged. Can include multiple objects or messages,
     *                which will be formatted and logged using the defined strategy.
     */
    fun fatalSilent(vararg content: Any) {
        log(fatalToFileOnlyStrategy, *content)
    }

    /**
     * Logs informational messages silently following a specific logging strategy.
     * The messages will only be logged to a file without any additional output.
     *
     * @param content The content to be logged. Can be a variable number of arguments of any type.
     */
    fun infoSilent(vararg content: Any) {
        log(infoToFileOnlyStrategy, *content)
    }

    /**
     * Logs the provided content using the warn-to-file-only strategy without generating console output.
     *
     * @param content A variable number of arguments to be logged as warning messages.
     */
    fun warnSilent(vararg content: Any) {
        log(warnToFileOnlyStrategy, *content)
    }
}
