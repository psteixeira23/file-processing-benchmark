# File Processing Benchmark (Java 21)

![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=alert_status)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=coverage)
![Maintainability](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=sqale_rating)
![Reliability](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=reliability_rating)

## Overview

This project benchmarks multiple strategies for reading and processing large CSV files
in Java 21. It compares execution time, memory usage, scalability, and code clarity
across different I/O approaches while running the same scenarios on each strategy.

## Features

- Four file-reading strategies (BufferedReader, Files.lines, NIO ByteBuffer, MemoryMapped).
- Two execution modes (single-pass and isolated) for realistic and analytical comparisons.
- Scenario-based processing with per-scenario timings and counts.
- Console report plus an HTML report for easier inspection.
- Modular architecture with interfaces and isolated strategy implementations.
- Unit tests with high coverage.

## Requirements

- Java 21
- Maven 3.9+

## Project Layout

```
src/main/java/com/patrick/benchmark/
  App.java
  BenchmarkRunner.java
  BenchmarkReport.java
  BenchmarkResult.java
  ProcessingMode.java
  metrics/
  processing/
  processing/scenario/
  readers/
  reporting/
src/main/resources/
  benchmark-input.csv
src/test/resources/
  benchmark-input-test.csv
reports/
  benchmark-report.html
```

## Input Data

The CSV format expects a header and six columns:

```
uf,tipo_doenca,local_obito,faixa_etaria,sexo,total
```

The header line is skipped during processing.

Included datasets:

- `src/main/resources/benchmark-input.csv`  
  Sample dataset for local runs.
- `src/test/resources/benchmark-input-test.csv`  
  Small dataset used by unit tests.

You can replace the sample dataset with your own file, as long as it follows
the same format.

## Scenarios

Each strategy is evaluated against the same scenarios:

- Total number of records.
- Records where `local_obito` is not `HOSPITAL` or `DOMICILIO`.
- Counts by `faixa_etaria`.
- Counts by sex (`M`, `F`, and `I` treated as `Other`).
- Counts by `uf`.

## Execution Modes

- **Single-Pass (Realistic)**: One pass computes all scenarios together.  
  Scenario time reflects processing within the same pass.

- **Isolated (Analytical)**: One pass per scenario.  
  Scenario time includes the full pass time for that scenario.

## Running

### 1) Build

```
mvn -q -DskipTests package
```

### 2) Execute the benchmark

```
java -cp target/classes com.patrick.benchmark.App <file> [--mode=single|isolated] [--charset=UTF-8]
```

Example:

```
java -cp target/classes com.patrick.benchmark.App ./src/main/resources/benchmark-input.csv --mode=isolated
```

### HTML Report

The HTML report is written to:

```
reports/benchmark-report.html
```

If the file already exists, it is overwritten by the latest run.

## Output

The console and HTML reports provide:

- Total execution time per strategy.
- Memory delta in MB (approximate).
- Records processed and invalid lines.
- Scenario timings and counts.
- Error counts and messages when failures occur.

## Testing

```
mvn test
```

The project enforces a minimum coverage threshold via JaCoCo.

## Notes

- Memory usage is approximate and based on a before/after snapshot.
- The CSV header is always ignored.
- Charset defaults to UTF-8; use `--charset=` to override.

## License

MIT. See `LICENSE`.
