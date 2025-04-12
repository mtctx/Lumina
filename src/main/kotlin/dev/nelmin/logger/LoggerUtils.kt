package dev.nelmin.logger

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import java.nio.file.Path

/**
 * Utility object providing logging-related functionality including time and date formatting,
 * log directory management, and generation of log file paths for specific strategies.
 */
object LoggerUtils {
    /**
     * Defines a constant `TIME_FORMAT` for formatting `LocalDateTime` values.
     *
     * The format is structured as follows:
     * - Hours with zero-padding.
     * - A colon `:` separator.
     * - Minutes with zero-padding.
     * - A colon `:` separator.
     * - Seconds with zero-padding.
     * - A colon `:` separator.
     * - Milliseconds represented up to 3 fractional digits.
     *
     * This format is primarily intended for creating consistent and easily readable
     * timestamp strings across logging, debugging, and other time-related operations.
     */
    val TIME_FORMAT = LocalDateTime.Format {
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
        char(':')
        second(Padding.ZERO)
        char(':')
        secondFraction(3)
    }
    /**
     * Defines a date formatting pattern for `LocalDateTime` objects.
     *
     * The format follows the pattern `DD.MM.YYYY`, where:
     * - `DD` represents the day of the month with zero padding.
     * - `MM` represents the numeric month with zero padding.
     * - `YYYY` represents the year with zero padding.
     *
     * This variable is used to standardize date representations across the application.
     */
    val DATE_FORMAT = LocalDateTime.Format {
        dayOfMonth(Padding.ZERO)
        char('.')
        monthNumber(Padding.ZERO)
        char('.')
        year(Padding.ZERO)
    }

    /**
     * Formats a given timestamp into a string representation based on the system's time zone and predefined format.
     *
     * @param timestamp The timestamp to format. Defaults to the current instant if not provided.
     * @return A formatted string representation of the timestamp.
     */
    fun getFormattedTime(timestamp: Instant = Clock.System.now()): String =
        timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).format(TIME_FORMAT)

    /**
     * Formats the current date and time into a human-readable string based on the defined date format.
     *
     * @return A string representing the current date and time in the specified format.
     */
    fun getFormattedDate(): String =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).format(DATE_FORMAT)


    /**
     * Represents the path to the logs directory, dynamically constructed based on the user's
     * current working directory and the current formatted date.
     *
     * The directory is located under "logs" in the user's working directory and is appended with
     * a subdirectory named using the string returned by `getFormattedDate()`.
     */
    val logsDir: Path = Path.of(System.getProperty("user.dir"), "logs", getFormattedDate())

    /**
     * Generates the file path for the log file corresponding to a given strategy.
     *
     * @param strategyName The name of the strategy for which the log file path is generated.
     * @return The path of the log file specific to the provided strategy name.
     */
    fun getLogFileForStrategy(strategyName: String): Path {
        return logsDir.resolve("${strategyName.lowercase()}.log")
    }
}