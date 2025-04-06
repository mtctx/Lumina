package dev.nelmin.logger

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Copyright 2025 © Nelmin
 * I hereby permit anyone to use this Logger when and where they want to, including myself.
 */
object Logger {
    init {
        Files.createDirectories(Path.of(System.getProperty("user.dir") + "/logs"))
    }

    private var debug: Boolean = System.getProperty("debugMode")?.toBoolean() ?: false // Is the Logger in Debug Mode.
    private var name: String = "CandleMC" // Name of the Module, Plugin and so on.

    // Single Thread Executor for all logging operations
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private var debugStrategy: DebugLoggingStrategy = DebugLoggingStrategy(executor, debug)
    private val errorStrategy: LoggingStrategy = LoggingStrategy(
        "error", "ERROR", executor, ANSI.RED
    )
    private val fatalStrategy: LoggingStrategy = LoggingStrategy(
        "error", "FATAL", executor, ANSI.BOLD_RED
    )
    private val infoStrategy: LoggingStrategy = LoggingStrategy(
        "info", "INFO", executor, ANSI.CYAN
    )
    private val stackTraceStrategy: LoggingStrategy = StackTraceLoggingStrategy(executor)
    private val warnStrategy: LoggingStrategy = LoggingStrategy(
        "info", "WARN", executor, ANSI.YELLOW
    )

    fun setName(name: String) {
        Logger.name = name
    }

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

    fun debug(vararg content: Any) {
        log(debugStrategy, *content)
    }

    fun error(vararg content: Any) {
        log(errorStrategy, *content)
    }

    fun fatal(vararg content: Any) {
        log(fatalStrategy, *content)
    }

    fun info(vararg content: Any) {
        log(infoStrategy, *content)
    }

    fun stacktrace(stackTrace: Throwable) {
        log(stackTraceStrategy, stackTrace)
    }

    fun warn(vararg content: Any) {
        log(warnStrategy, *content)
    }
}
