## 🎯 Release Overview

Version 2.0.1 includes refactoring of the logging system with structured messaging and queuing, alongside moving logging strategy classes to a new package.

## 🚀 Key Features

- **Refactored Logging Strategies**:
  - Logging strategy classes have been moved to a dedicated `strategy` package for better organization.

- **Structured Messaging and Queuing**:
  - Introduced `LogMessage` class to encapsulate logging details.
  - Refactored `Logger` to utilize message queuing via a `Channel`.
  - Added functions to process queued logs asynchronously.
  - Enhanced logging strategies with better thread safety and flexibility.
  - Several private strategies converted to public for broader accessibility.

## 📦 Installation

1. Update your Gradle or Maven dependency to version 2.0.1.
2. The library will be updated automatically.

## 💻 For Developers

To use Lumina's logging capabilities, update your dependencies as follows:

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

Key components:
- Use `Logger` singleton for logging.
- Utilize `LoggingStrategyBuilder` to create custom logging strategies.
- Refer to `LoggerUtils` for centralized logging utilities.
- For programs and MC Plugins, use `startListeningForLogMessages` to process logs asynchronously without a suspending function.

## 📌 System Requirements

- Kotlin 2.1.20 or higher
- Coroutines library (Optional)
- kotlinx-datetime