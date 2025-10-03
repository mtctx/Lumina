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

package dev.mtctx.library

import dev.mtctx.library.strategy.LoggingStrategy
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.sync.Mutex
import okio.FileSystem
import kotlin.io.path.ExperimentalPathApi
import kotlin.system.exitProcess
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val fs = FileSystem.SYSTEM

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

    suspend fun log(strategy: LoggingStrategy, logToConsole: Boolean, vararg content: Any) = log(
        LogMessage(
            arrayOf(*content),
            logToConsole,
            Clock.System.now(),
            strategy
        )
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

    fun logSync(strategy: LoggingStrategy, logToConsole: Boolean, timestamp: Instant, vararg content: Any) = logSync(
        LogMessage(
            arrayOf(*content),
            logToConsole,
            timestamp,
            strategy
        )
    )

    suspend fun debug(vararg content: Any, logToConsole: Boolean = true) =
        log(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.debugStrategy))

    @UseSynchronousFunctionsWithCaution
    fun debugSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.debugStrategy))

    suspend fun error(vararg content: Any, logToConsole: Boolean = true) =
        log(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.errorStrategy))

    @UseSynchronousFunctionsWithCaution
    fun errorSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.errorStrategy))

    suspend fun fatal(vararg content: Any, logToConsole: Boolean = true) =
        log(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.fatalStrategy))

    @UseSynchronousFunctionsWithCaution
    fun fatalSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.fatalStrategy))

    suspend fun info(vararg content: Any, logToConsole: Boolean = true) =
        log(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.infoStrategy))

    @UseSynchronousFunctionsWithCaution
    fun infoSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.infoStrategy))

    suspend fun warn(vararg content: Any, logToConsole: Boolean = true) =
        log(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.warnStrategy))

    @UseSynchronousFunctionsWithCaution
    fun warnSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(LogMessage(content, logToConsole, Clock.System.now(), defaultLoggingStrategies.warnStrategy))
}

@RequiresOptIn("This is a non-blocking, semi-synchronous logging function. It queues log messages for later processing, which might not be immediate. Prefer the corresponding 'suspend' function for guaranteed asynchronous processing.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class UseSynchronousFunctionsWithCaution

fun gracefulExit(logger: Logger, status: Int = 0, timeoutMillis: Long = 5000) = runBlocking {
    try {
        logger.waitForCoroutinesFinish(timeoutMillis)
    } finally {
        exitProcess(status)
    }
}

fun Logger.exitAppGracefully(status: Int = 0, timeoutMillis: Long = 5000) = gracefulExit(this, status, timeoutMillis)