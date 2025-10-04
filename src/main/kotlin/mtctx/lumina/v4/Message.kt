/*
 *     Lumina: Message.kt
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

package mtctx.lumina.v4

import mtctx.lumina.v4.strategy.LoggingStrategy
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Message(
    val strategy: LoggingStrategy,
    val content: Array<String>,
    val logToConsole: Boolean,
    val timestamp: Instant = Clock.System.now(),
) {
    override fun toString(): String = "Log(${content.joinToString(", ")}, $logToConsole, $timestamp, $strategy)"

    override fun equals(other: Any?): Boolean =
        this == other
                && javaClass == other.javaClass
                && other is Message
                && logToConsole == other.logToConsole
                && content.contentEquals(other.content)
                && timestamp == other.timestamp
                && strategy == other.strategy

    override fun hashCode(): Int = arrayOf(content, logToConsole, timestamp, strategy).contentHashCode()
}

/**
 * DSL for constructing structured [Message] instances.
 *
 * Provides a builder-like syntax to compose multi-line logs, key-value pairs,
 * and toggle console output.
 *
 * Example:
 * ```
 * lumina.info {
 *     +"Starting application" // is a new line
 *     +"Version: 1.0.0"
 *     +("Hello, " + "World!") // also a new line, but output is "Hello, World!"
 *     logToConsole = false // disable console output
 *     emptyLine() // insert a blank line
 *     line("Config:") // also a new line
 *     keyValue {
 *         define("Config", "dbUrl", "jdbc://localhost:5432")
 *     }
 * }
 * ```
 *
 * Supports both free-form text and structured key-value fields.
 *
 * - Use [line] or the unary plus operator (`+`) to add a plain line.
 * - Use [keyValue] to add structured data (`Object(key=value)`).
 * - Use [emptyLine] to insert a blank line.
 * - Use [logToConsole] to enable/disable console output for this message.
 *
 * Finally, call [build] (done automatically by [Lumina.log]) to produce a [Message].
 */
class MessageDSL(
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
    fun build(): Message {
        return Message(
            strategy,
            lines.toTypedArray(),
            logToConsole,
            Clock.System.now()
        )
    }

    class KeyValueDSL {
        private var objectName: String? = "KeyValue"
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
            require(keyPrefix.isNotEmpty() && keyPrefix.isNotBlank()) { "Key prefix cannot be empty or blank!" }
            require(key != null && key!!.isNotEmpty() && key!!.isNotBlank()) { "Key cannot be null or blank!" }
            require(valuePrefix.isNotEmpty() && valuePrefix.isNotBlank()) { "Value prefix cannot be empty or blank!" }
            require(value != null && value!!.isNotEmpty()) { "Value cannot be null or empty!" }

            return "$objectName($keyPrefix=$key, $valuePrefix=${value!!.joinToString(", ")})"
        }
    }
}