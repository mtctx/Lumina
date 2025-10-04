/*
 *     Lumina: Logger.kt
 *     Copyright (C) 2025 mtctx
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package mtctx.lumina.v3

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.sync.Mutex
import mtctx.lumina.v3.strategy.LoggingStrategy
import okio.FileSystem
import kotlin.io.path.ExperimentalPathApi
import kotlin.system.exitProcess
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Deprecated(
    message = "Use v4 instead",
    replaceWith = ReplaceWith("mtctx.lumina.v4.LuminaConfig::fileSystem"),
    level = DeprecationLevel.WARNING
)
val fs = FileSystem.SYSTEM

@Deprecated(
    message = "Use v4 instead",
    replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina"),
    level = DeprecationLevel.WARNING
)
@OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
class Logger(private val config: LoggerConfig) {
    var name: String
    var coroutineScope: CoroutineScope
    var mutex = Mutex()
    private val defaultLoggingStrategies: DefaultLoggingStrategies
    private val logChannel: Channel<LogMessage>
    private val logChannelJob: Job
    private var shouldLogRotationStop = false
    private val logRotationJob: Job?

    @Volatile
    private var isShuttingDown = false

    init {
        fs.createDirectories(config.logsDirectory)
        name = config.name
        coroutineScope = config.coroutineScope
        defaultLoggingStrategies = DefaultLoggingStrategies(coroutineScope, mutex)
        logChannel = config.logChannel
        logChannelJob = startListeningForMessages()
        logRotationJob = if (config.logRotation.enabled) rotateLogs(config.logRotation.duration) else null
    }

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::rotateLogs"),
        level = DeprecationLevel.WARNING
    )
    @OptIn(ExperimentalPathApi::class)
    fun rotateLogs(duration: Duration) = coroutineScope.launch {
        while (!shouldLogRotationStop) {
            val now = Clock.System.now()
            val threshold = now - duration

            fs.list(config.logsDirectory).forEach { path ->
                if (!fs.metadata(path).isDirectory) return@forEach

                val dirDate = try {
                    LoggerUtils.parseDateFromText(path.name)
                } catch (_: Exception) {
                    return@forEach
                }

                if (dirDate < threshold) {
                    fs.deleteRecursively(path)
                }
            }

            delay(config.logRotation.interval)
        }
    }

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::startListeningForMessages"),
        level = DeprecationLevel.WARNING
    )
    private fun startListeningForMessages() = coroutineScope.launch {
        for (message in logChannel) {
            try {
                message.strategy.log(
                    config,
                    message.timestamp,
                    message.logToConsole,
                    message.content
                )
            } catch (t: Throwable) {
                System.err.println("[$name] Log consumer error: ${t.message}")
            }
        }
    }

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::waitForCoroutinesFinish"),
        level = DeprecationLevel.WARNING
    )
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun waitForCoroutinesFinish(timeoutMillis: Long = 5000) {
        if (isShuttingDown) return

        shouldLogRotationStop = true
        isShuttingDown = true
        if (!logChannel.isClosedForSend) logChannel.close()

        val drained = withTimeoutOrNull(timeoutMillis) {
            logChannelJob.join()
            true
        } ?: run {
            while (!logChannel.isEmpty) {
                val msg = logChannel.tryReceive().getOrNull() ?: break
                msg.strategy.log(config, msg.timestamp, msg.logToConsole, msg.content)
            }
            false
        }

        if (!drained) {
            println(ANSI.BOLD_YELLOW + "WARNING: Logger shutdown timed out, some logs may be lost." + ANSI.RESET)
        }

        logRotationJob?.cancelAndJoin()
        if (coroutineScope !== config.coroutineScope) {
            coroutineScope.cancel()
        }
    }

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::log"),
        level = DeprecationLevel.WARNING
    )
    suspend fun log(logMessage: LogMessage) {
        if (isShuttingDown || logChannel.isClosedForSend) {
            logMessage.strategy.log(config, logMessage.timestamp, logMessage.logToConsole, logMessage.content)
            return
        }
        try {
            logChannel.send(logMessage)
        } catch (_: Exception) {
            logSync(logMessage)
        }
    }

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::log"),
        level = DeprecationLevel.WARNING
    )
    suspend fun log(strategy: LoggingStrategy, block: LogMessageDSL.() -> Unit) =
        log(LogMessageDSL(strategy).apply(block).build())

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::log"),
        level = DeprecationLevel.WARNING
    )
    suspend fun log(strategy: LoggingStrategy, logToConsole: Boolean, vararg content: Any) = log(
        LogMessage(
            content.map { it.toString() }.toTypedArray(),
            logToConsole,
            Clock.System.now(),
            strategy
        )
    )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::logSync"),
        level = DeprecationLevel.WARNING
    )
    @OptIn(InternalCoroutinesApi::class)
    fun logSync(logMessage: LogMessage): ChannelResult<Unit> {
        return if (isShuttingDown || logChannel.isClosedForSend) {
            runBlocking {
                try {
                    logMessage.strategy.log(config, logMessage.timestamp, logMessage.logToConsole, logMessage.content)
                } catch (t: Throwable) {
                    System.err.println("[Logger Sync Flush Error] ${t.message}")
                }
            }
            ChannelResult.success(Unit)
        } else {
            logChannel.trySend(logMessage)
        }
    }

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::logSync"),
        level = DeprecationLevel.WARNING
    )
    fun logSync(strategy: LoggingStrategy, block: LogMessageDSL.() -> Unit) =
        logSync(LogMessageDSL(strategy).apply(block).build())

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::logSync"),
        level = DeprecationLevel.WARNING
    )
    fun logSync(strategy: LoggingStrategy, logToConsole: Boolean, timestamp: Instant, vararg content: Any) = logSync(
        LogMessage(
            content.map { it.toString() }.toTypedArray(),
            logToConsole,
            timestamp,
            strategy
        )
    )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::debug"),
        level = DeprecationLevel.WARNING
    )
    suspend fun debug(vararg content: Any, logToConsole: Boolean = true) =
        log(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.debugStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::debug"),
        level = DeprecationLevel.WARNING
    )
    suspend fun debug(block: LogMessageDSL.() -> Unit) = log(defaultLoggingStrategies.debugStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::debugSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun debugSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.debugStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::debugSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun debugSync(block: LogMessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.debugStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::error"),
        level = DeprecationLevel.WARNING
    )
    suspend fun error(vararg content: Any, logToConsole: Boolean = true) =
        log(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.errorStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::error"),
        level = DeprecationLevel.WARNING
    )
    suspend fun error(block: LogMessageDSL.() -> Unit) = log(defaultLoggingStrategies.errorStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::errorSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun errorSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.errorStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::errorSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun errorSync(block: LogMessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.errorStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::fatal"),
        level = DeprecationLevel.WARNING
    )
    suspend fun fatal(vararg content: Any, logToConsole: Boolean = true) =
        log(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.fatalStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::fatal"),
        level = DeprecationLevel.WARNING
    )
    suspend fun fatal(block: LogMessageDSL.() -> Unit) = log(defaultLoggingStrategies.fatalStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::fatalSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun fatalSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.fatalStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::fatalSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun fatalSync(block: LogMessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.fatalStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::info"),
        level = DeprecationLevel.WARNING
    )
    suspend fun info(vararg content: Any, logToConsole: Boolean = true) =
        log(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.infoStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::info"),
        level = DeprecationLevel.WARNING
    )
    suspend fun info(block: LogMessageDSL.() -> Unit) = log(defaultLoggingStrategies.infoStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::infoSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun infoSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.infoStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::infoSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun infoSync(block: LogMessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.infoStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::warn"),
        level = DeprecationLevel.WARNING
    )
    suspend fun warn(vararg content: Any, logToConsole: Boolean = true) =
        log(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.warnStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::warn"),
        level = DeprecationLevel.WARNING
    )
    suspend fun warn(block: LogMessageDSL.() -> Unit) = log(defaultLoggingStrategies.warnStrategy, block)

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::warnSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun warnSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            LogMessage(
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
                defaultLoggingStrategies.warnStrategy
            )
        )

    @Deprecated(
        message = "Use v4 instead",
        replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::warnSync"),
        level = DeprecationLevel.WARNING
    )
    @UseSynchronousFunctionsWithCaution
    fun warnSync(block: LogMessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.warnStrategy, block)
}

@RequiresOptIn(
    message = "This is a non-blocking, semi-synchronous logging function. " +
            "It queues log messages for later processing, which might not be immediate. " +
            "Prefer the corresponding 'suspend' function for guaranteed asynchronous processing.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@Deprecated(
    message = "Use v4 instead",
    replaceWith = ReplaceWith("mtctx.lumina.v4.UseSynchronousFunctionsWithCaution"),
    level = DeprecationLevel.WARNING
)
annotation class UseSynchronousFunctionsWithCaution

@Deprecated(
    message = "Use v4 instead",
    replaceWith = ReplaceWith("mtctx.lumina.v4::gracefulExit"),
    level = DeprecationLevel.WARNING
)
fun gracefulExit(logger: Logger, status: Int = 0, timeoutMillis: Long = 5000) = runBlocking {
    try {
        logger.waitForCoroutinesFinish(timeoutMillis)
    } finally {
        exitProcess(status)
    }
}

@Deprecated(
    message = "Use v4 instead",
    replaceWith = ReplaceWith("mtctx.lumina.v4.Lumina::exitAppGracefully"),
    level = DeprecationLevel.WARNING
)
fun Logger.exitAppGracefully(status: Int = 0, timeoutMillis: Long = 5000) =
    gracefulExit(this, status, timeoutMillis)
