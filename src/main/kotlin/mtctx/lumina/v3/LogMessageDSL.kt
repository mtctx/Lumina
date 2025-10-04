/*
 *     Lumina: LogMessageDSL.kt
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

import mtctx.lumina.v3.strategy.LoggingStrategy
import kotlin.time.ExperimentalTime

@Deprecated("Use v4 instead", ReplaceWith("mtctx.lumina.v4.MessageDSL"))
class LogMessageDSL(
    private val strategy: LoggingStrategy,
    private var logToConsole: Boolean = true
) {
    private val lines = mutableListOf<String>()

    fun keyValue(block: KeyValueDSL.() -> Unit) = apply {
        val keyValue = KeyValueDSL().apply(block)
        lines.add(keyValue.build())
    }

    fun line(vararg content: String, delimiter: String = " ") = apply {
        require(content.isNotEmpty()) { "Content cannot be empty!" }
        lines.add(content.joinToString(delimiter))
    }

    fun emptyLine() = apply {
        lines.add("")
    }

    fun logToConsole(enabled: Boolean = true) = apply {
        this.logToConsole = enabled
    }

    operator fun String.unaryPlus() = line(this)

    @OptIn(ExperimentalTime::class)
    fun build(): LogMessage {
        return LogMessage(
            content = lines.toTypedArray(),
            logToConsole = logToConsole,
            strategy = strategy
        )
    }

    class KeyValueDSL {
        private var objectName: String? = null
        private var keyPrefix: String = "key"
        private var key: String? = null
        private var valuePrefix: String = "value"
        private var value: Array<out Any>? = null

        fun define(
            objectName: String,
            key: String,
            vararg value: Any,
            keyPrefix: String = "key",
            valuePrefix: String = "value"
        ) {
            this.objectName = objectName
            this.key = key
            this.value = value
        }

        fun objectName(objectName: String) {
            this.objectName = objectName
        }

        fun keyPrefix(keyPrefix: String) {
            this.keyPrefix = keyPrefix
        }

        fun key(key: String) {
            this.key = key
        }

        fun valuePrefix(valuePrefix: String) {
            this.valuePrefix = valuePrefix
        }

        fun value(vararg value: Any) {
            this.value = value
        }

        fun build(): String {
            require(objectName != null && objectName!!.isNotEmpty() && objectName!!.isNotBlank()) { "Object name cannot be null or blank!" }
            require(keyPrefix.isNotEmpty() && keyPrefix.isNotBlank()) { "Key prefix cannot be empty or blank!" }
            require(key != null && key!!.isNotEmpty() && key!!.isNotBlank()) { "Key cannot be null or blank!" }
            require(valuePrefix.isNotEmpty() && valuePrefix.isNotBlank()) { "Value prefix cannot be empty or blank!" }
            require(value != null && value!!.isNotEmpty()) { "Value cannot be null or empty!" }

            return "$objectName($keyPrefix=$key, $valuePrefix=${value!!.joinToString(", ")})"
        }
    }
}