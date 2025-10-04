/*
 *     Lumina: LogMessage.kt
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

import mtctx.lumina.v3.strategy.LoggingStrategy
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.Message"))
@OptIn(ExperimentalTime::class)
data class LogMessage(
    val content: Array<String>,
    val logToConsole: Boolean,
    val timestamp: Instant = Clock.System.now(),
    val strategy: LoggingStrategy,
) {
    override fun toString(): String = "Log(${content.joinToString(", ")}, $logToConsole, $timestamp, $strategy)"

    override fun equals(other: Any?): Boolean =
        this == other
                && javaClass == other.javaClass
                && other is LogMessage
                && logToConsole == other.logToConsole
                && content.contentEquals(other.content)
                && timestamp == other.timestamp
                && strategy == other.strategy

    override fun hashCode(): Int = arrayOf(content, logToConsole, timestamp, strategy).contentHashCode()
}