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
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val fs = FileSystem.SYSTEM

@OptIn(ExperimentalTime::class)
class Logger(private val config: LoggerConfig) {
    var name: String
    var coroutineScope: CoroutineScope
    var mutex = Mutex()
    private val defaultLoggingStrategies: DefaultLoggingStrategies
    private val logChannel: Channel<LogMessage>
    private val logChannelJob: Job
    private var shouldLogRotationStop = false
    private val logRotationJob: Job?

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
            message.strategy.log(
                config,
                message.timestamp,
                message.logToConsole,
                message.content
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun waitForCoroutinesFinish() = runBlocking {
        logChannel.close()
        logChannelJob.join()
        shouldLogRotationStop = true
        logRotationJob?.join()
        coroutineScope.cancel()
        config.coroutineScope.cancel()
    }

    suspend fun log(logMessage: LogMessage) = logChannel.send(logMessage)
    suspend fun log(strategy: LoggingStrategy, logToConsole: Boolean, vararg content: Any) = log(
        LogMessage(
            arrayOf(*content),
            logToConsole,
            Clock.System.now(),
            strategy
        )
    )

    fun logSync(logMessage: LogMessage): ChannelResult<Unit> = logChannel.trySend(logMessage)
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