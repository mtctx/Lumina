/*
 *     Lumina: LoggerDSL.kt
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import okio.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.LuminaDSL"))
class LoggerDSL {
    var name: String = "Lumina"
    var logsDirectory: Path = LoggerUtils.logsDir
    private var _coroutineScope: CoroutineScope? = null
    var coroutineScope: CoroutineScope
        get() = _coroutineScope ?: CoroutineScope(SupervisorJob()).also { _coroutineScope = it }
        set(value) {
            _coroutineScope?.cancel()
            _coroutineScope = value
        }
    var format: (timestamp: String, strategyName: String, loggerName: String, content: Array<out Any>) -> String =
        { timestamp, strategyName, loggerName, content ->
            "[$timestamp] - $strategyName - $loggerName - ${content.joinToString { it.toString() }}"
        }
    var logChannelSize: Int = Channel.UNLIMITED
    private var _logChannel: Channel<LogMessage>? = null
    var logChannel: Channel<LogMessage>
        get() = _logChannel ?: Channel<LogMessage>(logChannelSize).also { _logChannel = it }
        set(value) {
            _logChannel = value
        }
    private var logRotationConfig: LogRotation.Config = LogRotation.Config(true, 30.days, 1.days)

    fun logRotation(block: LogRotation.() -> Unit) {
        logRotationConfig = LogRotation().apply(block).build()
    }

    fun build(): Logger {
        require(name.isNotBlank()) { "Logger name cannot be blank." }
        require(logsDirectory.toFile().isDirectory) { "Logs directory must be a directory." }
        require(logChannelSize > 0) { "Log channel size must be greater than 0." }
        require(format.invoke("", "", "", emptyArray()).isNotBlank()) { "Format must not be blank." }

        return Logger(
            LoggerConfig(
                name,
                logsDirectory,
                coroutineScope,
                format,
                logChannelSize,
                logChannel,
                logRotationConfig
            )
        )
    }

    class LogRotation {
        var enabled: Boolean = true
        var duration: Duration = 30.days
        var interval: Duration = 1.days

        fun build(): Config {
            require(duration.isPositive()) { "Log rotation duration must be greater than 0." }
            require(duration.isFinite()) { "Log rotation duration must be finite." }
            require(duration.inWholeDays > 0) { "Log rotation duration must be greater than 0 days." }

            return Config(enabled, duration, interval)
        }

        data class Config(val enabled: Boolean, val duration: Duration, val interval: Duration)
    }
}

fun createLogger(block: LoggerDSL.() -> Unit): Logger = LoggerDSL().apply(block).build()
fun createLogger() = Logger(LoggerConfig())
