## 🎯 Release Overview

Version 2.0.0 introduces significant enhancements to Lumina with improved asynchronous logging, better timestamp handling, and a streamlined API for custom logging strategies.

## 🚀 Key Features

- **Asynchronous Logging with Coroutines**:
  - Replaced threading and `ExecutorService` with coroutines for improved performance and simplified asynchronous operations.

- **Enhanced Logging Strategy**:
  - Introduced `LoggingStrategyBuilder` for consistent and flexible logging strategy creation.
  - Removed `DebugLoggingStrategy` in favor of the more versatile builder pattern.

- **Improved Thread Safety**:
  - Updated logging strategy to enhance thread safety using `Mutex`, ensuring consistent logging across multiple threads.

- **Enhanced Timestamp Handling**:
  - Improved timestamp handling with `kotlinx.datetime.Instant` for precise and accurate time tracking.

- **Centralized Logging Utilities**:
  - Introduced `LoggerUtils` for centralized logging utilities like timestamp formatting and log directory management.

## 📦 Installation

1. Update your Gradle or Maven dependency to version 2.0.0.
2. The library will be updated automatically.

## 💻 For Developers

To use Lumina's logging capabilities, update your dependencies as follows:

<details>
<summary>Gradle</summary>

```gradle
implementation 'dev.nelmin:lumina:2.0.0'
```

</details>

<details>
<summary>Gradle (Kotlin)</summary>

```kts
implementation("dev.nelmin:lumina:2.0.0")
```

</details>

<details>
<summary>Maven</summary>

```xml
<dependency>
    <groupId>dev.nelmin</groupId>
    <artifactId>lumina</artifactId>
    <version>2.0.0</version>
</dependency>
```

</details>

Key components:
- Use `Logger` singleton for logging.
- Utilize `LoggingStrategyBuilder` to create custom logging strategies.
- Refer to `LoggerUtils` for centralized logging utilities.

## 📌 System Requirements

- Kotlin 2.1.20 or higher
- Coroutines library (Optional)
- kotlinx-datetime