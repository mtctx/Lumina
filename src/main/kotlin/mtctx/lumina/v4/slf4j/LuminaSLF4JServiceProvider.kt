/*
 *     Lumina: LuminaSLF4JServiceProvider.kt
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

package mtctx.lumina.v4.slf4j

import org.slf4j.ILoggerFactory
import org.slf4j.IMarkerFactory
import org.slf4j.spi.MDCAdapter
import org.slf4j.spi.SLF4JServiceProvider

class LuminaSLF4JServiceProvider : SLF4JServiceProvider {
    private val factory = LuminaLoggerFactory()

    override fun getLoggerFactory(): ILoggerFactory = factory
    override fun getRequestedApiVersion(): String = "2.0"

    override fun initialize() {}

    override fun getMarkerFactory(): IMarkerFactory? = null
    override fun getMDCAdapter(): MDCAdapter? = null
}