/*
 *     Lumina: LuminaDSL.kt
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import okio.FileSystem
import okio.Path
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

class LuminaDSL {
    var name: String = "Lumina"

    private var _coroutineScope: CoroutineScope? = null
    var coroutineScope: CoroutineScope
        get() = _coroutineScope ?: CoroutineScope(SupervisorJob()).also { _coroutineScope = it }
        set(value) {
            _coroutineScope?.cancel()
            _coroutineScope = value
        }

    var timeZone: TimeZone = TimeZone.UTC
    var fileSystem: FileSystem = FileSystem.SYSTEM
    var fileMutex: Mutex = Mutex()

    private var _logConfig: LuminaConfig.Log = LuminaConfig.Log()
    fun log(block: LogDSL.() -> Unit) {
        _logConfig = LogDSL().apply(block).build()
    }

    private var _formatConfig: LuminaConfig.Format = LuminaConfig.Format()
    fun format(block: FormatDSL.() -> Unit) {
        _formatConfig = FormatDSL().apply(block).build()
    }

    fun build(): LuminaConfig {
        require(name.isNotBlank()) { "Logger name cannot be blank." }
        require(_logConfig.channelSize > 0) { "Log channel size must be greater than 0." }

        return LuminaConfig(
            name = name,
            log = _logConfig,
            format = _formatConfig,
            timeZone = timeZone,
            fileSystem = fileSystem,
            coroutineScope = coroutineScope,
            fileMutex = fileMutex
        )
    }

    class LogDSL {
        var directory: (String) -> Path = LuminaConfig.Log().directory
        private var rotation: LuminaConfig.Log.RotationConfig = LuminaConfig.Log.RotationConfig(true, 30.days, 1.days)
        var channelSize: Int = Channel.UNLIMITED
        private var _channel: Channel<Message> = Channel(channelSize)

        fun rotation(block: RotationDSL.() -> Unit) {
            rotation = RotationDSL().apply(block).build()
        }

        fun channel(value: Channel<Message>) {
            _channel = value
        }

        fun build(): LuminaConfig.Log =
            LuminaConfig.Log(directory, rotation, channelSize, _channel)

        class RotationDSL {
            var enabled: Boolean = true
            var duration: Duration = 30.days
            var interval: Duration = 1.days

            fun build(): LuminaConfig.Log.RotationConfig {
                require(duration.isPositive()) { "Log rotation duration must be greater than 0." }
                return LuminaConfig.Log.RotationConfig(enabled, duration, interval)
            }
        }
    }

    class FormatDSL {
        var message: (String, String, String, Array<String>) -> String = LuminaConfig.Format().message
        var file: (Path, String) -> Path = LuminaConfig.Format().file
        var time: DateTimeFormat<LocalDateTime> = LuminaConfig.Format().time
        var date: DateTimeFormat<LocalDateTime> = LuminaConfig.Format().date

        fun build(): LuminaConfig.Format =
            LuminaConfig.Format(message, file, time, date)
    }
}

fun createLumina(block: LuminaDSL.() -> Unit) = Lumina(LuminaDSL().apply(block).build())
fun createLumina() = Lumina(LuminaConfig())

fun createLogger(block: LuminaDSL.() -> Unit) = createLumina(block)
fun createLogger() = createLumina()