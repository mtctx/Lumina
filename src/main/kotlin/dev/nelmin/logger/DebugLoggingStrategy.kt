package dev.nelmin.logger

import java.io.IOException
import java.util.concurrent.ExecutorService

class DebugLoggingStrategy(private val executor: ExecutorService, var inDebugMode: Boolean) : LoggingStrategy(
    "debug", "DEBUG", executor, ANSI.GREEN
) {
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
