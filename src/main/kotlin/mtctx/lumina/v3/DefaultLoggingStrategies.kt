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

package mtctx.lumina.v3

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import mtctx.lumina.v3.strategy.LoggingStrategy
import mtctx.lumina.v3.strategy.LoggingStrategyBuilder

@Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.DefaultLoggingStrategies"))
class DefaultLoggingStrategies(private val coroutineScope: CoroutineScope, private val mutex: Mutex) {

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.DefaultLoggingStrategies::debugStrategy"))
    val debugStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "DEBUG",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.GREEN,
    )

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.DefaultLoggingStrategies::errorStrategy"))
    val errorStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "ERROR",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.RED,
    )

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.DefaultLoggingStrategies::fatalStrategy"))
    val fatalStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "FATAL",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.BOLD_RED,
    )

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.DefaultLoggingStrategies::infoStrategy"))
    val infoStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "INFO",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.CYAN,
    )

    @Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.strategy.DefaultLoggingStrategies::warnStrategy"))
    val warnStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "WARN",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.YELLOW,
    )
}