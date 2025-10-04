/*
 *     Lumina: LuminaConfig.kt
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Configuration container for [Lumina].
 *
 * Controls log directories, formatting, channels, rotation policies, and coroutine scope.
 * Immutable once built; use [LuminaDSL] for easy construction.
 *
 * @property name The logical name of this logger, included in log output.
 * @property log Logging behavior settings (directory layout, rotation, channel).
 * @property format Formatting rules for messages, timestamps, and log file naming.
 * @property timeZone Time zone used for formatting dates/times in logs.
 * @property fileSystem Filesystem abstraction (defaults to [okio.FileSystem.SYSTEM]).
 * @property coroutineScope Coroutine scope for log workers and rotation jobs.
 * @property fileMutex Mutex ensuring file writes are thread-safe across strategies.
 *
 * Example:
 * ```
 * val config = LuminaConfig(
 *     name = "ServiceA",
 *     log = LuminaConfig.Log(
 *         directory = { currentDate -> "./logs/$currentDate/".toPath() },
 *         rotation = LuminaConfig.Log.RotationConfig(true, 30.days, 1.days)
 *     )
 * )
 * ```
 */
data class LuminaConfig(
    val name: String = "Lumina",
    val log: Log = Log(),
    val format: Format = Format(),
    val timeZone: TimeZone = TimeZone.UTC,
    val fileSystem: FileSystem = FileSystem.SYSTEM,
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob()),
    val fileMutex: Mutex = Mutex(),
) {
    data class Log(
        val directory: (currentDate: String) -> Path = { currentDate -> "./logs/$currentDate/".toPath() },
        val rotation: RotationConfig = RotationConfig(true, 30.days, 1.days),
        val channelSize: Int = Channel.UNLIMITED,
        val channel: Channel<Message> = Channel(channelSize)
    ) {
        data class RotationConfig(val enabled: Boolean, val duration: Duration, val interval: Duration)
    }

    @OptIn(ExperimentalTime::class)
    data class Format(
        val message: (timestamp: String, coloredStrategyName: String, loggerName: String, content: Array<String>) -> String = { timestamp, coloredStrategyName, loggerName, content ->
            val template = "[$timestamp] - $coloredStrategyName - $loggerName - "
            content.joinToString("\n") { line -> "$template${line.replace("\n", "\n$template")}" }
        },
        val file: (directory: Path, strategyName: String) -> Path = { directory, strategyName ->
            directory.resolve("$strategyName.log")
        },
        val time: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
            hour(Padding.ZERO)
            char(':')
            minute(Padding.ZERO)
            char(':')
            second(Padding.ZERO)
            char(':')
            secondFraction(3)
        },
        val date: DateTimeFormat<LocalDateTime> = LocalDateTime.Format {
            day(Padding.ZERO)
            char('.')
            monthNumber(Padding.ZERO)
            char('.')
            year(Padding.ZERO)
        },
    ) {
        fun formatted(instant: Instant = Clock.System.now(), format: DateTimeFormat<LocalDateTime>): String =
            instant.toLocalDateTime(TimeZone.UTC).format(format)

        fun timeString(instant: Instant = Clock.System.now(), format: DateTimeFormat<LocalDateTime> = time): String =
            formatted(instant, format)

        fun dateString(instant: Instant = Clock.System.now(), format: DateTimeFormat<LocalDateTime> = date): String =
            formatted(instant, format)
    }
}