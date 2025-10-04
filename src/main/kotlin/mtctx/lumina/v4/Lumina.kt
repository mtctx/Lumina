/*
 *     Lumina: Lumina.kt
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

package mtctx.lumina.v4

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ChannelResult
import mtctx.lumina.v4.strategy.DefaultLoggingStrategies
import mtctx.lumina.v4.strategy.LoggingStrategy
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.system.exitProcess
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

typealias Logger = Lumina
typealias LuminaLogger = Lumina
typealias LL = Lumina
typealias LoggerConfig = LuminaConfig

/**
 * Lumina is the main logging engine of v4.
 *
 * It manages:
 * - Log message dispatch via coroutines and channels.
 * - Log rotation based on configurable retention policies.
 * - Output to both console (ANSI colored) and files.
 * - Structured log messages built via [MessageDSL].
 *
 * Typical usage:
 * ```
 * val lumina = createLumina {
 *     name = "MyApp"
 *     log {
 *         rotation {
 *             enabled = true
 *             duration = 7.days
 *             interval = 1.days
 *         }
 *     }
 * }
 *
 * runBlocking {
 *     lumina.info { +"Application started" }
 *     lumina.error { +"Something went wrong" }
 * }
 * ```
 *
 * Use the `suspend` log functions ([debug], [info], [warn], [error], [fatal]) for normal usage.
 * Use the synchronous variants (e.g. [debugSync], [infoSync]) only when you must guarantee
 * immediate flushing, such as in shutdown hooks or fatal error handlers.
 *
 * Shutdown:
 * - Call [waitForCoroutinesToFinish] to gracefully flush and close resources.
 * - Or use [exitAppGracefully] to exit the process after flushing logs.
 *
 * @constructor creates a logger with the given [config].
 */
@OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
class Lumina(private val config: LuminaConfig) {
    internal var name: String = config.name
    internal var fileSystem: FileSystem = config.fileSystem
    internal var coroutineScope = config.coroutineScope
    internal var log: LuminaConfig.Log = config.log
    internal var format: LuminaConfig.Format = config.format

    private val logChannelJob: Job
    private val logRotationJob: Job?
    private val fileSinks: MutableMap<Path, BufferedSink> = mutableMapOf()
    private val defaultLoggingStrategies: DefaultLoggingStrategies = DefaultLoggingStrategies(config, fileSinks)

    @Volatile
    private var isShuttingDown = false

    init {
        val currentDate = format.dateString()
        if (!fileSystem.exists(log.directory(currentDate))) fileSystem.createDirectories(log.directory(currentDate))

        logChannelJob = startListeningForMessages()
        logRotationJob = if (log.rotation.enabled) rotateLogs(log.rotation.duration) else null
    }

    @OptIn(ExperimentalPathApi::class)
    fun rotateLogs(duration: Duration) = coroutineScope.launch {
        while (!isShuttingDown) {
            val now = Clock.System.now()
            val threshold = now - duration

            fileSystem.list(log.directory(format.dateString(now))).forEach { path ->
                if (!fileSystem.metadata(path).isDirectory) return@forEach

                val dirDate = try {
                    path.name.toInstant(format.date, config.timeZone)
                } catch (_: Exception) {
                    return@forEach
                }

                if (dirDate < threshold) {
                    fileSystem.deleteRecursively(path)
                }
            }

            delay(log.rotation.interval)
        }
    }

    private fun startListeningForMessages() = coroutineScope.launch {
        for (message in log.channel) {
            try {
                message.strategy.log(
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
    fun waitForCoroutinesToFinish(timeout: Duration = 5.seconds) = runBlocking {
        if (isShuttingDown) return@runBlocking
        isShuttingDown = true
        if (!log.channel.isClosedForSend) log.channel.close()

        val drained = withTimeoutOrNull(timeout) {
            logChannelJob.join()
            true
        } ?: run {
            while (!log.channel.isEmpty) {
                val msg = log.channel.tryReceive().getOrNull() ?: break
                msg.strategy.log(msg.timestamp, msg.logToConsole, msg.content)
            }
            false
        }

        if (!drained) {
            println(ANSI.BOLD_YELLOW + "WARNING: Logger shutdown timed out, some logs may be lost." + ANSI.RESET)
        }

        logRotationJob?.cancelAndJoin()
        fileSinks.values.forEach { it.flush(); it.close() }
        if (coroutineScope !== config.coroutineScope) {
            coroutineScope.cancel()
        }
    }

    suspend fun log(message: Message) {
        if (isShuttingDown || log.channel.isClosedForSend) {
            message.strategy.log(message.timestamp, message.logToConsole, message.content)
            return
        }
        try {
            log.channel.send(message)
        } catch (_: Exception) {
            logSync(message)
        }
    }

    suspend fun log(strategy: LoggingStrategy, block: MessageDSL.() -> Unit) =
        log(MessageDSL(strategy).apply(block).build())

    suspend fun log(strategy: LoggingStrategy, logToConsole: Boolean, vararg content: Any) = log(
        Message(
            strategy,
            content.map { it.toString() }.toTypedArray(),
            logToConsole,
            Clock.System.now()
        )
    )

    @OptIn(InternalCoroutinesApi::class)
    fun logSync(message: Message): ChannelResult<Unit> {
        return if (isShuttingDown || log.channel.isClosedForSend) {
            runBlocking {
                try {
                    message.strategy.log(message.timestamp, message.logToConsole, message.content)
                } catch (t: Throwable) {
                    System.err.println("[Logger Sync Flush Error] ${t.message}")
                }
            }
            ChannelResult.success(Unit)
        } else {
            log.channel.trySend(message)
        }
    }

    fun logSync(strategy: LoggingStrategy, block: MessageDSL.() -> Unit) =
        logSync(MessageDSL(strategy).apply(block).build())

    fun logSync(strategy: LoggingStrategy, logToConsole: Boolean, timestamp: Instant, vararg content: Any) = logSync(
        Message(
            strategy,
            content.map { it.toString() }.toTypedArray(),
            logToConsole,
            timestamp
        )
    )

    suspend fun debug(vararg content: Any, logToConsole: Boolean = true) =
        log(
            Message(
                defaultLoggingStrategies.debugStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    suspend fun debug(block: MessageDSL.() -> Unit) = log(defaultLoggingStrategies.debugStrategy, block)

    @UseSynchronousFunctionsWithCaution
    fun debugSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            Message(
                defaultLoggingStrategies.debugStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    @UseSynchronousFunctionsWithCaution
    fun debugSync(block: MessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.debugStrategy, block)

    suspend fun error(vararg content: Any, logToConsole: Boolean = true) =
        log(
            Message(
                defaultLoggingStrategies.errorStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    suspend fun error(block: MessageDSL.() -> Unit) = log(defaultLoggingStrategies.errorStrategy, block)

    @UseSynchronousFunctionsWithCaution
    fun errorSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            Message(
                defaultLoggingStrategies.errorStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    @UseSynchronousFunctionsWithCaution
    fun errorSync(block: MessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.errorStrategy, block)

    suspend fun fatal(vararg content: Any, logToConsole: Boolean = true) =
        log(
            Message(
                defaultLoggingStrategies.fatalStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    suspend fun fatal(block: MessageDSL.() -> Unit) = log(defaultLoggingStrategies.fatalStrategy, block)

    @UseSynchronousFunctionsWithCaution
    fun fatalSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            Message(
                defaultLoggingStrategies.fatalStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    @UseSynchronousFunctionsWithCaution
    fun fatalSync(block: MessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.fatalStrategy, block)

    suspend fun info(vararg content: Any, logToConsole: Boolean = true) =
        log(
            Message(
                defaultLoggingStrategies.infoStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    suspend fun info(block: MessageDSL.() -> Unit) = log(defaultLoggingStrategies.infoStrategy, block)

    @UseSynchronousFunctionsWithCaution
    fun infoSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            Message(
                defaultLoggingStrategies.infoStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    @UseSynchronousFunctionsWithCaution
    fun infoSync(block: MessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.infoStrategy, block)

    suspend fun warn(vararg content: Any, logToConsole: Boolean = true) =
        log(
            Message(
                defaultLoggingStrategies.warnStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    suspend fun warn(block: MessageDSL.() -> Unit) = log(defaultLoggingStrategies.warnStrategy, block)

    @UseSynchronousFunctionsWithCaution
    fun warnSync(vararg content: Any, logToConsole: Boolean = true): ChannelResult<Unit> =
        logSync(
            Message(
                defaultLoggingStrategies.warnStrategy,
                content.map { it.toString() }.toTypedArray(),
                logToConsole,
                Clock.System.now(),
            )
        )

    @UseSynchronousFunctionsWithCaution
    fun warnSync(block: MessageDSL.() -> Unit) = logSync(defaultLoggingStrategies.warnStrategy, block)
}

@RequiresOptIn("This is a non-blocking, semi-synchronous logging function. It queues log messages for later processing, which might not be immediate. Prefer the corresponding 'suspend' function for guaranteed asynchronous processing.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class UseSynchronousFunctionsWithCaution

fun gracefulExit(lumina: Lumina, status: Int = 0, timeoutMillis: Duration = 5.seconds) = runBlocking {
    try {
        lumina.waitForCoroutinesToFinish(timeoutMillis)
    } finally {
        exitProcess(status)
    }
}

fun Lumina.exitAppGracefully(status: Int = 0, timeoutMillis: Duration = 5.seconds) =
    gracefulExit(this, status, timeoutMillis)