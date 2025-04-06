package dev.nelmin.logger

import java.io.IOException
import java.util.concurrent.ExecutorService

/**
 * DebugLoggingStrategy is a specialized logging strategy that supports debug-level logging.
 * It provides functionality to log messages both to the console and to a file, with respect to
 * debug mode toggling. Messages are formatted based on the specified logging format and additional content.
 *
 * @constructor Initializes the strategy with the given executor and debug mode status.
 *
 * @param executor The ExecutorService used to handle asynchronous logging tasks.
 * @param inDebugMode A flag to enable or disable debug mode logging. If set to true, logs will be displayed in the console.
 */
class DebugLoggingStrategy(private val executor: ExecutorService, private var inDebugMode: Boolean) : LoggingStrategy(
    "debug", "DEBUG", executor, ANSI.GREEN
) {
    /**
     * Logs a message with the provided format and content. The message is processed and output to both
     * the console (if in debug mode) and a log file, along with additional context such as caller information
     * and a timestamp.
     *
     * @param name The name or identifier of the log entry source.
     * @param format The format to use for constructing the log message.
     * @param timestamp The timestamp associated with the log event, typically in milliseconds since epoch.
     * @param content Additional content or arguments to include in the log message.
     */
    override fun log(name: String, format: String, timestamp: Long, vararg content: Any) {
        val stackTraceElements = Thread.currentThread().stackTrace
        val caller = stackTraceElements[4]

        executor.execute {
            val stringBuilder = StringBuilder()
            for (`object` in content) {
                stringBuilder.append(`object`)
            }
            try {
                if (this.inDebugMode)
                    println(
                        generateConsoleMessage(
                            name,
                            stringBuilder,
                            format,
                            timestamp,
                            caller,
                            *content
                        )
                    )

                writeToFile(
                    generateLogFileMessage(
                        name,
                        stringBuilder,
                        format,
                        timestamp,
                        caller,
                        *content
                    )
                )
            } catch (e: IOException) {
                System.err.println("Log write error: " + e.message)
            }
        }
    }
}
