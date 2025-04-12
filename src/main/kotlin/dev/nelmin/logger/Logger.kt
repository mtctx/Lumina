package dev.nelmin.logger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * Singleton object responsible for logging operations within the application.
 * Provides various logging levels such as debug, error, fatal, info, warn,
 * as well as stack trace logging. Supports both file and console logging.
 */
object Logger {
    init {
        Files.createDirectories(LoggerUtils.logsDir)
    }

    /**
     * Determines whether the logger operates in debug mode.
     *
     * The value of this variable is derived from the system property `debugMode`. If the `debugMode` property
     * is set and evaluates to `true`, this variable will be set to `true`. Otherwise, it defaults to `false`.
     */
    private var debug: Boolean = System.getProperty("debugMode")?.toBoolean() == true // Is the Logger in Debug Mode.

    /**
     * Represents the default name used within the class or module.
     * This may serve as a label, identifier, or default value in the context where it is used.
     */
    private var name: String = "Lumina"

    /**
     * Represents a mutable `CoroutineScope` instance initialized with a `SupervisorJob`.
     *
     * This variable is intended to provide a structured concurrency scope, which acts as the parent for
     * child coroutines. The `SupervisorJob` ensures that cancellation or failure of one child coroutine
     * does not propagate to other child coroutines within the scope.
     *
     * The `coroutineScope` variable is often used to launch coroutines that need to run independently
     * and concurrently with error isolation.
     */
    var coroutineScope = CoroutineScope(SupervisorJob())

    /**
     * A mutex (mutual exclusion) object used to synchronize access to shared resources
     * across multiple coroutines or threads, ensuring that only one coroutine or thread
     * can access the protected resource at a time.
     */
    var mutex = Mutex()

    /**
     * Represents a logging strategy specifically tailored for debug-level messages.
     * This strategy is constructed using the `LoggingStrategyBuilder` with predefined
     * configurations, including a unique name ("DEBUG"), a coroutine scope for asynchronous
     * operations, a mutex for ensuring thread-safe file writes, and an ANSI color for
     * console output customization.
     */
    private val debugStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "DEBUG",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.GREEN,
    )

    /**
     * Represents a logging strategy specifically designed for handling error-level logs.
     *
     * This strategy is built using `LoggingStrategyBuilder` and assigned a name ("ERROR"),
     * a coroutine scope for managing asynchronous operations, a mutex for ensuring
     * thread-safe file writes, and an ANSI color code (`ANSI.RED`) for distinguishing error
     * logs in the console output. The strategy aids in centralized and organized logging
     * of error events in the application.
     */
    private val errorStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "ERROR",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.RED,
    )

    /**
     * Represents a logging strategy specifically tailored for handling fatal error messages.
     *
     * This strategy utilizes the `LoggingStrategyBuilder` to define a logging mechanism
     * with the following characteristics:
     * - The strategy is named "FATAL".
     * - Operates within the provided coroutine scope for asynchronous logging operations.
     * - Employs a mutex to ensure thread-safe logging to files.
     * - Uses bold red ANSI color formatting to highlight fatal messages in the console.
     *
     * The `fatalStrategy` provides a specific configuration suitable for logging
     * critical issues that may require immediate attention.
     */
    private val fatalStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "FATAL",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.BOLD_RED,
    )

    /**
     * A logging strategy for informational messages, customized with an "INFO" strategy name and
     * a cyan-colored ANSI styling for console logs. This strategy enables thread-safe logging
     * using a coroutine scope and mutex for synchronous file operations.
     *
     * The `infoStrategy` is built using the `LoggingStrategyBuilder` class, which inherits
     * from the base `LoggingStrategy`. It is tailored for asynchronously logging information-level
     * messages to both console and file with specific formatting and appearance.
     */
    private val infoStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "INFO",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.CYAN,
    )

    /**
     * Defines a logging strategy specifically for handling stack trace-based logs. This variable
     * utilizes the `StackTraceLoggingStrategy`, which is a specialized implementation of the
     * `LoggingStrategy` class.
     *
     * The `stackTraceStrategy` facilitates capturing, formatting, and logging stack trace information
     * to both the console and a file. It provides mechanisms for thread-safe operations and leverages
     * ANSI styling for console logs. This implementation is tailored to highlight the logs with a bold red
     * color for better visibility.
     *
     * Parameters related to `stackTraceStrategy`:
     * - `coroutineScope`: The coroutine scope used for asynchronous logging operations.
     * - `mutex`: A thread-safety mechanism ensuring logs are written to files without race conditions.
     * - `ANSI.BOLD_RED`: The ANSI color used to style log messages for console output, emphasizing them in red.
     */
    private val stackTraceStrategy: LoggingStrategy = StackTraceLoggingStrategy(coroutineScope, mutex, ANSI.BOLD_RED)

    /**
     * Defines a logging strategy for `WARN` level messages. This strategy is configured
     * to log messages with a yellow ANSI color when printed to the console, and provides
     * thread-safe file-based logging using the specified coroutine scope and mutex.
     *
     * The `warnStrategy` is initialized using the `LoggingStrategyBuilder` and encapsulates
     * the behavior for handling log messages categorized under the warning level.
     */
    private val warnStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "WARN",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.YELLOW,
    )

    /**
     * Sets the name of the logger.
     *
     * @param name The new name to set for the logger.
     */
    fun setName(name: String) {
        Logger.name = name
    }

    /**
     * Logs a message using the specified logging strategy, format, and additional content.
     * The log message is dynamically formatted based on whether debug mode is enabled
     * and can optionally be logged to the console in addition to being logged using the strategy.
     *
     * @param strategy The logging strategy to use for logging the message.
     * @param logToConsole A boolean indicating whether the log message should be printed to the console.
     * @param content Vararg of additional content to include in the log message.
     */
    suspend fun log(strategy: LoggingStrategy, logToConsole: Boolean, vararg content: Any) =
            strategy.log(
                name,
                if (debug)
                    "[%timestamp] - %strategyName - %loggerName - %package:%line - %content"
                else
                    "[%timestamp] - %strategyName - %loggerName - %content",
                Clock.System.now(),
                logToConsole,
                *content
            )

    /**
     * Logs debugging information using the provided logging strategy.
     *
     * @param content The content to be logged. Can include multiple objects or strings.
     * @param logToConsole A boolean flag indicating whether the log should also be output to the console. Defaults to true.
     */
    suspend fun debug(vararg content: Any, logToConsole: Boolean = true) =
        log(debugStrategy, logToConsole, *content)

    /**
     * Logs an error message using the specified logging strategy.
     *
     * @param content The contents or messages to be logged.
     * @param logToConsole Indicates whether the error message should also be logged to the console. Defaults to true.
     */
    suspend fun error(vararg content: Any, logToConsole: Boolean = true) =
        log(errorStrategy, logToConsole, *content)

    /**
     * Logs a fatal level message using the provided logging strategy.
     *
     * @param content Vararg parameter representing the message content to be logged.
     * @param logToConsole A boolean flag to determine whether the log should also be output to the console. Defaults to true.
     */
    suspend fun fatal(vararg content: Any, logToConsole: Boolean = true) =
        log(fatalStrategy, logToConsole, *content)

    /**
     * Logs the provided content using the info logging strategy.
     *
     * @param content An array of content objects to be logged.
     * @param logToConsole Indicates whether the logs should also be printed to the console. The default value is true.
     */
    suspend fun info(vararg content: Any, logToConsole: Boolean = true) =
        log(infoStrategy, logToConsole, *content)

    /**
     * Logs the provided throwable stack trace using the configured logging strategy.
     *
     * @param stackTrace The throwable whose stack trace needs to be logged.
     * @param logToConsole Whether the stack trace should also be logged to the console. Defaults to true.
     */
    suspend fun stacktrace(stackTrace: Throwable, logToConsole: Boolean = true) =
        log(stackTraceStrategy, logToConsole, stackTrace)

    /**
     * Logs a warning message using the specified warn strategy.
     *
     * @param content The content elements to be logged as a warning.
     * @param logToConsole Determines whether the message should also be logged to the console. Default is true.
     */
    suspend fun warn(vararg content: Any, logToConsole: Boolean = true) =
        log(warnStrategy, logToConsole, *content)
}