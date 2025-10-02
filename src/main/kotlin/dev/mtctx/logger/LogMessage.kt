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

package dev.mtctx.logger

import dev.mtctx.logger.strategy.LoggingStrategy
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class LogMessage(
    val content: Array<out Any>,
    val logToConsole: Boolean,
    val timestamp: Instant = Clock.System.now(),
    val strategy: LoggingStrategy,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogMessage

        if (logToConsole != other.logToConsole) return false
        if (!content.contentEquals(other.content)) return false
        if (timestamp != other.timestamp) return false
        if (strategy != other.strategy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = logToConsole.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + strategy.hashCode()
        return result
    }
}