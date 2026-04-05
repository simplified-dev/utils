# Utils

General-purpose utility library providing binary I/O stream abstractions with zero-copy byte array data input/output, compression utilities, and common helpers for arrays, characters, classes, numbers, primitives, strings, and system operations. Also includes mutable wrapper types, a stopwatch timer, and date formatting.

> [!IMPORTANT]
> This library requires **Java 21** or later. It depends on
> [collections](https://github.com/Simplified-Dev/collections) and is published
> via [JitPack](https://jitpack.io/#simplified-dev/utils).

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
  - [Binary I/O Streams](#binary-io-streams)
  - [Compression](#compression)
  - [Utility Classes](#utility-classes)
  - [Mutable Wrappers](#mutable-wrappers)
  - [Stopwatch](#stopwatch)
- [Project Structure](#project-structure)
- [Building](#building)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Zero-copy binary I/O** - `ByteArrayDataInput` and `ByteArrayDataOutput` for efficient byte array serialization without intermediate copies
- **Compression** - `Compression` utility for data compression/decompression with custom exception handling
- **Array utilities** - `ArrayUtil` for common array operations
- **Character utilities** - `CharUtil` for character inspection and manipulation
- **Class utilities** - `ClassUtil` for reflective class operations
- **Number utilities** - `NumberUtil` for numeric parsing and validation
- **Primitive utilities** - `PrimitiveUtil` for boxing/unboxing and type checks
- **String utilities** - `StringUtil` for string manipulation and formatting
- **System utilities** - `SystemUtil` for system-level operations
- **Regex utilities** - `RegexUtil` for pattern matching helpers
- **Range type** - `Range` for representing bounded numeric intervals
- **Mutable wrappers** - `MutableInt`, `MutableLong`, `MutableDouble`, `MutableFloat`, `MutableByte`, `MutableShort`, `MutableBoolean` for pass-by-reference semantics
- **Timing** - `Stopwatch` for measuring elapsed time
- **Date formatting** - `SimpleDate` for straightforward date operations
- **Exception utilities** - `ExceptionUtil` for exception handling helpers
- **Logging utilities** - `LogUtil` for logging convenience methods

## Getting Started

### Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| [Java](https://adoptium.net/) | **21+** | Required |
| [Gradle](https://gradle.org/) | **9.4+** | Included via wrapper |
| [Git](https://git-scm.com/) | 2.x+ | For cloning the repository |

### Installation

Add the JitPack repository and dependency to your `build.gradle.kts`:

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.simplified-dev:utils:master-SNAPSHOT")
}
```

> [!NOTE]
> This library transitively depends on
> [`collections`](https://github.com/Simplified-Dev/collections).
> JitPack resolves it automatically.

<details>
<summary>Groovy DSL</summary>

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.simplified-dev:utils:master-SNAPSHOT'
}
```

</details>

<details>
<summary>Maven</summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.simplified-dev</groupId>
    <artifactId>utils</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

</details>

## Usage

### Binary I/O Streams

Zero-copy byte array reading and writing:

```java
import dev.simplified.stream.ByteArrayDataOutput;
import dev.simplified.stream.ByteArrayDataInput;

// Write data
ByteArrayDataOutput output = new ByteArrayDataOutput();
output.writeInt(42);
output.writeUTF("hello");
byte[] bytes = output.toByteArray();

// Read data
ByteArrayDataInput input = new ByteArrayDataInput(bytes);
int number = input.readInt();
String text = input.readUTF();
```

### Compression

Compress and decompress byte arrays:

```java
import dev.simplified.stream.Compression;

byte[] compressed = Compression.compress(data);
byte[] decompressed = Compression.decompress(compressed);
```

### Utility Classes

Common helpers for everyday operations:

```java
import dev.simplified.util.StringUtil;
import dev.simplified.util.NumberUtil;
import dev.simplified.util.ArrayUtil;

// String operations
boolean empty = StringUtil.isEmpty(value);

// Number parsing
int parsed = NumberUtil.parseInt(text, defaultValue);

// Array operations
String[] merged = ArrayUtil.merge(first, second);
```

### Mutable Wrappers

Pass-by-reference semantics for primitives:

```java
import dev.simplified.util.mutable.MutableInt;

MutableInt counter = new MutableInt(0);
counter.increment();
int value = counter.get();
```

### Stopwatch

Measure elapsed time:

```java
import dev.simplified.util.time.Stopwatch;

Stopwatch stopwatch = Stopwatch.start();
// ... perform work ...
long elapsedMs = stopwatch.elapsedMillis();
```

## Project Structure

```
utils/
├── src/
│   ├── main/java/dev/simplified/
│   │   ├── stream/
│   │   │   ├── ByteArrayDataInput.java     # Zero-copy byte array reading
│   │   │   ├── ByteArrayDataOutput.java    # Zero-copy byte array writing
│   │   │   ├── Compression.java            # Data compression/decompression
│   │   │   └── exception/
│   │   │       └── CompressionException.java
│   │   └── util/
│   │       ├── ArrayUtil.java              # Array operations
│   │       ├── CharUtil.java               # Character utilities
│   │       ├── ClassUtil.java              # Reflective class operations
│   │       ├── ExceptionUtil.java          # Exception handling helpers
│   │       ├── LogUtil.java                # Logging convenience methods
│   │       ├── NumberUtil.java             # Numeric parsing and validation
│   │       ├── PrimitiveUtil.java          # Boxing/unboxing, type checks
│   │       ├── Range.java                  # Bounded numeric intervals
│   │       ├── RegexUtil.java              # Pattern matching helpers
│   │       ├── StringUtil.java             # String manipulation
│   │       ├── SystemUtil.java             # System-level operations
│   │       ├── mutable/                    # Mutable, MutableInt, MutableLong,
│   │       │                               # MutableDouble, MutableFloat,
│   │       │                               # MutableByte, MutableShort, MutableBoolean
│   │       └── time/
│   │           ├── SimpleDate.java         # Date formatting
│   │           └── Stopwatch.java          # Elapsed time measurement
│   └── test/java/                          # JUnit 5 tests
├── build.gradle.kts
├── settings.gradle.kts
└── LICENSE.md
```

## Building

Build the project using the Gradle wrapper:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for development setup, code style
guidelines, and how to submit a pull request.

## License

This project is licensed under the **Apache License 2.0** - see
[LICENSE.md](LICENSE.md) for the full text.
