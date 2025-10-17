/*
 *     Lumina: LuminaSLF4J.kt
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

import mtctx.lumina.v4.Lumina
import mtctx.lumina.v4.LuminaConfig
import mtctx.lumina.v4.UseSynchronousFunctionsWithCaution
import mtctx.lumina.v4.formatMessage
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level

class LuminaSLF4J(private val config: LuminaConfig) : Logger {
    constructor(name: String) : this(LuminaConfig(name = name))

    private val lumina = Lumina(config)

    override fun getName(): String = config.name

    @OptIn(UseSynchronousFunctionsWithCaution::class)
    private fun log(level: Level, msg: String, t: Throwable?) {
        val messageContent = if (t != null) arrayOf(msg, t.stackTraceToString()) else arrayOf(msg)
        when (level) {
            Level.TRACE, Level.DEBUG -> lumina.debugSync(*messageContent)
            Level.INFO -> lumina.infoSync(*messageContent)
            Level.WARN -> lumina.warnSync(*messageContent)
            Level.ERROR -> lumina.errorSync(*messageContent)
        }
    }

    override fun isTraceEnabled(): Boolean = true
    override fun trace(format: String?) = log(Level.TRACE, format ?: "", null)
    override fun trace(format: String?, arg: Any?) = log(Level.TRACE, format?.formatMessage(arg) ?: "", null)
    override fun trace(format: String?, arg1: Any?, arg2: Any?) =
        log(Level.TRACE, format?.formatMessage(arg1, arg2) ?: "", null)

    override fun trace(format: String?, vararg args: Any?) = log(Level.TRACE, format?.formatMessage(*args) ?: "", null)
    override fun trace(format: String?, t: Throwable?) = log(Level.TRACE, format ?: "", t)

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun isTraceEnabled(marker: Marker?): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun trace(marker: Marker?, format: String?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun trace(marker: Marker?, format: String?, arg: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun trace(marker: Marker?, format: String?, vararg args: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun trace(marker: Marker?, format: String?, t: Throwable?) {
    }

    override fun isDebugEnabled(): Boolean = true
    override fun debug(format: String?) = log(Level.DEBUG, format ?: "", null)
    override fun debug(format: String?, arg: Any?) = log(Level.DEBUG, format?.formatMessage(arg) ?: "", null)
    override fun debug(format: String?, arg1: Any?, arg2: Any?) =
        log(Level.DEBUG, format?.formatMessage(arg1, arg2) ?: "", null)

    override fun debug(format: String?, vararg args: Any?) = log(Level.DEBUG, format?.formatMessage(*args) ?: "", null)
    override fun debug(format: String?, t: Throwable?) = log(Level.DEBUG, format ?: "", t)

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun isDebugEnabled(marker: Marker?): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun debug(marker: Marker?, format: String?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun debug(marker: Marker?, format: String?, arg: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun debug(marker: Marker?, format: String?, vararg args: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun debug(marker: Marker?, format: String?, t: Throwable?) {
    }

    override fun isInfoEnabled(): Boolean = true
    override fun info(format: String?) = log(Level.INFO, format ?: "", null)
    override fun info(format: String?, arg: Any?) = log(Level.INFO, format?.formatMessage(arg) ?: "", null)
    override fun info(format: String?, arg1: Any?, arg2: Any?) =
        log(Level.INFO, format?.formatMessage(arg1, arg2) ?: "", null)

    override fun info(format: String?, vararg args: Any?) = log(Level.INFO, format?.formatMessage(*args) ?: "", null)
    override fun info(format: String?, t: Throwable?) = log(Level.INFO, format ?: "", t)

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun isInfoEnabled(marker: Marker?): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun info(marker: Marker?, format: String?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun info(marker: Marker?, format: String?, arg: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun info(marker: Marker?, format: String?, vararg args: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun info(marker: Marker?, format: String?, t: Throwable?) {
    }

    override fun isWarnEnabled(): Boolean = true
    override fun warn(format: String?) = log(Level.WARN, format ?: "", null)
    override fun warn(format: String?, arg: Any?) = log(Level.WARN, format?.formatMessage(arg) ?: "", null)
    override fun warn(format: String?, arg1: Any?, arg2: Any?) =
        log(Level.WARN, format?.formatMessage(arg1, arg2) ?: "", null)

    override fun warn(format: String?, vararg args: Any?) = log(Level.WARN, format?.formatMessage(*args) ?: "", null)
    override fun warn(format: String?, t: Throwable?) = log(Level.WARN, format ?: "", t)

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun isWarnEnabled(marker: Marker?): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun warn(marker: Marker?, format: String?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun warn(marker: Marker?, format: String?, arg: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun warn(marker: Marker?, format: String?, vararg args: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun warn(marker: Marker?, format: String?, t: Throwable?) {
    }

    override fun isErrorEnabled(): Boolean = true
    override fun error(format: String?) = log(Level.ERROR, format ?: "", null)
    override fun error(format: String?, arg: Any?) = log(Level.ERROR, format?.formatMessage(arg) ?: "", null)
    override fun error(format: String?, arg1: Any?, arg2: Any?) =
        log(Level.ERROR, format?.formatMessage(arg1, arg2) ?: "", null)

    override fun error(format: String?, vararg args: Any?) = log(Level.ERROR, format?.formatMessage(*args) ?: "", null)
    override fun error(format: String?, t: Throwable?) = log(Level.ERROR, format ?: "", t)

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun isErrorEnabled(marker: Marker?): Boolean = false

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun error(marker: Marker?, format: String?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun error(marker: Marker?, format: String?, arg: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun error(marker: Marker?, format: String?, vararg args: Any?) {
    }

    @Suppress("UNUSED_PARAMETER")
    @Deprecated("Marker functionality is not supported by Lumina", level = DeprecationLevel.ERROR)
    override fun error(marker: Marker?, format: String?, t: Throwable?) {
    }
}
