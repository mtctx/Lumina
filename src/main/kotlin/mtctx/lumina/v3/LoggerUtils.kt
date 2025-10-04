/*
 *     Lumina: LoggerUtils.kt
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

import kotlinx.datetime.*
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import okio.Path
import okio.Path.Companion.toPath
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.Utils", "mtctx.lumina.v4.LuminaConfig"))
@OptIn(ExperimentalTime::class)
object LoggerUtils {
    val TIME_FORMAT = LocalDateTime.Format {
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
        char(':')
        second(Padding.ZERO)
        char(':')
        secondFraction(3)
    }

    val DATE_FORMAT = LocalDateTime.Format {
        day(Padding.ZERO)
        char('.')
        monthNumber(Padding.ZERO)
        char('.')
        year(Padding.ZERO)
    }

    fun getFormattedTime(timestamp: Instant = Clock.System.now()): String =
        timestamp.toLocalDateTime(TimeZone.UTC).format(TIME_FORMAT)

    fun getFormattedDate(date: Instant = Clock.System.now()): String =
        date.toLocalDateTime(TimeZone.UTC).format(DATE_FORMAT)

    val logsDir: Path by lazy {
        val path = "./logs/${getFormattedDate()}".toPath()
        if (!fs.exists(path)) fs.createDirectories(path)
        fs.canonicalize(path)
    }

    fun getLogFileForStrategy(path: Path, strategyName: String): Path {
        if (!fs.exists(path)) fs.createDirectories(path)
        return path.resolve("${strategyName.lowercase()}.log")
    }

    fun parseDateFromText(text: String): Instant = DATE_FORMAT.parse(text).toInstant(TimeZone.UTC)
}