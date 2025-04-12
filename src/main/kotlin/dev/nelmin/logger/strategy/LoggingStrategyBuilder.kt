package dev.nelmin.logger.strategy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

/**
 * Builder class for constructing and customizing specific logging strategies.
 * Inherits from the `LoggingStrategy` base class, leveraging its asynchronous
 * logging capabilities, log message formatting, and thread-safe file operations.
 *
 * This class allows users to create a logging strategy with a defined name, using
 * dependency-injected parameters such as a coroutine scope for managing asynchronous
 * tasks, a mutex for synchronization, and an ANSI color for console logs.
 *
 * @constructor Initializes a `LoggingStrategyBuilder` instance with the following parameters:
 * @param strategyName The unique name or identifier of the logging strategy.
 * @param coroutineScope The coroutine scope within which asynchronous logging tasks will run.
 * @param mutex The mutex object used to ensure thread-safe write operations for file-based logging.
 * @param ansiColor The ANSI color code to be applied to console log messages.
 */
class LoggingStrategyBuilder(
    private val strategyName: String,
    private val coroutineScope: CoroutineScope,
    private val mutex: Mutex,
    private val ansiColor: String,
) : LoggingStrategy(strategyName, coroutineScope, mutex, ansiColor)