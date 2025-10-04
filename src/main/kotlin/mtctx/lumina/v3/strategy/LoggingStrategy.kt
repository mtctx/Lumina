/*
 *     Lumina: LoggingStrategy.kt
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

package mtctx.lumina.v3.strategy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mtctx.lumina.v3.ANSI
import mtctx.lumina.v3.ANSI.translateToANSI
import mtctx.lumina.v3.LoggerConfig
import mtctx.lumina.v3.LoggerUtils
import mtctx.lumina.v3.fs
import okio.Path
import java.io.IOException
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.LoggingStrategy"))
open class LoggingStrategy(
    private val strategyName: String, private val coroutineScope: CoroutineScope, private val mutex: Mutex,
    private val ansiColor: String
) {
    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.LoggingStrategy::log"))
    open suspend fun log(
        config: LoggerConfig,
        timestamp: Instant,
        logToConsole: Boolean,
        content: Array<out Any>
    ) {
        val formattedTimestamp = LoggerUtils.getFormattedTime(timestamp)
        val message = generateMessage(config, formattedTimestamp, content)

        try {
            if (logToConsole) println(
                message.translateToANSI()
            )
            writeToFile(LoggerUtils.getLogFileForStrategy(config.logsDirectory, strategyName), message)
        } catch (e: IOException) {
            System.err.println("Log write error: " + e.message)
        }
    }

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.LoggingStrategy::generateMessage"))
    open fun generateMessage(
        config: LoggerConfig, formattedTimestamp: String,
        content: Array<out Any>
    ): String = config.format(
        formattedTimestamp,
        "$ansiColor$strategyName${ANSI.RESET}",
        config.name,
        content.map { it.toString() }.toTypedArray()
    )

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.LoggingStrategy::log"))
    @Throws(IOException::class)
    open suspend fun writeToFile(logFilePath: Path, message: String): Unit =
        mutex.withLock {
            withContext(Dispatchers.IO) {
                if (!fs.exists(logFilePath.parent!!)) fs.createDirectories(logFilePath.parent!!)

                fs.write(logFilePath) {
                    writeUtf8(message)
                    writeUtf8("\n")
                }
            }
        }
}