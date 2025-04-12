package dev.nelmin.logger

import dev.nelmin.logger.strategy.LoggingStrategy
import dev.nelmin.logger.strategy.LoggingStrategyBuilder
import dev.nelmin.logger.strategy.StackTraceLoggingStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import java.nio.file.Files

/**
 * Logger is a utility object for handling logging operations within the application.
 * It supports various logging levels such as debug, error, fatal, info, stacktrace, and warn.
 * The logs can be processed synchronously or queued for asynchronous execution.
 */
object Logger {
    init {
        Files.createDirectories(LoggerUtils.logsDir)
    }

    /**
     * Indicates whether the Logger is in debug mode.
     *
     * This variable is set based on the system property `debugMode`. If the property
     * is present and can be parsed as `true` (case-insensitive), the value of `debug` will be `true`.
     * Otherwise, it defaults to `false`.
     */
    private var debug: Boolean = System.getProperty("debugMode")?.toBoolean() == true // Is the Logger in Debug Mode.

    /**
     * Represents the name of the application or module used within the logging system.
     * This variable may act as an identifier for logging and formatting purposes,
     * or as part of the visual output of logs.
     */
    private var name: String = "Lumina"

    /**
     * A buffered `Channel` for transmitting `LogMessage` objects within the logging system.
     *
     * The `logChannel` serves as a communication medium that allows the system to handle log messages asynchronously
     * by implementing a producer-consumer pattern. The channel is buffered, meaning it can temporarily store a finite
     * number of log messages when no active consumer is present, helping to prevent immediate blocking of producers.
     *
     * A `LogMessage` contains details such as the log content, whether the log should
     * also be output to the console, and the logging strategy being used.
     */
    val logChannel = Channel<LogMessage>(Channel.BUFFERED)

    /**
     * Represents a coroutine job responsible for managing a channel used for logging operations.
     *
     * This variable holds a reference to the active coroutine handling logging tasks or remains null
     * if no logging coroutine is currently running. It is used to control and monitor the lifecycle
     * of the logging mechanism in the application.
     */
    var logChannelCoroutine: Job? = null;

    /**
     * Represents a coroutine scope that is initialized with a `SupervisorJob` as its Job.
     * This provides a structured concurrency context for launching coroutines,
     * allowing them to run independently of each other. Any cancellation or failure
     * of a child coroutine does not affect the parent or sibling coroutines within this scope.
     */
    var coroutineScope = CoroutineScope(SupervisorJob())

    /**
     * A variable representing a Mutex used for thread synchronization to ensure mutual
     * exclusion when accessing shared resources.
     *
     * This mutex is utilized to coordinate concurrent access, preventing race conditions
     * and ensuring consistency of shared data across multiple coroutines or threads.
     */
    var mutex = Mutex()

    /**
     * Sets the name for the Logger.
     *
     * @param name The name to assign to the Logger instance.
     */
    fun setName(name: String) {
        Logger.name = name
    }

    /**
     * Starts listening for log messages sent through the logChannel.
     * Messages are consumed and processed using the provided coroutine scope.
     *
     * @param coroutineScope The scope in which the log listener coroutine is launched.
     * Defaults to the Logger's coroutineScope.
     */
    fun startListeningForLogMessages(coroutineScope: CoroutineScope = Logger.coroutineScope) {
        logChannelCoroutine = coroutineScope.launch {
            for ((content, logToConsole, strategy) in logChannel)
                log(strategy, logToConsole, content)
        }
    }

    /**
     * Stops listening for log messages and performs necessary cleanup operations.
     *
     * This method closes the log channel to terminate further communication and cancels the
     * associated coroutine, ensuring that any ongoing processing for log messages is gracefully
     * stopped.
     */
    fun stopListeningForLogMessages() {
        logChannel.close()
        logChannelCoroutine?.cancel()
    }

    /**
     * Logs a message using the specified logging strategy with customizable format and behavior.
     *
     * This function allows logging of content dynamically, utilizing a specific logging strategy.
     * The log entries can be formatted differently depending on whether debug mode is enabled,
     * and optionally displayed in the console.
     *
     * @param strategy The logging strategy used to log the message.
     * @param logToConsole A flag indicating whether the log message should be printed to the console.
     * @param content The content to be logged, provided as a variable argument list.
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
     * Sends a log message to the `logChannel` based on the provided logging strategy,
     * console logging preference, and content.
     *
     * @param strategy The logging strategy to be used for processing the log message.
     * @param logToConsole Specifies whether the log should also be output to the console.
     *                     Defaults to `true`.
     * @param content The content to be included in the log message. Can accept multiple arguments.
     */
    fun queue(strategy: LoggingStrategy, logToConsole: Boolean = true, vararg content: Any) =
        logChannel.trySend(
            LogMessage(
                content,
                logToConsole,
                strategy
            )
        )

    /**
     * Logs debug-level messages using the provided logging strategy.
     *
     * @param content The content to be logged. Accepts a variable number of arguments of any type.
     * @param logToConsole Determines whether the log should also be output to the console. Defaults to true.
     */
    suspend fun debug(vararg content: Any, logToConsole: Boolean = true) =
        log(DefaultLoggingStrategies.debugStrategy, logToConsole, *content)

    /**
     * Sends a debug log message to the log channel.
     *
     * @param content The log message content, which can include multiple objects or values.
     * @param logToConsole A flag indicating whether the log should also be output to the console. Defaults to true.
     */
    fun queueDebug(vararg content: Any, logToConsole: Boolean = true) =
        logChannel.trySend(
            LogMessage(
                content,
                logToConsole,
                DefaultLoggingStrategies.debugStrategy
            )
        )

    /**
     * Logs an error message using the default error logging strategy.
     *
     * @param content The content to be logged, accepts a variable number of arguments.
     * @param logToConsole Determines whether the log should also be output to the console. Defaults to true.
     */
    suspend fun error(vararg content: Any, logToConsole: Boolean = true) =
        log(DefaultLoggingStrategies.errorStrategy, logToConsole, *content)

    /**
     * Queues a log message with the error logging strategy.
     *
     * @param content Vararg parameter representing the content to be logged.
     * @param logToConsole Specifies whether the log message should also be printed to the console. Default is true.
     */
    fun queueError(vararg content: Any, logToConsole: Boolean = true) =
        queue(DefaultLoggingStrategies.errorStrategy, logToConsole, *content)

    /**
     * Logs a message or a series of messages using the fatal logging strategy.
     *
     * @param content The message or series of messages to be logged.
     * @param logToConsole Indicates whether the log should also be output to the console. Defaults to true.
     */
    suspend fun fatal(vararg content: Any, logToConsole: Boolean = true) =
        log(DefaultLoggingStrategies.fatalStrategy, logToConsole, *content)

    /**
     * Queues a fatal log message with the specified content and logging behavior.
     *
     * @param content The content to be logged as part of the fatal message. Can include multiple arguments.
     * @param logToConsole Indicates whether the message should also be logged to the console. Defaults to true.
     */
    fun queueFatal(vararg content: Any, logToConsole: Boolean = true) =
        queue(DefaultLoggingStrategies.fatalStrategy, logToConsole, *content)

    /**
     * Logs informational messages using the default information logging strategy.
     *
     * @param content The content to be logged, passed as a variable number of arguments.
     * @param logToConsole Determines whether the log should also be output to the console. Defaults to true.
     */
    suspend fun info(vararg content: Any, logToConsole: Boolean = true) =
        log(DefaultLoggingStrategies.infoStrategy, logToConsole, *content)

    /**
     * Logs the provided content using the default info logging strategy.
     *
     * @param content Variable arguments representing the content to be logged.
     * @param logToConsole A boolean flag indicating whether to log the content to the console. Defaults to true.
     */
    fun queueInfo(vararg content: Any, logToConsole: Boolean = true) =
        queue(DefaultLoggingStrategies.infoStrategy, logToConsole, *content)

    /**
     * Logs a stack trace using the default stack trace logging strategy.
     *
     * This function is designed to log the stack trace of a throwable. It uses the default stack
     * trace logging strategy and provides an option to log the stack trace output to the console.
     *
     * @param stackTrace The throwable whose stack trace will be logged.
     * @param logToConsole A flag indicating whether the stack trace should also be printed to the console.
     */
    suspend fun stacktrace(stackTrace: Throwable, logToConsole: Boolean = true) =
        log(DefaultLoggingStrategies.stackTraceStrategy, logToConsole, stackTrace)

    /**
     * Queues a `Throwable` stack trace for logging using the default stack trace logging strategy.
     *
     * @param stackTrace The `Throwable` whose stack trace should be logged.
     * @param logToConsole Specifies whether the stack trace should also be output to the console. Defaults to `true`.
     */
    fun queueStackTrace(stackTrace: Throwable, logToConsole: Boolean = true) =
        queue(DefaultLoggingStrategies.stackTraceStrategy, logToConsole, stackTrace)

    /**
     * Logs a warning message using the default warning logging strategy.
     *
     * This function sends the provided content to be logged as a warning. The behavior can be customized
     * to log the message to the console or not.
     *
     * @param content The content to be logged, provided as a variable number of arguments.
     * @param logToConsole A flag indicating whether the log message should be printed to the console.
     */
    suspend fun warn(vararg content: Any, logToConsole: Boolean = true) =
        log(DefaultLoggingStrategies.warnStrategy, logToConsole, *content)

    /**
     * Sends a warning log message to the `logChannel` using the default error logging strategy.
     *
     * @param content The content to be included in the warning log message. Can accept multiple arguments.
     * @param logToConsole Specifies whether the log should also be output to the console. Defaults to `true`.
     */
    fun queueWarn(vararg content: Any, logToConsole: Boolean = true) =
        queue(DefaultLoggingStrategies.errorStrategy, logToConsole, *content)
}