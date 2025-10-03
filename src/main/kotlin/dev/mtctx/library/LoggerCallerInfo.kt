/*
 *     Lumina: LoggerCallerInfo.kt
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

data class LoggerCallerInfo(
    val className: String,
    val lineNumber: Int,
)

@JvmOverloads
fun getCallerInfo(skipDepth: Int = 2): LoggerCallerInfo {
    val element = Throwable().stackTrace[skipDepth]
    return LoggerCallerInfo(element.className, element.lineNumber)
}