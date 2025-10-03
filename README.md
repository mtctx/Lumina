# 🌟 Lumina – Kotlin Logging That *Feels* Good

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## ✨ What is Lumina?

Lumina is a modern, **coroutine-first logger for Kotlin**.
It’s lightweight, colorful, and modular — designed to keep your logs clear, structured, and fun to work with.

Think of it as a **logging toolkit**: simple defaults for day-to-day work, but fully customizable when you need advanced
setups.

👉 Full API documentation: [lumina.apidoc.mtctx.dev](https://lumina.apidoc.mtctx.dev/)

---

## 🎯 Key Features

* 🌈 **Colorful Console Output** – Pretty ANSI colors that make logs readable at a glance.
* 🚀 **Asynchronous by Default** – Uses Kotlin coroutines and channels to keep logging off your main thread.
* 🧵 **Thread-Safe Logging** – Safe across multiple threads using `Mutex`.
* 📦 **Message Queuing** – All logs pass through a channel → no lost messages, no blocking.
* 🔄 **Log Rotation** – Old log directories are automatically cleaned up.
* 🛠️ **Extensible Strategies** – Build custom strategies with `LoggingStrategyBuilder`.
* 📝 **File Logging Included** – Out-of-the-box structured log files per log level.

---

## 🎮 Quick Start

### 1. Add Dependency

<details>
<summary>Gradle (Kotlin DSL)</summary>

```kotlin
implementation("dev.mtctx.library:lumina:3.0.0")
```

</details>

<details>
<summary>Maven</summary>

```xml

<dependency>
    <groupId>dev.mtctx.librarydev.mtctx.library</groupId>
    <artifactId>lumina</artifactId>
    <version>3.0.0</version>
</dependency>
```

</details>

---

### 2. Initialize the Logger

```kotlin
import dev.mtctx.library.*

fun main() {
    val logger = createLogger() // or createLogger { /* dsl here */ }

    logger.info("Lumina is ready to shine! ✨")
    logger.error("Something went wrong... but gracefully 😅")

    // Always stop the logger gracefully
    logger.waitForCoroutinesFinish()
}
```

---

## 🎨 Log Levels

Lumina ships with ready-to-use strategies:

* 🟦 **DEBUG** – for curious dev moments
* ℹ️ **INFO** – the “nice to know” logs
* ⚠️ **WARN** – heads-up situations
* 🔴 **ERROR** – recoverable problems
* ⛔ **FATAL** – “stop everything” issues

But you can also create your own strategies!

---

## ⚡ Sync vs Async Logging

By default, all logs are asynchronous.
If you really need *synchronous* logging (e.g. right before shutdown), Lumina provides `debugSync`, `errorSync`, etc.

⚠️ **But use them carefully!** They can block if channels are full. (That’s why they’re annotated with
`@UseSynchronousFunctionsWithCaution`.)

---

## 🛠️ Custom Strategies

Need something special? You can build your own strategy:

```kotlin
val custom = LoggingStrategyBuilder(
    strategyName = "CUSTOM",
    coroutineScope = myScope,
    mutex = Mutex(),
    ansiColor = ANSI.PURPLE
)
```

---

## 📚 Docs

Full API reference: [https://lumina.apidoc.mtctx.dev](https://lumina.apidoc.mtctx.dev/)

---

## 📜 License

Lumina is open source under the **GNU GPL v3**.
Use it, hack it, improve it — just keep it free. ❤️

---

✨ Lumina isn’t just a logger — it’s your app’s sidekick in understanding itself.