package dev.nelmin.logger

import dev.nelmin.logger.Logger.coroutineScope
import dev.nelmin.logger.Logger.mutex
import dev.nelmin.logger.strategy.LoggingStrategy
import dev.nelmin.logger.strategy.LoggingStrategyBuilder
import dev.nelmin.logger.strategy.StackTraceLoggingStrategy

/**
 * Object that provides a collection of default logging strategies.
 * These strategies are predefined for commonly used logging levels
 * such as debug, error, fatal, info, stack trace, and warnings.
 * Each strategy includes configuration for its name, appearance, and
 * concurrent safety features like coroutine scope and mutex.
 */
object DefaultLoggingStrategies {

    /**
     * A logging strategy initialized for the debug level.
     *
     * `debugStrategy` is a pre-configured instance of `LoggingStrategy` specifically tailored
     * for logging with a debug level. It uses the debug strategy name, a green ANSI color for styling
     * the console logs, and relies on a provided coroutine scope and mutex for thread-safe and
     * asynchronous operations. Logs are generated in both console and file outputs according
     * to the specified formatting and message-handling logic.
     */
    val debugStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "DEBUG",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.GREEN,
    )

    /**
     * Defines the logging strategy for error-level logs, utilizing the [LoggingStrategy] base class.
     *
     * This strategy is specifically designed to handle error logs, using a designated name ("ERROR")
     * and red ANSI color to differentiate error messages visually in the console.
     * It employs thread-safe mechanisms and supports asynchronous coroutine-based operations
     * for writing logs to a file or displaying them in the console.
     *
     * Key characteristics:
     * - `strategyName` is set to "ERROR", identifying the purpose of this strategy.
     * - `coroutineScope` manages asynchronous logging operations effectively.
     * - `mutex` is used to synchronize access during file writes to prevent race conditions.
     * - `ansiColor` is set to red, making error messages visually distinct when printed to the console.
     */
    val errorStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "ERROR",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.RED,
    )

    /**
     * Represents a logging strategy for handling fatal errors. Uses the `LoggingStrategyBuilder`
     * to configure the strategy with a predefined name, coroutine scope, thread-safe operations
     * through a mutex, and a specific ANSI color for console log appearance.
     *
     * This strategy logs messages with a "FATAL" label and ensures an emphasis on critical
     * error logging through bold red text for console output.
     */
    val fatalStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "FATAL",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.BOLD_RED,
    )

    /**
     * A logging strategy implementation specifically configured for logging information messages.
     * This strategy is initialized with predefined settings, including a unique name, ANSI color
     * for console messages, and mechanisms for thread-safe and asynchronous operations.
     *
     * The configuration parameters include:
     * - `strategyName`: Set to "INFO" to identify this as the information strategy.
     * - `coroutineScope`: Used for managing coroutine-based asynchronous logging operations.
     * - `mutex`: Ensures thread-safe writes to log files.
     * - `ansiColor`: Specifies the ANSI color code `ANSI.CYAN` for coloring information logs in the console.
     *
     * The `infoStrategy` provides a ready-to-use instance for managing logging tasks
     * while maintaining consistent formatting and ensuring safe concurrent operations.
     */
    val infoStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "INFO",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.CYAN,
    )

    /**
     * Specifies the logging strategy employed for handling stack traces in the application.
     * This variable is initialized with an instance of `StackTraceLoggingStrategy`, a specialized
     * implementation of `LoggingStrategy`, to enable formatted and color-coded log messages for
     * stack trace-related information.
     *
     * The `stackTraceStrategy` is configured with:
     * - A `coroutineScope` to support asynchronous logging operations.
     * - A `mutex` to ensure thread-safe access to shared resources, such as log files.
     * - An ANSI color (`ANSI.BOLD_RED`) to highlight log entries visually when printed to the console.
     */
    val stackTraceStrategy: LoggingStrategy = StackTraceLoggingStrategy(coroutineScope, mutex, ANSI.BOLD_RED)

    /**
     * Defines the logging strategy for warning messages.
     *
     * The `warnStrategy` variable is an instance of the `LoggingStrategy` class,
     * which provides mechanisms for logging warning-level messages to both the
     * console and a file. It is configured with specific attributes for handling
     * warnings effectively, including:
     * - A strategy name of "WARN".
     * - A coroutine scope for asynchronous operations.
     * - A mutex for ensuring thread-safe file writes.
     * - The ANSI yellow color for console log appearance.
     */
    val warnStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "WARN",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.YELLOW,
    )
}