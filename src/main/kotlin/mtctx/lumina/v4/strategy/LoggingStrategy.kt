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

package mtctx.lumina.v4.strategy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mtctx.lumina.v4.ANSI
import mtctx.lumina.v4.ANSI.stripANSI
import mtctx.lumina.v4.ANSI.translateToANSI
import mtctx.lumina.v4.LuminaConfig
import okio.BufferedSink
import okio.Path
import okio.buffer
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
open class LoggingStrategy(
    private val strategyName: String,
    private val ansiColor: String,
    private val config: LuminaConfig,
    private val fileSinks: MutableMap<Path, BufferedSink>
) {
    private var currentDate: String = config.format.dateString(Clock.System.now())
    private var currentPath: Path = config.format.file(config.log.directory(currentDate), strategyName.lowercase())
    private var bufferedSink: BufferedSink = openSink(currentPath)

    private fun openSink(path: Path): BufferedSink {
        return fileSinks.getOrPut(path) {
            if (!config.fileSystem.exists(path.parent!!)) {
                config.fileSystem.createDirectories(path.parent!!)
            }
            config.fileSystem.appendingSink(path).buffer()
        }
    }

    private fun rotateIfNeeded(dateString: String) {
        if (currentDate == dateString) return

        bufferedSink.flush()
        bufferedSink.close()
        fileSinks.remove(currentPath)

        currentDate = dateString
        currentPath = config.format.file(config.log.directory(dateString), strategyName.lowercase())
        bufferedSink = openSink(currentPath)
    }

    open suspend fun log(
        timestamp: Instant,
        logToConsole: Boolean,
        content: Array<String>
    ) {
        val formattedTime = config.format.timeString(timestamp)
        val formattedDate = config.format.dateString(timestamp)
        val message = generateMessage(formattedTime, content)

        config.fileMutex.withLock {
            rotateIfNeeded(formattedDate)

            try {
                if (logToConsole) println(message.translateToANSI())
                withContext(Dispatchers.IO) {
                    bufferedSink.writeUtf8(message.stripANSI())
                    bufferedSink.writeUtf8("\n")
                    bufferedSink.flush()
                }
            } catch (e: IOException) {
                System.err.println("Log write error: " + e.message)
            }
        }
    }

    open fun generateMessage(
        formattedTimestamp: String,
        content: Array<String>
    ): String = config.format.message(
        formattedTimestamp,
        "$ansiColor$strategyName${ANSI.RESET}",
        config.name,
        content
    )
}