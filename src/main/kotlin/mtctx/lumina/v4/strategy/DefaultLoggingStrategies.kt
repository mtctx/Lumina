/*
 *     Lumina: DefaultLoggingStrategies.kt
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

import mtctx.lumina.v4.ANSI
import mtctx.lumina.v4.LuminaConfig
import okio.BufferedSink
import okio.Path

class DefaultLoggingStrategies(
    config: LuminaConfig,
    fileSinks: MutableMap<Path, BufferedSink>
) {

    val debugStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "DEBUG",
        ansiColor = ANSI.GREEN,
        config = config,
        fileSinks = fileSinks
    )

    val errorStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "ERROR",
        ansiColor = ANSI.RED,
        config = config,
        fileSinks = fileSinks
    )

    val fatalStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "FATAL",
        ansiColor = ANSI.BOLD_RED,
        config = config,
        fileSinks = fileSinks
    )

    val infoStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "INFO",
        ansiColor = ANSI.CYAN,
        config = config,
        fileSinks = fileSinks
    )

    val warnStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "WARN",
        ansiColor = ANSI.YELLOW,
        config = config,
        fileSinks = fileSinks
    )
}