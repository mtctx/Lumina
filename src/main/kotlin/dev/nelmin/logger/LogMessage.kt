package dev.nelmin.logger

import dev.nelmin.logger.strategy.LoggingStrategy

data class LogMessage(
    val content: Array<out Any>,
    val logToConsole: Boolean,
    val strategy: LoggingStrategy,
)