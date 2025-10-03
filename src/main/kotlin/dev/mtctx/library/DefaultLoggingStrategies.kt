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

package dev.mtctx.library

import dev.mtctx.library.strategy.LoggingStrategy
import dev.mtctx.library.strategy.LoggingStrategyBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

class DefaultLoggingStrategies(private val coroutineScope: CoroutineScope, private val mutex: Mutex) {

    val debugStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "DEBUG",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.GREEN,
    )

    val errorStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "ERROR",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.RED,
    )

    val fatalStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "FATAL",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.BOLD_RED,
    )

    val infoStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "INFO",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.CYAN,
    )

    val warnStrategy: LoggingStrategy = LoggingStrategyBuilder(
        strategyName = "WARN",
        coroutineScope = coroutineScope,
        mutex = mutex,
        ansiColor = ANSI.YELLOW,
    )
}