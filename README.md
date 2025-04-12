# 🌟 Lumina - Modular Kotlin Logger
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL%203.0-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

## ✨ What's Lumina?

Lumina is like a Swiss Army knife for logging - simple on the surface, but packed with powerful features! It's a Kotlin-based logging library that makes tracking your application's behavior as easy as sending a text message.

### 🎯 Key Features

- 🌈 **Colorful Console Output**: Pretty [ANSI colors](src/main/kotlin/dev/nelmin/logger/ANSI.kt) make your logs pop! (Because who said logs have to be boring?)
- 🔍 **Smart Stack Traces**: Detailed error tracking that actually makes sense
- 🎨 **Extensible Design**: Like LEGOs, but for logging - build and customize as you need
- 🚀 **Async Logging**: Non-blocking operations using Kotlin Coroutines keep your app speedy
- 📝 **File-based Logging**: Everything gets neatly organized in log files
- ⏱️ **Improved Timestamp Handling**: Using `kotlinx.datetime.Instant` for precise time tracking
- 🧵 **Thread-Safe Logging**: Ensures safe and consistent logging across multiple threads using `Mutex`
- 🛠️ **Custom Logging Strategies**: Easily build custom logging strategies with the new `LoggingStrategyBuilder`
- 📦 **Message Queuing**: Utilizes message queuing via a `Channel` for asynchronous log processing.

## 🎮 Quick Start

Import it via maven central:

<details>
<summary>Gradle</summary>

```gradle
implementation 'dev.nelmin:lumina:2.0.1'
```

</details>

<details>
<summary>Gradle (Kotlin)</summary>

```kts
implementation("dev.nelmin:lumina:2.0.1")
```

</details>

<details>
<summary>Maven</summary>

```xml
<dependency>
    <groupId>dev.nelmin</groupId>
    <artifactId>lumina</artifactId>
    <version>2.0.1</version>
</dependency>
```

</details>

then just use the Logger Singleton:

```kotlin
// Initialize the logger (it's a singleton, no need to create instances!)
Logger.debug("Starting up my awesome app! 🚀")
Logger.info("Everything is running smoothly ✨")
Logger.error("Oops, something went wrong! 😅")
```

For programs or MC Plugins without suspending functions:

```kotlin
Logger.startListeningForLogMessages()
```

## 🎨 Log Levels

Lumina comes with five flavors of logging:
- 🟦 [**DEBUG**](src/main/kotlin/dev/nelmin/logger/strategy/LoggingStrategy.kt): For when you're being extra curious
- ℹ️ [**INFO**](src/main/kotlin/dev/nelmin/logger/strategy/LoggingStrategy.kt): For the "nice to know" stuff
- ⚠️ [**WARN**](src/main/kotlin/dev/nelmin/logger/strategy/LoggingStrategy.kt): For "heads up!" moments
- 🔴 [**ERROR**](src/main/kotlin/dev/nelmin/logger/strategy/LoggingStrategy.kt): For when things go wrong
- ⛔ [**FATAL**](src/main/kotlin/dev/nelmin/logger/strategy/LoggingStrategy.kt): For those "we need to talk" situations
- ⛔ [**STACKTRACE**](src/main/kotlin/dev/nelmin/logger/strategy/StackTraceLoggingStrategy.kt): For those "F*CK" situations

## 🗺️ Future Roadmap

Get excited! Here's what's cooking for future releases:
- 🔄 Log rotation capabilities

## 📜 License

Lumina is proudly open source under the GNU General Public License v3.0. Share the love! ❤️

---

### 🌟 Remember

Lumina is like a trusty sidekick for your application - always there when you need it, never getting in your way when you don't!