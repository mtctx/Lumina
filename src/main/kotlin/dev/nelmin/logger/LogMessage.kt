package dev.nelmin.logger

import dev.nelmin.logger.strategy.LoggingStrategy

/**
 * Represents a log message with content and associated metadata for logging behavior.
 *
 * @property content The content of the log message, represented as a variadic array of objects.
 * @property logToConsole A boolean flag indicating whether the log message should be printed to the console.
 * @property strategy The logging strategy defining how and where the log message is handled.
 */
data class LogMessage(
    val content: Any,
    val logToConsole: Boolean,
    val strategy: LoggingStrategy,
)