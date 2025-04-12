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
    val content: Array<out Any>,
    val logToConsole: Boolean,
    val strategy: LoggingStrategy,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogMessage

        if (logToConsole != other.logToConsole) return false
        if (!content.contentEquals(other.content)) return false
        if (strategy != other.strategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = logToConsole.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + strategy.hashCode()
        return result
    }
}