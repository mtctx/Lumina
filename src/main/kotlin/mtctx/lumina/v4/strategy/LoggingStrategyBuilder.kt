/*
 *     Lumina: LoggingStrategyBuilder.kt
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

import mtctx.lumina.v4.LuminaConfig
import okio.BufferedSink
import okio.Path

class LoggingStrategyBuilder(
    strategyName: String,
    ansiColor: String,
    config: LuminaConfig,
    fileSinks: MutableMap<Path, BufferedSink>
) : LoggingStrategy(strategyName, ansiColor, config, fileSinks)