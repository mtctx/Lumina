/*
 *     Lumina: ANSI.kt
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

object ANSI {
    const val BLACK: String = "\u001B[0;30m"
    const val RED: String = "\u001B[0;31m"
    const val GREEN: String = "\u001B[0;32m"
    const val YELLOW: String = "\u001B[0;33m"
    const val BLUE: String = "\u001B[0;34m"
    const val PURPLE: String = "\u001B[0;35m"
    const val CYAN: String = "\u001B[0;36m"
    const val WHITE: String = "\u001B[0;37m"

    const val BOLD_BLACK: String = "\u001B[1;30m"
    const val BOLD_RED: String = "\u001B[1;31m"
    const val BOLD_GREEN: String = "\u001B[1;32m"
    const val BOLD_YELLOW: String = "\u001B[1;33m"
    const val BOLD_BLUE: String = "\u001B[1;34m"
    const val BOLD_PURPLE: String = "\u001B[1;35m"
    const val BOLD_CYAN: String = "\u001B[1;36m"
    const val BOLD_WHITE: String = "\u001B[1;37m"

    const val UNDERLINE_BLACK: String = "\u001B[4;30m"
    const val UNDERLINE_RED: String = "\u001B[4;31m"
    const val UNDERLINE_GREEN: String = "\u001B[4;32m"
    const val UNDERLINE_YELLOW: String = "\u001B[4;33m"
    const val UNDERLINE_BLUE: String = "\u001B[4;34m"
    const val UNDERLINE_PURPLE: String = "\u001B[4;35m"
    const val UNDERLINE_CYAN: String = "\u001B[4;36m"
    const val UNDERLINE_WHITE: String = "\u001B[4;37m"

    const val BACKGROUND_BLACK: String = "\u001B[40m"
    const val BACKGROUND_RED: String = "\u001B[41m"
    const val BACKGROUND_GREEN: String = "\u001B[42m"
    const val BACKGROUND_YELLOW: String = "\u001B[43m"
    const val BACKGROUND_BLUE: String = "\u001B[44m"
    const val BACKGROUND_PURPLE: String = "\u001B[45m"
    const val BACKGROUND_CYAN: String = "\u001B[46m"
    const val BACKGROUND_WHITE: String = "\u001B[47m"

    const val HIGH_INTENSITY_BLACK: String = "\u001B[0;90m"
    const val HIGH_INTENSITY_RED: String = "\u001B[0;91m"
    const val HIGH_INTENSITY_GREEN: String = "\u001B[0;92m"
    const val HIGH_INTENSITY_YELLOW: String = "\u001B[0;93m"
    const val HIGH_INTENSITY_BLUE: String = "\u001B[0;94m"
    const val HIGH_INTENSITY_PURPLE: String = "\u001B[0;95m"
    const val HIGH_INTENSITY_CYAN: String = "\u001B[0;96m"
    const val HIGH_INTENSITY_WHITE: String = "\u001B[0;97m"

    const val BOLD_HIGH_INTENSITY_BLACK: String = "\u001B[1;90m"
    const val BOLD_HIGH_INTENSITY_RED: String = "\u001B[1;91m"
    const val BOLD_HIGH_INTENSITY_GREEN: String = "\u001B[1;92m"
    const val BOLD_HIGH_INTENSITY_YELLOW: String = "\u001B[1;93m"
    const val BOLD_HIGH_INTENSITY_BLUE: String = "\u001B[1;94m"
    const val BOLD_HIGH_INTENSITY_PURPLE: String = "\u001B[1;95m"
    const val BOLD_HIGH_INTENSITY_CYAN: String = "\u001B[1;96m"
    const val BOLD_HIGH_INTENSITY_WHITE: String = "\u001B[1;97m"

    const val BACKGROUND_HIGH_INTENSITY_BLACK: String = "\u001B[0;100m"
    const val BACKGROUND_HIGH_INTENSITY_RED: String = "\u001B[0;101m"
    const val BACKGROUND_HIGH_INTENSITY_GREEN: String = "\u001B[0;102m"
    const val BACKGROUND_HIGH_INTENSITY_YELLOW: String = "\u001B[0;103m"
    const val BACKGROUND_HIGH_INTENSITY_BLUE: String = "\u001B[0;104m"
    const val BACKGROUND_HIGH_INTENSITY_PURPLE: String = "\u001B[0;105m"
    const val BACKGROUND_HIGH_INTENSITY_CYAN: String = "\u001B[0;106m"
    const val BACKGROUND_HIGH_INTENSITY_WHITE: String = "\u001B[0;107m"

    const val RESET: String = "\u001B[0m"

    const val BOLD: String = "\u001B[1m"
    const val ITALIC: String = "\u001B[3m"
    const val UNDERLINE: String = "\u001B[4m"
    const val STRIKETHROUGH: String = "\u001B[9m"

    private fun getANSICode(colorChar: Char): String? = when (colorChar) {
        '0' -> BLACK
        '1' -> BLUE
        '2' -> GREEN
        '3' -> CYAN
        '4' -> RED
        '5' -> PURPLE
        '6' -> YELLOW
        '7' -> WHITE
        '8' -> HIGH_INTENSITY_BLACK
        '9' -> HIGH_INTENSITY_BLUE
        'a' -> HIGH_INTENSITY_GREEN
        'b' -> HIGH_INTENSITY_CYAN
        'c' -> HIGH_INTENSITY_RED
        'd' -> HIGH_INTENSITY_PURPLE
        'e' -> HIGH_INTENSITY_YELLOW
        'f' -> HIGH_INTENSITY_WHITE
        'g' -> YELLOW
        'r' -> RESET
        else -> null
    }

    fun String.translateToANSI(): String {
        val char = '&'
        if (char !in this && '\\' !in this) return this
        val result = StringBuilder(this.length + 32)
        var i = 0
        var lastPos = 0
        val length = this.length

        while (i < length) {
            if (this[i] == '\\' && i + 1 < length && this[i + 1] == char) {
                if (lastPos < i) {
                    result.append(this, lastPos, i)
                }
                result.append(char)
                i += 2
                lastPos = i
                continue
            }

            if (this[i] == char && i + 1 < length) {
                val nextChar = this[i + 1]
                val colorChar = if (nextChar in 'A'..'Z') (nextChar.code + 32).toChar() else nextChar
                val ansiCode = getANSICode(colorChar)

                if (ansiCode != null) {
                    if (lastPos < i) {
                        result.append(this, lastPos, i)
                    }
                    result.append(ansiCode)
                    i += 2
                    lastPos = i
                    continue
                }
            }

            i++
        }

        if (lastPos < length) {
            result.append(this, lastPos, length)
        }

        return result.toString()
    }

    fun String.stripANSI(): String {
        return this.replace(Regex("\u001B\\[[0-9;]*m"), "")
    }
}