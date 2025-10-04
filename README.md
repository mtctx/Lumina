# Lumina – Kotlin Logging That *Feels* Good

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

---

## What is Lumina?

Lumina is a coroutine-first logging library for Kotlin.  
It aims to be simple by default, but flexible enough when you need to configure it for more serious projects.

You get clean console output with ANSI colors, structured log files, and a DSL for building messages that don’t just end
up as unreadable strings.

---

## What’s New in v4

- Cleaner design: logging, configuration, and strategies are properly separated.
- DSL-based configuration (`createLogger { ... }`).
- Configurable log rotation with safe defaults.
- Structured message DSL (`logger.info { +"line"; keyValue { ... } }`).
- Better shutdown handling (`waitForCoroutinesToFinish`).
- Extensible strategies: build your own `LoggingStrategy`.

---

## Installation

[Available on Maven Central](https://central.sonatype.com/artifact/dev.mtctx.library/lumina)

### Gradle (Kotlin DSL)

```kotlin
implementation("dev.mtctx.lumina:lumina:4.0.0")
````

### Maven

```xml

<dependency>
    <groupId>dev.mtctx.lumina</groupId>
    <artifactId>lumina</artifactId>
    <version>4.0.0</version>
</dependency>
```

---

## Quick Start

```kotlin
import mtctx.lumina.v4.*

fun main() {
    val logger = createLogger {
        name = "MyApp"

        log {
            rotation {
                enabled = true
                duration = 7.days
                interval = 1.days
            }
        }
    }

    runBlocking {
        logger.info { +"Lumina is ready" }
        logger.error { +"Something went wrong" }
    }

    logger.waitForCoroutinesToFinish()
}
```

---

## Log Levels

Lumina ships with common levels:

* DEBUG
* INFO
* WARN
* ERROR
* FATAL

Each has both asynchronous (`logger.info { ... }`) and synchronous (`logger.infoSync { ... }`) variants.
Use sync logging only when you must flush logs immediately (for example, right before shutdown).

---

## Structured Messages

Instead of plain strings, you can build structured logs:

```kotlin
logger.info {
    +"Application started"
    keyValue {
        define("Config", "dbUrl", "jdbc://localhost:5432")
    }
    +"Environment: production"
}
```

---

## Custom Strategies

If the default levels aren’t enough, you can create your own strategy:

```kotlin
val custom = LoggingStrategyBuilder(
    strategyName = "CUSTOM",
    ansiColor = ANSI.PURPLE,
    config = myConfig,
    fileSinks = mutableMapOf()
)
```

---

## Migration from v3

* `Logger` → `Lumina`
* `LoggerConfig` → `LuminaConfig` (with a new DSL)
* `LogMessageDSL` → `MessageDSL`
* `waitForCoroutinesFinish` → `waitForCoroutinesToFinish`

v3 APIs are still present but deprecated, with migration hints in the code.

---

## Documentation

API reference: [https://lumina.apidoc.mtctx.dev](https://lumina.apidoc.mtctx.dev/)

---

## License

Lumina is free software under the GNU GPL v3.
Use it, modify it, and share it — just keep it free.