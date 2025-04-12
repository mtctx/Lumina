package dev.nelmin.logger

import dev.nelmin.logger.strategy.LoggingStrategy
import dev.nelmin.logger.strategy.LoggingStrategyBuilder
import dev.nelmin.logger.strategy.StackTraceLoggingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import java.nio.file.Files

/**
 * Object for handling logging functionality with various logging strategies.
 * Provides mechanisms to log, queue, and process messages through different levels
 * including debug, info, warning, error, fatal, and stack trace logging.
 */
object Logger {
    init {
        Files.createDirectories(LoggerUtils.logsDir)
    }

    /**
     * Indicates whether the logger is operating in debug mode.
     *
     * This variable determines if debug-level logging should be enabled.
     * The value is derived from the system property "debugMode" and converted to a boolean.
     * If the "debugMode" property is set to "true", debug mode is enabled.
     */
    private var debug: Boolean = System.getProperty("debugMode")?.toBoolean() == true // Is the Logger in Debug Mode.

    /**
     * Represents the name used internally within the system, initialized with a default value.
     * This variable may serve as an identifier or label for the current instance.
     */
    private var name: String = "Lumina"

    /**
     * A buffered channel used for sending and receiving log messages.
     *
     * This channel facilitates the handling of `LogMessage` objects, which encapsulate information
     * about a log entry. The channel allows concurrent processing of log messages, providing a means
     * for asynchronous and scalable logging operations.
     *
     * The `BUFFERED` capacity ensures that the channel can hold multiple log messages in a queue*/
    val logChannel = Channel<LogMessage>(Channel.BUFFERED)

    /**
     * Represents a `CoroutineScope` initialized with a `SupervisorJob`.
     *
     * This variable provides a context for launching coroutines that are independent of each other.
     * If one child coroutine fails, it does not affect the rest. The `SupervisorJob` prevents
     * exceptions in one coroutine from propagating to sibling coroutines.
     *
     * This `coroutineScope` can be used to manage structured concurrency and handle coroutine*/
    var coroutineScope = CoroutineScope(SupervisorJob())

    /**
     * A `Mutex` instance used to control and synchronize access to shared resources
     * in a concurrent or multi-threaded environment*/
    var mutex = Mutex()

    /**
     * Defines the logging strategy used for debug-level messages within the application.
     *
     * The `debugStrategy` utilizes a `LoggingStrategy` instance configured specifically for handling
     * debug logs. It is initialized with a strategy name of "DEBUG" and employs a coroutine scope
     * and a mutex for managing asynchronous log operations and ensuring thread safety. The ANSI green
     * color is used to distinguish debug logs visually in the console.
     */
    private val debugStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "DEBUG",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.GREEN,
    )

    /**
     * A predefined logging strategy specifically configured for error-level logging.
     *
     * This logging strategy utilizes a red ANSI color for console messages, ensuring clear
     * differentiation of error logs. It operates within the provided coroutine scope, ensuring
     * asynchronous operations do not block the main thread, and uses a mutex to guarantee
     * thread-safe file writes. The strategy name is set to "ERROR" to reflect its specific purpose.
     *
     * The associated `LoggingStrategy` class provides mechanisms for formatting messages for
     * both console output and file logging, while also maintaining thread safety and offering
     * customizable message structures.
     */
    val errorStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "ERROR",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.RED,
    )

    /**
     *
     */
    val fatalStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "FATAL",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.BOLD_RED,
    )

    /**
     * Represents a logging strategy configured for handling log messages with the "INFO" strategy.
     *
     * This variable is an instance of [LoggingStrategy] created using the [LoggingStrategyBuilder].
     * It is associated with the following attributes:
     *
     * - Strategy Name: "*/
    val infoStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "INFO",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.CYAN,
    )

    /**
     * Represents the logging strategy for Stack Trace-based logs within the system.
     *
     * This variable is initialized as an instance of `StackTraceLoggingStrategy`, configured with a coroutine scope,
     * a mutex for thread-safe operations, and a distinct ANSI color to differentiate its log output.
     * The `stackTraceStrategy` is primarily used to capture and log detailed stack trace information
     * for debug or error tracking purposes.
     */
    val stackTraceStrategy: LoggingStrategy = StackTraceLoggingStrategy(coroutineScope, mutex, ANSI.BOLD_RED)

    /**
     * Represents a logging strategy configured for handling warning-level log messages.
     *
     * This strategy uses the `LoggingStrategy` base class with the following properties:
     * - The strategy name is set to "WARN", distinguishing it as the warning-level logger.
     * - It operates within a specified `coroutineScope` for handling asynchronous log operations.
     * - A `mutex` is provided to ensure thread-safe writes to the log file.
     * - The ANSI color code is set to yellow (`ANSI.YELLOW`), customizing the appearance of console logs.
     *
     * The `warnStrategy` is typically used to log events that indicate potential issues which do not
     * immediately impact application functionality but should be reviewed to prevent future problems.
     */
    val warnStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "WARN",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.YELLOW,
    )

    /**
     * Updates the name property of the Logger.
     *
     * @param name The new name to set for the Logger.
     */
    fun setName(name: String) {
        Logger.name = name
    }

    fun startListeningForLogMessages(coroutineScope: CoroutineScope = Logger.coroutineScope) {
        coroutineScope.launch {
            for ((content, logToConsole, strategy) in logChannel)
                log(strategy, logToConsole, content)
        }
    }

    /**
     * Logs a message using the specified logging strategy and optionally outputs it to the console.
     *
     * @param strategy The logging strategy to use for recording the log.
     * @param logToConsole A boolean flag indicating whether the log message should also be printed to the console.
     * @param*/
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
     * Queues a log message to be processed by a specified logging strategy.
     * The message can optionally be logged to the console and may consist of multiple content elements.
     *
     * @param strategy The logging strategy responsible for processing the log message.
     * @param logToConsole Boolean flag to indicate whether the log message should also be printed to the console.*/
    fun queue(strategy: LoggingStrategy, logToConsole: Boolean = true, vararg content: Any) =
        logChannel.trySend(
            LogMessage(
                content,
                logToConsole,
                strategy
            )
        )

    /**
     * Logs debug level messages using the specified strategy. The content is formatted
     * and logged based on the current debugging configuration.
     *
     * @param content The content to be logged as debug information. Accepts a variable number of arguments.
     * @param logToConsole If true, the message will also be logged to the console in addition to the specified logging strategy. Defaults to true.
     */
    suspend fun debug(vararg content: Any, logToConsole: Boolean = true) =
        log(debugStrategy, logToConsole, *content)

    /**
     * Sends a debug message to the logging channel.
     *
     * @param content The content of the message to be logged, which can be any number of objects. These objects are converted to a string and concatenated with a space delimiter
     * .
     * @param logToConsole Indicates whether the message should also be logged to the console. Defaults to true*/
    fun queueDebug(vararg content: Any, logToConsole: Boolean = true) =
        logChannel.trySend(
            LogMessage(
                content,
                logToConsole,
                debugStrategy
            )
        )

    /**
     * Logs an error message using the specified error strategy.
     *
     * @param content The content to be logged.
     * @param logToConsole Whether the log should also be printed to the console. Default is true.
     */
    suspend fun error(vararg content: Any, logToConsole: Boolean = true) =
        log(errorStrategy, logToConsole, *content)

    /**
     * Queues an error message for logging using the specified error*/
    fun queueError(vararg content: Any, logToConsole: Boolean = true) =
        queue(errorStrategy, logToConsole, *content)

    /**
     * Logs a fatal-level message using the specified content and logging strategy.
     *
     * @param content The content to log. Can accept multiple arguments of any type.
     * @param logToConsole Determines whether the log should also be printed to the console. Defaults to true.
     */
    suspend fun fatal(vararg content: Any, logToConsole: Boolean = true) =
        log(fatalStrategy, logToConsole, *content)

    /**
     * Logs a fatal error message to the logging system and optionally prints it to the console.
     *
     * @param content The content to be included in the log message. Multiple variadic elements are supported.
     * @param logToConsole Determines whether the log message should also be printed to the console. Defaults to true.
     */
    fun queueFatal(vararg content: Any, logToConsole: Boolean = true) =
        queue(errorStrategy, logToConsole, *content)

    /**
     * Logs informational messages using the specified logging strategy.
     *
     * @param content The content to be logged. Accepts a variable number of arguments of any type.
     * @param logToConsole Determines whether the content should be logged to the console. Defaults to true.
     */
    suspend fun info(vararg content: Any, logToConsole: Boolean = true) =
        log(infoStrategy, logToConsole, *content)

    /**
     * Queues logging information to be processed with a specified error handling strategy.
     *
     * @param content Variable number of content arguments that will be logged.
     * @param logToConsole Specifies whether the log messages should also be output to the console. Defaults to true.
     */
    fun queueInfo(vararg content: Any, logToConsole: Boolean = true) =
        queue(errorStrategy, logToConsole, *content)

    /**
     * Logs the stack trace information using the provided logging strategy.
     *
     * @param stackTrace The throwable whose stack trace will be logged.
     * @param logToConsole A flag indicating whether to also log the stack trace to the console. Defaults to true.
     */
    suspend fun stacktrace(stackTrace: Throwable, logToConsole: Boolean = true) =
        log(stackTraceStrategy, logToConsole, stackTrace)

    /**
     * Sends*/
    fun queueStackTrace(stackTrace: Throwable, logToConsole: Boolean = true) =
        queue(stackTraceStrategy, logToConsole, stackTrace)

    /**
     * Logs a warning message using the provided logging strategy.
     *
     * @param content The content to be logged as a warning. Can include multiple elements.
     * @param logToConsole Determines whether the warning should also be logged to the console. Default is true.
     */
    suspend fun warn(vararg content: Any, logToConsole: Boolean = true) =
        log(warnStrategy, logToConsole, *content)

    /**
     * Logs a warning message by queuing it to a log channel using the specified error strategy.
     * Optionally logs the message to the console.
     *
     * @param content The content to include in the warning message. Can be one or more objects.
     * @param logToConsole A boolean flag indicating whether the message should also be logged to the console. Defaults to true.
     */
    fun queueWarn(vararg content: Any, logToConsole: Boolean = true) =
        queue(errorStrategy, logToConsole, *content)
}