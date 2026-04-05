# utils

General-purpose utility library with binary I/O streams, compression, and common helpers.

## Package Structure
- `dev.simplified.stream` - ByteArrayDataInput, ByteArrayDataOutput, Compression
- `dev.simplified.stream.exception` - CompressionException
- `dev.simplified.util` - ArrayUtil, CharUtil, ClassUtil, ExceptionUtil, LogUtil, NumberUtil, PrimitiveUtil, Range, RegexUtil, StringUtil, SystemUtil
- `dev.simplified.util.mutable` - Mutable, MutableBoolean, MutableByte, MutableDouble, MutableFloat, MutableInt, MutableLong, MutableShort
- `dev.simplified.util.time` - SimpleDate, Stopwatch

## Key Classes
- `ByteArrayDataInput`/`ByteArrayDataOutput` - Zero-copy byte array I/O
- `Compression` - Data compression/decompression
- `StringUtil`, `NumberUtil`, `ArrayUtil` - Most commonly used utilities
- `Mutable*` - Wrapper types for pass-by-reference primitives
- `Stopwatch` - Elapsed time measurement

## Dependencies
- `com.github.simplified-dev:collections:master-SNAPSHOT`
- JetBrains annotations, Lombok
- JUnit 5, Hamcrest (test)

## Build
```bash
./gradlew build
./gradlew test
```

## Stats
- Java 21, group `dev.simplified`, version `1.0.0`
- 25 source files, 1 test file
- Published via JitPack: `com.github.simplified-dev:utils:master-SNAPSHOT`
